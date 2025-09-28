"""
ONNX Model Exporter
Converts trained PyTorch models to ONNX format.
"""

import torch
import onnx
from model_trainer import SimpleClassifier
import os


def load_pytorch_model(checkpoint_path: str) -> torch.nn.Module:
    """Load a PyTorch model state dict from `checkpoint_path` and return an eval-mode model.

    Args:
        checkpoint_path: Path to a saved PyTorch state_dict file.

    Returns:
        An instance of `SimpleClassifier` with loaded weights in evaluation mode.
    """
    model = SimpleClassifier()
    model.load_state_dict(torch.load(checkpoint_path))
    model.eval()
    return model


def create_dummy_input(batch_size: int = 1) -> torch.Tensor:
    """Create a dummy input tensor compatible with the SimpleClassifier input.

    Args:
        batch_size: Number of samples in the batch dimension.

    Returns:
        A torch.FloatTensor of shape (batch_size, 10).
    """
    return torch.randn(batch_size, 10)


def export_to_onnx(model: torch.nn.Module, dummy_input: torch.Tensor, output_path: str) -> None:
    """Export a PyTorch model to ONNX format and validate the resulting file.

    This will create parent directories for `output_path` as needed. The function
    will raise if exporting or validation fails.

    Args:
        model: A torch.nn.Module instance in eval mode.
        dummy_input: A tensor representing a valid input to the model.
        output_path: Destination .onnx file path.
    """
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
