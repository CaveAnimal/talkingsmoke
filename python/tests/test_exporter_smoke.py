import importlib
import sys
import os

import pytest

# ensure python package path
ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), '..'))
if ROOT not in sys.path:
    sys.path.insert(0, ROOT)

try:
    from onnx_exporter import create_dummy_input, export_to_onnx
    import torch
    import onnx
    TORCH_AVAILABLE = True
except Exception:
    TORCH_AVAILABLE = False


def test_create_dummy_input_shape():
    dummy = create_dummy_input(batch_size=4)
    assert hasattr(dummy, 'shape')
    assert tuple(dummy.shape) == (4, 10)


@pytest.mark.skipif(not TORCH_AVAILABLE, reason="torch/onnx not available in environment")
def test_export_to_onnx_smoke(tmp_path):
    # This test will attempt an in-memory export; it requires torch and onnx
    model = None
    try:
        from onnx_exporter import load_pytorch_model, SimpleClassifier
        model = SimpleClassifier()
        dummy = create_dummy_input(batch_size=2)
        outp = tmp_path / "smoke_export.onnx"
        export_to_onnx(model, dummy, str(outp))
        assert outp.exists()
        # try loading via onnx
        onnx_model = onnx.load(str(outp))
        onnx.checker.check_model(onnx_model)
    finally:
        # no cleanup needed beyond tmp_path
        pass
