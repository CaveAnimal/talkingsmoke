"""
TalkingSmoke Model Trainer
Trains a simple feedforward neural network for ONNX export demonstration.
"""

import torch
import torch.nn as nn
import torch.optim as optim
from typing import Tuple
import os


class SimpleClassifier(nn.Module):
    """Simple feedforward neural network with fixed input size of 10."""

    def __init__(self, input_size: int = 10, hidden_size: int = 20, output_size: int = 1):
        super(SimpleClassifier, self).__init__()
        self.fc1 = nn.Linear(input_size, hidden_size)
        self.relu = nn.ReLU()
        self.fc2 = nn.Linear(hidden_size, output_size)
        self.sigmoid = nn.Sigmoid()

    def forward(self, x: torch.Tensor) -> torch.Tensor:
        x = self.fc1(x)
        x = self.relu(x)
        x = self.fc2(x)
        x = self.sigmoid(x)
        return x


def generate_synthetic_data(num_samples: int = 1000) -> Tuple[torch.Tensor, torch.Tensor]:
    """Generate synthetic binary classification data.

    Returns:
        Tuple of (features, labels) tensors
    """
    torch.manual_seed(42)
    X = torch.randn(num_samples, 10)
    y = (X[:, :5].sum(dim=1) > X[:, 5:].sum(dim=1)).float().unsqueeze(1)
    return X, y


def train_model(model: nn.Module, X: torch.Tensor, y: torch.Tensor, epochs: int = 100) -> nn.Module:
    """Train the model with binary cross entropy loss."""
    criterion = nn.BCELoss()
    optimizer = optim.Adam(model.parameters(), lr=0.01)

    model.train()
    for epoch in range(epochs):
        outputs = model(X)
        loss = criterion(outputs, y)
        optimizer.zero_grad()
        loss.backward()
        optimizer.step()
        if (epoch + 1) % 20 == 0:
            print(f'Epoch [{epoch+1}/{epochs}], Loss: {loss.item():.4f}')
    return model


def save_checkpoint(model: nn.Module, filepath: str) -> None:
    os.makedirs(os.path.dirname(filepath), exist_ok=True)
    torch.save(model.state_dict(), filepath)


if __name__ == "__main__":
    X, y = generate_synthetic_data(200)
    model = SimpleClassifier()
    trained = train_model(model, X, y, epochs=10)
    save_checkpoint(trained, os.path.join("..", "python", "models", "checkpoint.pth"))
    print("Training complete. Checkpoint saved.")
