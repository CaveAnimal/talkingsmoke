"""
ONNX Model Exporter
Converts trained PyTorch models to ONNX format.
"""

import torch
import onnx
from model_trainer import SimpleClassifier
import os


def load_pytorch_model(checkpoint_path: str) -> torch.nn.Module:
    model = SimpleClassifier()
    model.load_state_dict(torch.load(checkpoint_path))
    model.eval()
    return model


def create_dummy_input(batch_size: int = 1) -> torch.Tensor:
    return torch.randn(batch_size, 10)


def export_to_onnx(model: torch.nn.Module, dummy_input: torch.Tensor, output_path: str) -> None:
    model.eval()
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    torch.onnx.export(
        model,
        dummy_input,
        output_path,
        export_params=True,
        opset_version=11,
        do_constant_folding=True,
        input_names=['input'],
        output_names=['output'],
        dynamic_axes={'input': {0: 'batch_size'}, 'output': {0: 'batch_size'}}
    )
    # Validate with onnx
    onnx_model = onnx.load(output_path)
    onnx.checker.check_model(onnx_model)
    print(f"Model exported and validated to {output_path}")


if __name__ == "__main__":
    ckpt = os.path.join(os.path.dirname(__file__), "models", "checkpoint.pth")
    if not os.path.exists(ckpt):
        print("Checkpoint not found. Run model_trainer.py first to create checkpoint.")
    else:
        model = load_pytorch_model(ckpt)
        dummy = create_dummy_input()
        out = os.path.join(os.path.dirname(__file__), "models", "exported_model.onnx")
        export_to_onnx(model, dummy, out)
