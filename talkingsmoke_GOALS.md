🎯 Phase 1 Goals: ONNX Runtime Integration in Java Web App
1. Model Preparation (Python-side)
- ✅ Export a simple PyTorch model to ONNX format.
- Example: A basic linear regression or feedforward classifier.
- Input shape: fixed-size tensor (e.g., 1x10).
- ✅ Validate the ONNX model using onnx.checker and onnxruntime.InferenceSession.
2. Java Web App Setup
- ✅ Create a minimal Spring Boot or plain Java web application.
- Use Maven for dependency management.
- Include ONNX Runtime Java API:
<dependency>
  <groupId>com.microsoft.onnxruntime</groupId>
  <artifactId>onnxruntime</artifactId>
  <version>1.16.3</version> <!-- or latest -->
</dependency>


3. Model Loading and Inference (Java-side)
- ✅ Load the exported .onnx model using ONNX Runtime in Java.
- ✅ Prepare dummy input tensor matching the model’s expected shape.
- ✅ Run inference and log the output.
- ✅ Wrap this logic in a simple REST endpoint (e.g., /infer) that triggers inference and returns the result.
4. Validation and Diagnostics
- ✅ Log model metadata (input/output names, shapes).
- ✅ Include basic runtime introspection:
- Model load time
- Inference latency
- Input/output tensor shapes
- ✅ Confirm reproducibility: same input yields same output across runs.
5. Testing First Contact
- ✅ Deploy locally and hit the /infer endpoint.
- ✅ Verify that the Java app successfully loads the ONNX model and returns inference results.
- ✅ Log all steps with timestamps and changelog entries for onboarding clarity.
