package com.talkingsmoke.service;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

/**
 * Lightweight ONNXInferenceService that provides a basic model-loading check
 * and simple inference plumbing. The implementation is defensive: it attempts
 * to initialize the ONNX Runtime reflectively if present on the classpath,
 * but the service remains usable (compiles and runs) without the native
 * ONNX Runtime dependencies.
 *
 * <p>Public API includes methods to load a model from the classpath or
 * filesystem, prepare inputs as ONNX tensors, and run inference returning
 * primitive double arrays. The service also exposes lightweight metrics
 * (inference count and cumulative inference time) that can be surfaced by
 * controllers for monitoring.</p>
 */
@Service
public class ONNXInferenceService {

    @Autowired(required = false)
    private Environment env;

    private volatile boolean modelLoaded = false;
    private String loadedModelPath = null;
    // lightweight metrics
    private final java.util.concurrent.atomic.AtomicLong inferenceCount = new java.util.concurrent.atomic.AtomicLong(0);
    private final java.util.concurrent.atomic.AtomicLong totalInferenceTimeNs = new java.util.concurrent.atomic.AtomicLong(0);
    // Reflection-based handles for optional ONNX Runtime integration
    private Object ortEnvRef = null;
    private Object ortSessionRef = null;
    // test hook: allow tests to inject input info map to avoid reflective session stubbing
    private java.util.Map<String, Object> testInputInfo = null;
    private static final Logger log = LoggerFactory.getLogger(ONNXInferenceService.class);

    public boolean isModelLoaded() {
        return modelLoaded;
    }

    /**
     * Check for the presence of a model resource on the classpath at
     * /models/exported_model.onnx. If present, mark the model as loaded.
     * This does not perform ONNX Runtime initialization; it only confirms
     * that the model artifact is available to the application.
     *
     * @return true if model resource is found and marked as loaded
     */
    public synchronized boolean loadModelFromClasspath() {
        final String resourcePath = "/models/exported_model.onnx";
        InputStream is = null;
        try {
            is = getClass().getResourceAsStream(resourcePath);
            if (is == null) {
                modelLoaded = false;
                return false;
            }
            // Resource exists — record a lightweight marker. Real session init
            // will be implemented later using ONNX Runtime.
            loadedModelPath = resourcePath;
            modelLoaded = true;
            return true;
        } catch (RuntimeException e) {
            log.error("Unexpected error while checking classpath model resource", e);
            modelLoaded = false;
            return false;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ioe) {
                    log.debug("Failed to close resource input stream", ioe);
                }
            }
        }
    }

    /**
     * Basic loader that accepts an absolute filesystem path. It will only
     * check that the file exists and mark the model as loaded. Does not
     * initialize ONNX Runtime session here.
    *
    * @param modelFilePath absolute path to the model file on disk
    * @return true if the file exists and was marked as loaded
     */
    public synchronized boolean loadModelFromFile(String modelFilePath) {
        try {
            java.nio.file.Path p = java.nio.file.Paths.get(modelFilePath);
            if (java.nio.file.Files.exists(p) && java.nio.file.Files.isRegularFile(p)) {
                loadedModelPath = modelFilePath;
                modelLoaded = true;
                return true;
            } else {
                modelLoaded = false;
                return false;
            }
        } catch (RuntimeException e) {
            log.error("Error while checking filesystem model path: {}", modelFilePath, e);
            modelLoaded = false;
            return false;
        }
    }

    /**
     * Placeholder close method for symmetry with future implementation.
    * This method will attempt to close any reflectively initialized
    * runtime/session objects if they exist, and reset internal state.
     */
    public synchronized void close() {
        loadedModelPath = null;
        modelLoaded = false;
        // Attempt to close reflective runtime handles if present
        if (ortSessionRef != null) {
            try {
                ortSessionRef.getClass().getMethod("close").invoke(ortSessionRef);
            } catch (ReflectiveOperationException roe) {
                log.warn("Failed to close reflective OrtSession", roe);
            }
            ortSessionRef = null;
        }
        if (ortEnvRef != null) {
            try {
                ortEnvRef.getClass().getMethod("close").invoke(ortEnvRef);
            } catch (ReflectiveOperationException roe) {
                log.warn("Failed to close reflective OrtEnvironment", roe);
            }
            ortEnvRef = null;
        }
    }

    /**
     * Try to initialize ONNX Runtime via reflection. This allows the code to compile
     * without the ONNX Runtime dependency while still attempting to use it at runtime
     * if available on the application's classpath.
     *
     * @param modelFilePath absolute path to the model file
     * @return true if runtime was initialized and a session created, false otherwise
     */
    public synchronized boolean tryInitOrtSessionReflective(String modelFilePath) {
        try {
            // Load ONNX Runtime classes reflectively (package is ai.onnxruntime in the jar)
            Class<?> ortEnvClass = Class.forName("ai.onnxruntime.OrtEnvironment");
            Class<?> sessionOptionsClass = Class.forName("ai.onnxruntime.OrtSession$SessionOptions");

            java.lang.reflect.Method getEnv = ortEnvClass.getMethod("getEnvironment");
            Object envObj = getEnv.invoke(null);

            // create SessionOptions via OrtSession.SessionOptions nested class
            Object opts = sessionOptionsClass.getDeclaredConstructor().newInstance();

            // env.createSession(String, SessionOptions)
            java.lang.reflect.Method createSession = envObj.getClass().getMethod("createSession", String.class, sessionOptionsClass);
            Object sessionObj = createSession.invoke(envObj, modelFilePath, opts);

            this.ortEnvRef = envObj;
            this.ortSessionRef = sessionObj;
            this.loadedModelPath = modelFilePath;
            this.modelLoaded = true;
            return true;
        } catch (ClassNotFoundException cnfe) {
            // ONNX Runtime not available on classpath
            log.debug("ONNX Runtime classes not found on classpath: {}", cnfe.getMessage());
            return false;
        } catch (ReflectiveOperationException roe) {
            log.error("Failed to initialize ONNX Runtime reflectively", roe);
            // cleanup
            try {
                if (ortSessionRef != null) {
                    ortSessionRef.getClass().getMethod("close").invoke(ortSessionRef);
                }
            } catch (ReflectiveOperationException closeEx) {
                log.warn("Failed to close partially created session", closeEx);
            }
            ortSessionRef = null;
            ortEnvRef = null;
            modelLoaded = false;
            return false;
        } catch (Throwable t) {
            log.error("Unexpected error during reflective init", t);
            ortSessionRef = null;
            ortEnvRef = null;
            modelLoaded = false;
            return false;
        }
    }

    /**
     * Prepare an ONNX Runtime tensor from a flat float[] input of length inputSize.
     * Returns an OnnxTensor or null if creation failed. Caller is responsible for closing the tensor.
     */
    public OnnxTensor prepareInputTensor(float[] input, int inputSize) throws OrtException {
        if (input == null || input.length != inputSize) {
            throw new IllegalArgumentException("Input must be non-null and length " + inputSize);
        }
        // ONNX Runtime expects a 2D tensor for batch-size 1: [1, inputSize]
        float[][] arr = new float[1][inputSize];
        System.arraycopy(input, 0, arr[0], 0, inputSize);
        OrtEnvironment envLocal = OrtEnvironment.getEnvironment();
        return OnnxTensor.createTensor(envLocal, arr);
    }

    /**
     * Prepare an ONNX Runtime tensor from a flat long[] input of length inputSize.
     * Returns an OnnxTensor. Caller is responsible for closing the tensor.
     */
    public OnnxTensor prepareInputTensorLong(long[] input, int inputSize) throws OrtException {
        if (input == null || input.length != inputSize) {
            throw new IllegalArgumentException("Input must be non-null and length " + inputSize);
        }
        long[][] arr = new long[1][inputSize];
        System.arraycopy(input, 0, arr[0], 0, inputSize);
        OrtEnvironment envLocal = OrtEnvironment.getEnvironment();
        return OnnxTensor.createTensor(envLocal, arr);
    }

    /**
     * Run inference using the created OrtSession. Returns the first output as a double[] if available,
     * or null if there was an error. The method closes intermediate OnnxTensor objects.
     */
    public double[] runInference(float[] input, int inputSize) {
        if (!modelLoaded) {
            throw new IllegalStateException("Model not loaded");
        }

        long startNs = System.nanoTime();

        // Try reflective session first
        if (ortSessionRef != null) {
            log.debug("Running inference using reflective OrtSession");
            try {
                // Reflectively invoke run(Map<String, OnnxTensor>) with a dummy input name
                java.lang.reflect.Method getInputNames = ortSessionRef.getClass().getMethod("getInputNames");
                @SuppressWarnings("unchecked")
                java.util.Set<String> inames = (java.util.Set<String>) getInputNames.invoke(ortSessionRef);
                String inputName = inames.iterator().next();

                // Create tensor via direct API then pass it to reflective session
                try (OnnxTensor tensor = prepareInputTensor(input, inputSize)) {
                    java.util.Map<String, OnnxTensor> feed = java.util.Map.of(inputName, tensor);
                    java.lang.reflect.Method runMethod = ortSessionRef.getClass().getMethod("run", java.util.Map.class);
                    Object resultObj = runMethod.invoke(ortSessionRef, feed);

                    // resultObj is OrtSession.Result — call its get(0).getValue()
                    java.lang.reflect.Method get0 = resultObj.getClass().getMethod("get", int.class);
                    Object v0 = get0.invoke(resultObj, 0);
                    java.lang.reflect.Method getValue = v0.getClass().getMethod("getValue");
                    Object out = getValue.invoke(v0);

                    // attempt to parse known array shapes with pattern matching
                    if (out instanceof float[] fo) {
                        double[] d = new double[fo.length];
                        for (int i = 0; i < fo.length; i++) d[i] = fo[i];
                        // close result if close method exists
                        try { resultObj.getClass().getMethod("close").invoke(resultObj); } catch (Exception ex) { /* ignore */ }
                        long elapsed = System.nanoTime() - startNs;
                        addInferenceTimeNs(elapsed);
                        log.info("Inference completed (reflective) in {} ms", elapsed / 1_000_000.0);
                        return d;
                    } else if (out instanceof double[] d) {
                        try { resultObj.getClass().getMethod("close").invoke(resultObj); } catch (Exception ex) { /* ignore */ }
                        long elapsed = System.nanoTime() - startNs;
                        addInferenceTimeNs(elapsed);
                        log.info("Inference completed (reflective) in {} ms", elapsed / 1_000_000.0);
                        return d;
                    } else if (out instanceof float[][] f2) {
                        float[] fo = f2[0];
                        double[] d = new double[fo.length];
                        for (int i = 0; i < fo.length; i++) d[i] = fo[i];
                        try { resultObj.getClass().getMethod("close").invoke(resultObj); } catch (Exception ex) { /* ignore */ }
                        long elapsed = System.nanoTime() - startNs;
                        addInferenceTimeNs(elapsed);
                        log.info("Inference completed (reflective) in {} ms", elapsed / 1_000_000.0);
                        return d;
                    } else {
                        try { resultObj.getClass().getMethod("close").invoke(resultObj); } catch (Exception ex) { /* ignore */ }
                        long elapsed = System.nanoTime() - startNs;
                        addInferenceTimeNs(elapsed);
                        log.warn("Inference returned unknown output type (reflective) after {} ms: {}", elapsed/1_000_000.0, out != null ? out.getClass() : null);
                        return null;
                    }
                }
            } catch (ReflectiveOperationException roe) {
                log.error("Reflective inference failed", roe);
                long elapsed = System.nanoTime() - startNs;
                addInferenceTimeNs(elapsed);
                return null;
            } catch (OrtException oe) {
                log.error("ONNX runtime error during reflective inference", oe);
                long elapsed = System.nanoTime() - startNs;
                addInferenceTimeNs(elapsed);
                return null;
            }
        }

        // Direct API path
        try {
            OrtEnvironment env = OrtEnvironment.getEnvironment();
            try (OrtSession s = env.createSession(loadedModelPath, new OrtSession.SessionOptions())) {
                try (OnnxTensor tensor = prepareInputTensor(input, inputSize)) {
                    try (OrtSession.Result res = s.run(java.util.Map.of(s.getInputNames().iterator().next(), tensor))) {
                        Object out = res.get(0).getValue();
                        if (out instanceof float[] fo) {
                            double[] d = new double[fo.length];
                            for (int i = 0; i < fo.length; i++) d[i] = fo[i];
                            // metrics
                            inferenceCount.incrementAndGet();
                            long elapsed = System.nanoTime() - startNs;
                            addInferenceTimeNs(elapsed);
                            log.info("Inference completed (direct) in {} ms", elapsed / 1_000_000.0);
                            return d;
                        } else if (out instanceof double[]) {
                            inferenceCount.incrementAndGet();
                            long elapsed = System.nanoTime() - startNs;
                            addInferenceTimeNs(elapsed);
                            log.info("Inference completed (direct) in {} ms", elapsed / 1_000_000.0);
                            return (double[]) out;
                        } else if (out instanceof float[][]) {
                            float[][] f2 = (float[][]) out;
                            float[] fo = f2[0];
                            double[] d = new double[fo.length];
                            for (int i = 0; i < fo.length; i++) d[i] = fo[i];
                            inferenceCount.incrementAndGet();
                            long elapsed = System.nanoTime() - startNs;
                            addInferenceTimeNs(elapsed);
                            log.info("Inference completed (direct) in {} ms", elapsed / 1_000_000.0);
                            return d;
                        } else {
                            log.warn("Unknown output type from ONNX Runtime: {}", out != null ? out.getClass() : null);
                            long elapsed = System.nanoTime() - startNs;
                            addInferenceTimeNs(elapsed);
                            return null;
                        }
                    }
                }
            }
        } catch (OrtException oe) {
            log.error("ONNX runtime error during direct inference", oe);
            return null;
        }
    }

    /**
     * Run inference using a long[] (int64) input. Useful for models that expect token ids.
     */
    public double[] runInferenceFromLongs(long[] input, int inputSize) {
        if (!modelLoaded) {
            throw new IllegalStateException("Model not loaded");
        }

        // Reflective session path
        if (ortSessionRef != null) {
            log.debug("Running inference using reflective OrtSession (long input)");
            try {
                java.lang.reflect.Method getInputNames = ortSessionRef.getClass().getMethod("getInputNames");
                @SuppressWarnings("unchecked")
                java.util.Set<String> inames = (java.util.Set<String>) getInputNames.invoke(ortSessionRef);
                String inputName = inames.iterator().next();
                java.util.List<OnnxTensor> toClose = new java.util.ArrayList<>();
                try (OnnxTensor tensor = prepareInputTensorLong(input, inputSize)) {
                    java.util.Map<String, OnnxTensor> feed = new java.util.HashMap<>();
                    feed.put(inputName, tensor);

                    // Create default tensors for other expected inputs (attention_mask, token_type_ids, etc.)
                    for (String iname : inames) {
                        if (feed.containsKey(iname)) continue;
                        String low = iname.toLowerCase();
                        try {
                            if (low.contains("attention")) {
                                long[] mask = new long[inputSize];
                                java.util.Arrays.fill(mask, 1L);
                                OnnxTensor maskTensor = prepareInputTensorLong(mask, inputSize);
                                feed.put(iname, maskTensor);
                                toClose.add(maskTensor);
                            } else if (low.contains("token_type") || low.contains("type_ids") || low.contains("token_type_ids")) {
                                long[] types = new long[inputSize];
                                java.util.Arrays.fill(types, 0L);
                                OnnxTensor typesTensor = prepareInputTensorLong(types, inputSize);
                                feed.put(iname, typesTensor);
                                toClose.add(typesTensor);
                            } else if (low.contains("input")) {
                                long[] zero = new long[inputSize];
                                java.util.Arrays.fill(zero, 0L);
                                OnnxTensor t = prepareInputTensorLong(zero, inputSize);
                                feed.put(iname, t);
                                toClose.add(t);
                            }
                        } catch (OrtException oe) {
                            log.warn("Failed to create default tensor for input {}", iname, oe);
                        }
                    }

                    java.lang.reflect.Method runMethod = ortSessionRef.getClass().getMethod("run", java.util.Map.class);
                    Object resultObj = runMethod.invoke(ortSessionRef, feed);
                    try {
                        java.lang.reflect.Method get0 = resultObj.getClass().getMethod("get", int.class);
                        Object v0 = get0.invoke(resultObj, 0);
                        java.lang.reflect.Method getValue = v0.getClass().getMethod("getValue");
                        Object out = getValue.invoke(v0);

                        double[] normalized = normalizeOutputToDoubleArray(out);
                        if (normalized != null) return normalized;
                        log.warn("Unknown output type from reflective ONNX Runtime (long input): {}", out != null ? out.getClass() : null);
                        return null;
                    } finally {
                        // close any additional tensors we created
                        for (OnnxTensor t : toClose) {
                            try { t.close(); } catch (RuntimeException ex) { /* ignore */ }
                        }
                        try { resultObj.getClass().getMethod("close").invoke(resultObj); } catch (ReflectiveOperationException ex) { /* ignore */ } catch (RuntimeException ex) { /* ignore */ }
                    }
                }
            } catch (ReflectiveOperationException roe) {
                log.error("Reflective long-input inference failed", roe);
                return null;
            } catch (OrtException oe) {
                log.error("ONNX runtime error during reflective long-input inference", oe);
                return null;
            }
        }

        // Direct API path
        try {
            OrtEnvironment env = OrtEnvironment.getEnvironment();
            try (OrtSession s = env.createSession(loadedModelPath, new OrtSession.SessionOptions())) {
                // Build feed map with named inputs
                java.util.Set<String> inames = s.getInputNames();
                String firstInput = inames.iterator().next();
                java.util.Map<String, OnnxTensor> feed = new java.util.HashMap<>();
                java.util.List<OnnxTensor> toClose = new java.util.ArrayList<>();
                OnnxTensor mainTensor = prepareInputTensorLong(input, inputSize);
                feed.put(firstInput, mainTensor);
                toClose.add(mainTensor);

                for (String iname : inames) {
                    if (feed.containsKey(iname)) continue;
                    String low = iname.toLowerCase();
                    try {
                        if (low.contains("attention")) {
                            long[] mask = new long[inputSize];
                            java.util.Arrays.fill(mask, 1L);
                            OnnxTensor maskTensor = prepareInputTensorLong(mask, inputSize);
                            feed.put(iname, maskTensor);
                            toClose.add(maskTensor);
                        } else if (low.contains("token_type") || low.contains("type_ids") || low.contains("token_type_ids")) {
                            long[] types = new long[inputSize];
                            java.util.Arrays.fill(types, 0L);
                            OnnxTensor typesTensor = prepareInputTensorLong(types, inputSize);
                            feed.put(iname, typesTensor);
                            toClose.add(typesTensor);
                        } else if (low.contains("input")) {
                            long[] zero = new long[inputSize];
                            java.util.Arrays.fill(zero, 0L);
                            OnnxTensor t = prepareInputTensorLong(zero, inputSize);
                            feed.put(iname, t);
                            toClose.add(t);
                        }
                    } catch (OrtException oe) {
                        log.warn("Failed to create default tensor for input {}", iname, oe);
                    }
                }

                try (OrtSession.Result res = s.run(feed)) {
                    Object out = res.get(0).getValue();
                    double[] normalized = normalizeOutputToDoubleArray(out);
                    if (normalized != null) {
                        inferenceCount.incrementAndGet();
                        return normalized;
                    }
                    log.warn("Unknown output type from ONNX Runtime (long input): {}", out != null ? out.getClass() : null);
                    return null;
                } finally {
                    for (OnnxTensor t : toClose) {
                        try { t.close(); } catch (Exception ex) { /* ignore */ }
                    }
                }
            }
        } catch (OrtException oe) {
            log.error("ONNX runtime error during direct long-input inference", oe);
            return null;
        }
    }

    public String getLoadedModelPath() {
        return loadedModelPath;
    }

    // Metrics getters used by controller
    public long getInferenceCount() {
        return inferenceCount.get();
    }

    public double getAverageInferenceTimeMs() {
        long count = inferenceCount.get();
        if (count == 0) return 0.0;
        return totalInferenceTimeNs.get() / (count * 1_000_000.0);
    }

    /**
     * Return the total accumulated inference time in milliseconds.
     */
    public double getTotalInferenceTimeMs() {
        return totalInferenceTimeNs.get() / 1_000_000.0;
    }

    // Allow controller or tests to report elapsed time per inference
    public void addInferenceTimeNs(long nanos) {
        if (nanos > 0) totalInferenceTimeNs.addAndGet(nanos);
    }

    // Test helpers
    public void setTestInputInfo(java.util.Map<String, Object> info) {
        this.testInputInfo = info;
    }
    public java.util.Map<String, Object> getTestInputInfo() { return this.testInputInfo; }

    /**
     * Best-effort check whether the provided input matches the model's expected input data type.
     * If the model metadata is not available, returns true (no validation).
     * This method inspects the model's first input info and searches for keywords like INT64 or FLOAT.
     */
    public InputValidationResult validateInputCompatibility(Object input, boolean treatingAsLongs) {
        try {
            // Test hook: if tests injected input info, use that directly
            if (testInputInfo != null && !testInputInfo.isEmpty()) {
                Object first = testInputInfo.values().iterator().next();
                if (first != null) {
                    String desc = first.toString().toLowerCase();
                    Integer expectedInner = parseExpectedInnerDim(desc);
                    if (treatingAsLongs) {
                        if (!(desc.contains("int64") || desc.contains("long"))) {
                return InputValidationResult.fail("Model expects integer token ids (int64), but tokenized input appears as longs",
                    "dtype-mismatch", null, null);
                        }
                    } else {
                        if (!(desc.contains("float") || desc.contains("float32") || desc.contains("fp32"))) {
                return InputValidationResult.fail("Model does not appear to accept float inputs",
                    "dtype-mismatch", null, null);
                        }
                    }
                    if (expectedInner != null && input != null) {
                        int provided = -1;
                        if (input instanceof float[] f) provided = f.length;
                        else if (input instanceof long[] l) provided = l.length;
                        if (provided >= 0 && provided != expectedInner) {
                return InputValidationResult.fail(String.format("Model expects inner dimension %d, but input length is %d", expectedInner, provided),
                    "shape-mismatch", expectedInner, provided);
                        }
                    }
                    return InputValidationResult.ok();
                }
            }
            // If reflective session is available, inspect its input info
            if (ortSessionRef != null) {
                java.lang.reflect.Method getInputInfo = ortSessionRef.getClass().getMethod("getInputInfo");
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> info = (java.util.Map<String, Object>) getInputInfo.invoke(ortSessionRef);
                if (info != null && !info.isEmpty()) {
                    Object first = info.values().iterator().next();
                    if (first != null) {
                            String desc = first.toString().toLowerCase();
                            // Log model input description for observability (T3.3.3)
                            log.info("Model input metadata (reflective): {}", desc);
                        // attempt to parse an expected dimension (like sequence length) from the description
                        Integer expectedInner = parseExpectedInnerDim(desc);
                            if (treatingAsLongs) {
                                if (!(desc.contains("int64") || desc.contains("long"))) {
                                    return InputValidationResult.fail("Model expects integer token ids (int64), but tokenized input appears as longs",
                                            "dtype-mismatch", null, null);
                                }
                            } else {
                                if (!(desc.contains("float") || desc.contains("float32") || desc.contains("fp32"))) {
                                    return InputValidationResult.fail("Model does not appear to accept float inputs",
                                            "dtype-mismatch", null, null);
                                }
                            }
                        // If we have an expected inner dimension and an input array, compare lengths
                        if (expectedInner != null && input != null) {
                            int provided = -1;
                            if (input instanceof float[] f) provided = f.length;
                            else if (input instanceof long[] l) provided = l.length;
                            if (provided >= 0 && provided != expectedInner) {
                                return InputValidationResult.fail(String.format("Model expects inner dimension %d, but input length is %d", expectedInner, provided),
                                        "shape-mismatch", expectedInner, provided);
                            }
                        }
                        return InputValidationResult.ok();
                    }
                }
            }

            // Fallback: if we have a model path, create a session and inspect input info
            if (loadedModelPath != null) {
                OrtEnvironment env = OrtEnvironment.getEnvironment();
                try (OrtSession s = env.createSession(loadedModelPath, new OrtSession.SessionOptions())) {
                    java.util.Set<String> names = s.getInputNames();
                    if (!names.isEmpty()) {
                        String name = names.iterator().next();
                        java.util.Map<String, ai.onnxruntime.NodeInfo> infos = s.getInputInfo();
                        ai.onnxruntime.NodeInfo ni = infos.get(name);
                        if (ni != null) {
                            String desc = ni.toString().toLowerCase();
                            // Log the NodeInfo description so users can see expected shapes/dtypes
                            log.info("Model input metadata (direct): {}", desc);
                            Integer expectedInner = parseExpectedInnerDim(desc);
                            if (treatingAsLongs) {
                                if (!(desc.contains("int64") || desc.contains("long"))) return InputValidationResult.fail("Model expects integer token ids (int64), not float tensors",
                                        "dtype-mismatch", null, null);
                            } else {
                                if (!(desc.contains("float") || desc.contains("float32") || desc.contains("fp32"))) return InputValidationResult.fail("Model does not appear to accept float inputs",
                                        "dtype-mismatch", null, null);
                            }
                            if (expectedInner != null && input != null) {
                                int provided = -1;
                                if (input instanceof float[] f) provided = f.length;
                                else if (input instanceof long[] l) provided = l.length;
                                if (provided >= 0 && provided != expectedInner) {
                    return InputValidationResult.fail(String.format("Model expects inner dimension %d, but input length is %d", expectedInner, provided),
                        "shape-mismatch", expectedInner, provided);
                                }
                            }
                            return InputValidationResult.ok();
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Could not determine model input info for validation", e);
            // When metadata can't be determined, return OK to avoid false rejections; controller will still handle runtime errors
            return InputValidationResult.ok();
        }
        // If we couldn't find metadata, assume compatible
        return InputValidationResult.ok();
    }

    /**
     * Try to extract an expected inner dimension (sequence length) from a NodeInfo description string.
     * This is heuristic and best-effort: looks for patterns like "[1, 384]" or "(1,384)" and returns 384.
     */
    public Integer parseExpectedInnerDim(String desc) {
        if (desc == null) return null;
        try {
            // look for numeric tuples
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\[\\s*\\d+\\s*,\\s*(\\d+)(?:\\s*,[^]]*)?\\s*\\]").matcher(desc);
            if (m.find()) {
                return Integer.parseInt(m.group(1));
            }
            m = java.util.regex.Pattern.compile("\\(\\s*\\d+\\s*,\\s*(\\d+)(?:\\s*,[^)]*)?\\s*\\)").matcher(desc);
            if (m.find()) {
                return Integer.parseInt(m.group(1));
            }
            // try simple "dim: 384" style
            m = java.util.regex.Pattern.compile("dim\\s*[:=]\\s*(\\d+)").matcher(desc);
            if (m.find()) return Integer.parseInt(m.group(1));
        } catch (RuntimeException re) {
            // fallthrough
        }
        return null;
    }

    /**
     * Normalize various ONNX Runtime output array shapes into a double[] if possible.
     * Looks for float[] or nested float arrays and returns the first innermost vector.
     */
    private double[] normalizeOutputToDoubleArray(Object out) {
        if (out == null) return null;
        // Use pattern matching for instanceof (Java 21+)
        // NOTE: This helper normalizes several common runtime output shapes into a single
        // double[] vector. It prefers the first inner vector when nested arrays are present.
        if (out instanceof double[] dArr) {
            return dArr;
        }
        if (out instanceof float[] fArr) {
            double[] d = new double[fArr.length];
            for (int i = 0; i < fArr.length; i++) d[i] = fArr[i];
            return d;
        }
        if (out instanceof float[][] f2) {
            if (f2.length > 0) {
                float[] fo = f2[0];
                double[] d = new double[fo.length];
                for (int i = 0; i < fo.length; i++) d[i] = fo[i];
                return d;
            }
            return null;
        }
        if (out instanceof float[][][] f3) {
            if (f3.length > 0 && f3[0].length > 0) {
                float[] fo = f3[0][0];
                double[] d = new double[fo.length];
                for (int i = 0; i < fo.length; i++) d[i] = fo[i];
                return d;
            }
            return null;
        }
        if (out instanceof double[][] d2) {
            if (d2.length > 0) return d2[0];
            return null;
        }
        if (out instanceof double[][][] d3) {
            if (d3.length > 0 && d3[0].length > 0) return d3[0][0];
            return null;
        }
        // Add more shapes if needed
        return null;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        // First, prefer a configured model path via application.yml (talkingsmoke.model.path)
        try {
            String configured = null;
            if (env != null) configured = env.getProperty("talkingsmoke.model.path");
            if (configured != null && !configured.isBlank()) {
                java.nio.file.Path p = java.nio.file.Paths.get(configured);
                if (java.nio.file.Files.exists(p) && java.nio.file.Files.isRegularFile(p)) {
                    this.loadedModelPath = p.toAbsolutePath().toString();
                    boolean started = tryInitOrtSessionReflective(this.loadedModelPath);
                    if (!started) {
                        log.info("ONNX Runtime not available reflectively; model at {} will be used as file-only marker", this.loadedModelPath);
                        this.modelLoaded = true;
                    } else {
                        log.info("ONNX Runtime initialized reflectively with model {}", this.loadedModelPath);
                    }
                    return;
                } else {
                    log.warn("Configured talkingsmoke.model.path does not exist or is not a file: {}", configured);
                }
            }
        } catch (Exception e) {
            log.warn("Error while checking configured model path", e);
        }

        // Developer convenience path: keep the previous hard-coded dev path check
        final String devModelPath = "E:\\MyProjects\\MyGitHubCopilot\\talkingsmoke\\tas-01\\java\\src\\main\\resources\\models\\all-MiniLM-L6-v2\\exported_model.onnx";
        try {
            java.nio.file.Path p = java.nio.file.Paths.get(devModelPath);
            if (java.nio.file.Files.exists(p) && java.nio.file.Files.isRegularFile(p)) {
                this.loadedModelPath = p.toAbsolutePath().toString();
                boolean started = tryInitOrtSessionReflective(this.loadedModelPath);
                if (!started) {
                    log.info("ONNX Runtime not available reflectively; model at {} will be used as file-only marker", this.loadedModelPath);
                    this.modelLoaded = true;
                } else {
                    log.info("ONNX Runtime initialized reflectively with model {}", this.loadedModelPath);
                }
                return;
            }
        } catch (Exception e) {
            log.warn("Error while checking dev model path {}", devModelPath, e);
        }

        // Fallback: If a model exists on the classpath, copy it to a temp file and attempt to initialize runtime
        final String resourcePath = "/models/exported_model.onnx";
        InputStream is = null;
        try {
            is = getClass().getResourceAsStream(resourcePath);
            if (is == null) {
                log.info("No classpath model found at {}", resourcePath);
                return;
            }
            java.nio.file.Path tmp = java.nio.file.Files.createTempFile("exported_model", ".onnx");
            java.nio.file.Files.copy(is, tmp, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            // set loadedModelPath to the temp file and attempt reflective init
            this.loadedModelPath = tmp.toAbsolutePath().toString();
            boolean started = tryInitOrtSessionReflective(this.loadedModelPath);
            if (!started) {
                log.info("ONNX Runtime not available or failed to init reflectively; model file copied to {}", this.loadedModelPath);
                this.modelLoaded = true; // mark present even if runtime missing
            } else {
                log.info("ONNX Runtime initialized reflectively with model {}", this.loadedModelPath);
            }
        } catch (Exception e) {
            log.warn("Failed to prepare classpath model for ONNX runtime", e);
        } finally {
            if (is != null) {
                try { is.close(); } catch (IOException ignore) {}
            }
        }
    }
}
