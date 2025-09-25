import unittest
import torch
from python.model_trainer import SimpleClassifier, generate_synthetic_data, train_model


class TrainerTests(unittest.TestCase):
    def test_generate_data(self):
        X, y = generate_synthetic_data(50)
        self.assertEqual(X.shape, (50, 10))
        self.assertEqual(y.shape, (50, 1))

    def test_train_small(self):
        X, y = generate_synthetic_data(100)
        model = SimpleClassifier()
        trained = train_model(model, X, y, epochs=5)
        self.assertIsNotNone(trained)


if __name__ == '__main__':
    unittest.main()
