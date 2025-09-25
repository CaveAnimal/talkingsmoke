"""
ONNX Model Validator
Loads ONNX model and runs a simple inference to confirm shape and runtime behavior.
"""

import onnx
import onnxruntime as ort
import numpy as np
import os


def validate_onnx_structure(model_path: str) -> bool:
    try:
        onnx_model = onnx.load(model_path)
        onnx.checker.check_model(onnx_model)
        print("ONNX structure OK")
        return True
    except Exception as e:
        print(f"ONNX validation failed: {e}")
        return False


def run_runtime_test(model_path: str) -> bool:
    try:
        sess = ort.InferenceSession(model_path, providers=['CPUExecutionProvider'])
        # prepare dummy input
        inp_name = sess.get_inputs()[0].name
        arr = np.random.randn(1, 10).astype(np.float32)
        out = sess.run(None, {inp_name: arr})
        print("Inference successful. Output shapes:", [o.shape for o in out])
        return True
    except Exception as e:
        print(f"ONNXRuntime inference failed: {e}")
        return False


if __name__ == "__main__":
    model = os.path.join(os.path.dirname(__file__), "models", "exported_model.onnx")
    if not os.path.exists(model):
        print("No exported_model.onnx found in python/models. Run exporter first.")
    else:
        ok = validate_onnx_structure(model)
        if ok:
            run_runtime_test(model)
