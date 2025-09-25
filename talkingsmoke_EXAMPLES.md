# TalkingSmoke EXAMPLES - Code Templates and Examples

## 📋 Overview

This document provides concrete code examples for each task step that benefits from a template or example.

---

## 🐍 PYTHON EXAMPLES

### T1.1.2 - Project Directory Structure
```
talkingsmoke-pr/
python/
├── model_trainer.py
├── onnx_exporter.py
├── model_validator.py
├── requirements.txt
├── models/
│   └── (generated .onnx files will go here)
└── tests/
    ├── __init__.py
    ├── test_trainer.py
    ├── test_exporter.py
    └── test_validator.py
```

### T1.1.4 - requirements.txt Template
```
torch==2.8.0
onnx==1.19.0
onnxruntime==1.23.0
numpy==2.2.6
pytest==8.4.2
```

### T1.2.1 - model_trainer.py File Structure Template
```python
"""
TalkingSmoke Model Trainer
Trains a simple feedforward neural network for ONNX export demonstration.
"""

import torch
import torch.nn as nn
import torch.optim as optim
import numpy as np
from typing import Tuple
import os

class SimpleClassifier(nn.Module):
    """Simple feedforward neural network for demonstration."""
    # TODO: Implement in T1.2.2
    pass

def generate_synthetic_data(num_samples: int = 1000) -> Tuple[torch.Tensor, torch.Tensor]:
    """Generate synthetic training data."""
    # TODO: Implement in T1.2.3
    pass

def train_model(model: nn.Module, X: torch.Tensor, y: torch.Tensor, epochs: int = 100) -> nn.Module:
    """Train the model with given data."""
    # TODO: Implement in T1.2.4
    pass

def save_checkpoint(model: nn.Module, filepath: str) -> None:
    """Save model checkpoint."""
    # TODO: Implement in T1.2.5
    pass

if __name__ == "__main__":
    # Main execution logic will go here
    pass
```

### T1.2.2 - SimpleClassifier Class Implementation
```python
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
```

### T1.2.3 - Synthetic Data Generation Function
```python
def generate_synthetic_data(num_samples: int = 1000) -> Tuple[torch.Tensor, torch.Tensor]:
    """
    Generate synthetic binary classification data.
    
    Args:
        num_samples: Number of samples to generate
        
    Returns:
        Tuple of (features, labels) tensors
    """
    # Set random seed for reproducibility
    torch.manual_seed(42)
    
    # Generate random features (10 dimensions)
    X = torch.randn(num_samples, 10)
    
    # Create simple linear decision boundary
    # y = 1 if sum of first 5 features > sum of last 5 features, else 0
    y = (X[:, :5].sum(dim=1) > X[:, 5:].sum(dim=1)).float().unsqueeze(1)
    
    return X, y
```

### T1.2.4 - Training Loop Implementation
```python
def train_model(model: nn.Module, X: torch.Tensor, y: torch.Tensor, epochs: int = 100) -> nn.Module:
    """
    Train the model with binary cross entropy loss.
    
    Args:
        model: PyTorch model to train
        X: Feature tensor
        y: Label tensor
        epochs: Number of training epochs
        
    Returns:
        Trained model
    """
    criterion = nn.BCELoss()
    optimizer = optim.Adam(model.parameters(), lr=0.01)
    
    model.train()
    for epoch in range(epochs):
        # Forward pass
        outputs = model(X)
        loss = criterion(outputs, y)
        
        # Backward pass and optimization
        optimizer.zero_grad()
        loss.backward()
        optimizer.step()
        
        # Print progress every 20 epochs
        if (epoch + 1) % 20 == 0:
            print(f'Epoch [{epoch+1}/{epochs}], Loss: {loss.item():.4f}')
    
    return model
```

### T1.3.1 - onnx_exporter.py File Structure
```python
"""
ONNX Model Exporter
Converts trained PyTorch models to ONNX format.
"""

import torch
import onnx
from model_trainer import SimpleClassifier
import os

def load_pytorch_model(checkpoint_path: str) -> torch.nn.Module:
    """Load trained PyTorch model from checkpoint."""
    # TODO: Implement in T1.3.2
    pass

def create_dummy_input() -> torch.Tensor:
    """Create dummy input tensor for ONNX export."""
    # TODO: Implement in T1.3.3
    pass

def export_to_onnx(model: torch.nn.Module, dummy_input: torch.Tensor, output_path: str) -> None:
    """Export PyTorch model to ONNX format."""
    # TODO: Implement in T1.3.4
    pass

if __name__ == "__main__":
    # Main execution logic
    pass
```

### T1.3.4 - ONNX Export Function
```python
def export_to_onnx(model: torch.nn.Module, dummy_input: torch.Tensor, output_path: str) -> None:
    """
    Export PyTorch model to ONNX format.
    
    Args:
        model: Trained PyTorch model
        dummy_input: Sample input tensor
        output_path: Path to save ONNX model
    """
    model.eval()
    
    # Create output directory if it doesn't exist
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    
    # Export model
    torch.onnx.export(
        model,                      # Model to export
        dummy_input,                # Sample input
        output_path,                # Output file path
        export_params=True,         # Store trained parameters
        opset_version=11,          # ONNX version
        do_constant_folding=True,   # Optimize constant folding
        input_names=['input'],      # Input tensor name
        output_names=['output'],    # Output tensor name
        dynamic_axes={'input': {0: 'batch_size'},    # Variable batch size
                     'output': {0: 'batch_size'}}
    )
    
    print(f"Model exported to {output_path}")
```

### T1.4.2 - ONNX Structure Validation
```python
def validate_onnx_structure(model_path: str) -> bool:
    """
    Validate ONNX model structure using onnx.checker.
    
    Args:
        model_path: Path to ONNX model file
        
    Returns:
        True if validation passes, False otherwise
    """
    try:
        # Load ONNX model
        onnx_model = onnx.load(model_path)
        
        # Check model structure
        onnx.checker.check_model(onnx_model)
        
        # Print model info
        print("=== ONNX Model Information ===")
        print(f"Model inputs: {[input.name for input in onnx_model.graph.input]}")
        print(f"Model outputs: {[output.name for output in onnx_model.graph.output]}")
        print(f"Model version: {onnx_model.model_version}")
        
        return True
        
    except Exception as e:
        print(f"ONNX validation failed: {e}")
        return False
```

---

## ☕ JAVA EXAMPLES

### T2.1.2 - pom.xml Template
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>
    
    <groupId>com.talkingsmoke</groupId>
    <artifactId>onnx-inference-service</artifactId>
    <version>1.0.0</version>
    <name>TalkingSmoke ONNX Inference Service</name>
    <description>Spring Boot application for ONNX model inference</description>
    
    <properties>
        <java.version>17</java.version>
    </properties>
    
    <dependencies>
        <!-- Spring Boot Web Starter -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <!-- ONNX Runtime Java API -->
        <dependency>
            <groupId>com.microsoft.onnxruntime</groupId>
            <artifactId>onnxruntime</artifactId>
            <version>1.16.3</version>
        </dependency>
        
        <!-- Testing Dependencies -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

### T2.1.5 - application.yml Configuration
```yaml
server:
  port: 8080

spring:
  application:
    name: talkingsmoke-inference-service

# ONNX Model Configuration
onnx:
  model:
    path: classpath:models/exported_model.onnx
    input-name: input
    output-name: output
    input-size: 10

# Logging Configuration
logging:
  level:
    com.talkingsmoke: DEBUG
    ai.onnxruntime: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"

# Management endpoints
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

### T2.2.1 - Main Application Class
```java
package com.talkingsmoke;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * TalkingSmoke ONNX Inference Service
 * Main Spring Boot application class
 */
@SpringBootApplication
@EnableConfigurationProperties
public class TalkingSmokeApplication {

    public static void main(String[] args) {
        SpringApplication.run(TalkingSmokeApplication.class, args);
    }
}
```

### T2.2.4 - InferenceRequest DTO
```java
package com.talkingsmoke.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for inference endpoint
 */
public class InferenceRequest {
    
    @JsonProperty("input")
    @NotNull(message = "Input array cannot be null")
    @Size(min = 10, max = 10, message = "Input array must contain exactly 10 elements")
    private float[] input;
    
    // Default constructor for Jackson
    public InferenceRequest() {}
    
    public InferenceRequest(float[] input) {
        this.input = input;
    }
    
    public float[] getInput() {
        return input;
    }
    
    public void setInput(float[] input) {
        this.input = input;
    }
}
```

### T2.2.4 - InferenceResponse DTO
```java
package com.talkingsmoke.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * Response DTO for inference endpoint
 */
public class InferenceResponse {
    
    @JsonProperty("prediction")
    private float prediction;
    
    @JsonProperty("confidence")
    private float confidence;
    
    @JsonProperty("inferenceTime")
    private long inferenceTimeMs;
    
    @JsonProperty("modelVersion")
    private String modelVersion;
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
    
    // Default constructor
    public InferenceResponse() {
        this.timestamp = LocalDateTime.now();
        this.modelVersion = "1.0.0";
    }
    
    // Constructor with prediction
    public InferenceResponse(float prediction, long inferenceTimeMs) {
        this();
        this.prediction = prediction;
        this.confidence = Math.abs(prediction - 0.5f) * 2; // Simple confidence calculation
        this.inferenceTimeMs = inferenceTimeMs;
    }
    
    // Getters and setters
    public float getPrediction() { return prediction; }
    public void setPrediction(float prediction) { this.prediction = prediction; }
    
    public float getConfidence() { return confidence; }
    public void setConfidence(float confidence) { this.confidence = confidence; }
    
    public long getInferenceTimeMs() { return inferenceTimeMs; }
    public void setInferenceTimeMs(long inferenceTimeMs) { this.inferenceTimeMs = inferenceTimeMs; }
    
    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
```

### T2.3.1 - ONNX Model Loading Method
```java
/**
 * Load ONNX model and create inference session
 */
@PostConstruct
public void loadModel() {
    try {
        long startTime = System.currentTimeMillis();
        
        // Get model file from resources
        ClassPathResource resource = new ClassPathResource(modelPath);
        InputStream modelStream = resource.getInputStream();
        
        // Read model bytes
        byte[] modelBytes = modelStream.readAllBytes();
        modelStream.close();
        
        // Create ONNX Runtime session
        OrtEnvironment env = OrtEnvironment.getEnvironment();
        SessionOptions sessionOptions = new SessionOptions();
        this.ortSession = env.createSession(modelBytes, sessionOptions);
        
        long loadTime = System.currentTimeMillis() - startTime;
        
        // Log model information
        logger.info("ONNX model loaded successfully");
        logger.info("Model load time: {} ms", loadTime);
        logger.info("Input names: {}", ortSession.getInputNames());
        logger.info("Output names: {}", ortSession.getOutputNames());
        
        this.modelLoaded = true;
        this.modelLoadTime = loadTime;
        
    } catch (Exception e) {
        logger.error("Failed to load ONNX model: {}", e.getMessage(), e);
        throw new RuntimeException("Model loading failed", e);
    }
}
```

### T2.3.3 - Input Tensor Preparation Method
```java
/**
 * Create ONNX tensor from input array
 */
private OnnxTensor createInputTensor(float[] inputData) throws OrtException {
    // Validate input size
    if (inputData.length != expectedInputSize) {
        throw new IllegalArgumentException(
            String.format("Expected input size %d, got %d", expectedInputSize, inputData.length)
        );
    }
    
    // Create tensor shape [1, 10] (batch size 1, features 10)
    long[] shape = {1, inputData.length};
    
    // Convert float[] to FloatBuffer
    FloatBuffer buffer = FloatBuffer.wrap(inputData);
    
    // Create ONNX tensor
    OrtEnvironment env = OrtEnvironment.getEnvironment();
    return OnnxTensor.createTensor(env, buffer, shape);
}
```

### T2.4.3 - POST /infer Endpoint Implementation
```java
@PostMapping("/infer")
@ResponseBody
public ResponseEntity<?> performInference(@Valid @RequestBody InferenceRequest request) {
    logger.info("Inference request received with input size: {}", request.getInput().length);
    
    if (!modelLoaded) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", "Model not loaded"));
    }
    
    try {
        long startTime = System.currentTimeMillis();
        
        // Perform inference
        float prediction = inferenceService.predict(request.getInput());
        
        long inferenceTime = System.currentTimeMillis() - startTime;
        
        // Create response
        InferenceResponse response = new InferenceResponse(prediction, inferenceTime);
        
        logger.info("Inference completed in {} ms, prediction: {}", inferenceTime, prediction);
        
        return ResponseEntity.ok(response);
        
    } catch (IllegalArgumentException e) {
        logger.warn("Invalid input: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(Map.of("error", "Invalid input: " + e.getMessage()));
                
    } catch (Exception e) {
        logger.error("Inference failed: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Inference failed: " + e.getMessage()));
    }
}
```

---

## 🧪 TESTING EXAMPLES

### T3.2.2 - Health Endpoint Test (curl)
```bash
# Test health endpoint
curl -X GET http://localhost:8080/health \
  -H "Accept: application/json" \
  -w "\n"

# Expected response:
# {
#   "status": "UP",
#   "modelLoaded": true,
#   "modelLoadTime": 145,
#   "lastInference": "2024-01-15T10:30:45Z"
# }
```

### T3.2.3 - Inference Endpoint Test (curl)
```bash
# Test inference endpoint with sample data
curl -X POST http://localhost:8080/infer \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "input": [1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0]
  }' \
  -w "\n"

# Expected response format:
# {
#   "prediction": 0.85,
#   "confidence": 0.92,
#   "inferenceTime": 12,
#   "modelVersion": "1.0.0",
#   "timestamp": "2024-01-15T10:30:45"
# }
```

### T3.2.5 - Error Case Testing
```bash
# Test with wrong input size (should return 400)
curl -X POST http://localhost:8080/infer \
  -H "Content-Type: application/json" \
  -d '{
    "input": [1.0, 2.0, 3.0]
  }' \
  -w "\nHTTP Status: %{http_code}\n"

# Test with missing input field (should return 400)
curl -X POST http://localhost:8080/infer \
  -H "Content-Type: application/json" \
  -d '{}' \
  -w "\nHTTP Status: %{http_code}\n"

# Test with malformed JSON (should return 400)
curl -X POST http://localhost:8080/infer \
  -H "Content-Type: application/json" \
  -d '{invalid json}' \
  -w "\nHTTP Status: %{http_code}\n"
```

### T3.2.6 - Performance Testing Script
```bash
#!/bin/bash
# performance_test.sh - Test multiple requests

echo "Starting performance test..."
start_time=$(date +%s)

for i in {1..100}; do
  curl -s -X POST http://localhost:8080/infer \
    -H "Content-Type: application/json" \
    -d '{
      "input": [1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0]
    }' > /dev/null
  
  if [ $((i % 10)) -eq 0 ]; then
    echo "Completed $i requests"
  fi
done

end_time=$(date +%s)
total_time=$((end_time - start_time))
echo "100 requests completed in ${total_time} seconds"
echo "Average: $((total_time * 10)) ms per request"
```

---

## 📊 LOGGING AND MONITORING EXAMPLES

### T3.3.1 - Structured Logging Example
```java
/**
 * Enhanced logging for model loading with structured information
 */
@PostConstruct
public void loadModel() {
    MDC.put("operation", "model_loading");
    MDC.put("modelPath", modelPath);
    
    try {
        long startTime = System.currentTimeMillis();
        
        logger.info("Starting ONNX model loading process");
        
        // Load model (existing code)...
        
        long loadTime = System.currentTimeMillis() - startTime;
        
        // Structured logging with model metadata
        logger.info("Model loading completed successfully - " +
                   "loadTime={}ms, inputNames={}, outputNames={}, " +
                   "inputShapes={}", 
                   loadTime, 
                   ortSession.getInputNames(),
                   ortSession.getOutputNames(),
                   getInputShapes());
                   
        MDC.put("modelLoadTime", String.valueOf(loadTime));
        MDC.put("modelLoaded", "true");
        
    } catch (Exception e) {
        logger.error("Model loading failed - error={}, cause={}", 
                    e.getMessage(), e.getCause());
        MDC.put("modelLoaded", "false");
        throw new RuntimeException("Model loading failed", e);
    } finally {
        MDC.clear();
    }
}
```

### T3.3.2 - Inference Timing Implementation
```java
/**
 * Detailed timing for inference operations
 */
public float predict(float[] inputData) throws OrtException {
    long totalStart = System.nanoTime();
    
    // Timing: Tensor creation
    long tensorStart = System.nanoTime();
    OnnxTensor inputTensor = createInputTensor(inputData);
    long tensorTime = System.nanoTime() - tensorStart;
    
    // Timing: Inference execution
    long inferenceStart = System.nanoTime();
    OrtSession.Result result = ortSession.run(
        Collections.singletonMap(inputName, inputTensor)
    );
    long inferenceTime = System.nanoTime() - inferenceStart;
    
    // Timing: Result processing
    long processingStart = System.nanoTime();
    float[][] output = (float[][]) result.get(0).getValue();
    float prediction = output[0][0];
    long processingTime = System.nanoTime() - processingStart;
    
    long totalTime = System.nanoTime() - totalStart;
    
    // Log detailed timing information
    logger.debug("Inference timing breakdown - " +
                "total={}μs, tensor={}μs, inference={}μs, processing={}μs",
                totalTime / 1000, tensorTime / 1000, 
                inferenceTime / 1000, processingTime / 1000);
    
    // Cleanup
    inputTensor.close();
    result.close();
    
    return prediction;
}
```

---

## 🧪 UNIT TEST EXAMPLES

### T4.1.2 - Python Model Training Unit Test
```python
# tests/test_trainer.py
import pytest
import torch
import sys
import os

# Add parent directory to path for imports
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from model_trainer import SimpleClassifier, generate_synthetic_data, train_model

class TestModelTrainer:
    
    def test_simple_classifier_creation(self):
        """Test that SimpleClassifier can be created with correct architecture."""
        model = SimpleClassifier(input_size=10, hidden_size=20, output_size=1)
        
        assert isinstance(model, torch.nn.Module)
        assert model.fc1.in_features == 10
        assert model.fc1.out_features == 20
        assert model.fc2.in_features == 20
        assert model.fc2.out_features == 1
    
    def test_forward_pass(self):
        """Test forward pass produces correct output shape."""
        model = SimpleClassifier()
        input_tensor = torch.randn(5, 10)  # Batch of 5 samples
        
        output = model(input_tensor)
        
        assert output.shape == (5, 1)
        assert torch.all(output >= 0) and torch.all(output <= 1)  # Sigmoid output
    
    def test_synthetic_data_generation(self):
        """Test synthetic data generation produces correct shapes and types."""
        X, y = generate_synthetic_data(num_samples=100)
        
        assert X.shape == (100, 10)
        assert y.shape == (100, 1)
        assert X.dtype == torch.float32
        assert y.dtype == torch.float32
        assert torch.all((y == 0) | (y == 1))  # Binary labels
    
    def test_model_training_reduces_loss(self):
        """Test that training actually reduces loss."""
        model = SimpleClassifier()
        X, y = generate_synthetic_data(num_samples=200)
        
        # Calculate initial loss
        initial_output = model(X)
        criterion = torch.nn.BCELoss()
        initial_loss = criterion(initial_output, y).item()
        
        # Train model
        trained_model = train_model(model, X, y, epochs=20)
        
        # Calculate final loss
        final_output = trained_model(X)
        final_loss = criterion(final_output, y).item()
        
        assert final_loss < initial_loss, "Training should reduce loss"
```

### T4.1.5 - Java ONNXInferenceService Unit Test
```java
// src/test/java/com/talkingsmoke/service/ONNXInferenceServiceTest.java
package com.talkingsmoke.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "onnx.model.path=classpath:test-models/simple_test_model.onnx",
    "onnx.model.input-size=10"
})
class ONNXInferenceServiceTest {
    
    private ONNXInferenceService inferenceService;
    
    @BeforeEach
    void setUp() {
        inferenceService = new ONNXInferenceService();
        // Note: In real test, you'd use @MockBean or create a test model
    }
    
    @Test
    @DisplayName("Should validate input array size correctly")
    void testInputValidation() {
        // Test correct input size
        float[] validInput = new float[10];
        assertDoesNotThrow(() -> {
            // This would call internal validation method
        });
        
        // Test incorrect input size
        float[] invalidInput = new float[5];
        assertThrows(IllegalArgumentException.class, () -> {
            inferenceService.predict(invalidInput);
        });
    }
    
    @Test
    @DisplayName("Should produce consistent predictions for same input")
    void testPredictionConsistency() throws Exception {
        float[] testInput = {1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 
                            6.0f, 7.0f, 8.0f, 9.0f, 10.0f};
        
        // Make multiple predictions with same input
        float prediction1 = inferenceService.predict(testInput);
        float prediction2 = inferenceService.predict(testInput);
        
        assertEquals(prediction1, prediction2, 0.0001f, 
                    "Same input should produce same prediction");
    }
    
    @Test
    @DisplayName("Should produce predictions in valid range")
    void testPredictionRange() throws Exception {
        float[] testInput = {1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 
                            6.0f, 7.0f, 8.0f, 9.0f, 10.0f};
        
        float prediction = inferenceService.predict(testInput);
        
        assertTrue(prediction >= 0.0f && prediction <= 1.0f, 
                  "Prediction should be between 0 and 1");
    }
}
```

### T4.1.6 - Integration Test for REST Endpoints
```java
// src/test/java/com/talkingsmoke/controller/InferenceControllerIntegrationTest.java
package com.talkingsmoke.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.talkingsmoke.model.InferenceRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureWebMvc
class InferenceControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("UP")))
                .andExpect(jsonPath("$.modelLoaded", is(true)));
    }
    
    @Test
    void testInferenceEndpointSuccess() throws Exception {
        InferenceRequest request = new InferenceRequest(
            new float[]{1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 7.0f, 8.0f, 9.0f, 10.0f}
        );
        
        mockMvc.perform(post("/infer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.prediction", isA(Number.class)))
                .andExpect(jsonPath("$.confidence", isA(Number.class)))
                .andExpect(jsonPath("$.inferenceTime", isA(Number.class)))
                .andExpect(jsonPath("$.modelVersion", is("1.0.0")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }
    
    @Test
    void testInferenceEndpointInvalidInput() throws Exception {
        InferenceRequest request = new InferenceRequest(
            new float[]{1.0f, 2.0f, 3.0f}  // Wrong size
        );
        
        mockMvc.perform(post("/infer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Invalid input")));
    }
}
```

---

## 🔧 ERROR HANDLING EXAMPLES

### T4.2.1 - Missing Model File Error Handling
```java
/**
 * Enhanced model loading with comprehensive error handling
 */
@PostConstruct
public void loadModel() {
    try {
        logger.info("Attempting to load ONNX model from: {}", modelPath);
        
        // Check if model file exists
        ClassPathResource resource = new ClassPathResource(modelPath);
        if (!resource.exists()) {
            throw new ModelLoadingException(
                String.format("Model file not found at path: %s", modelPath)
            );
        }
        
        // Check file readability
        if (!resource.isReadable()) {
            throw new ModelLoadingException(
                String.format("Model file is not readable: %s", modelPath)
            );
        }
        
        // Load model bytes with size validation
        InputStream modelStream = resource.getInputStream();
        byte[] modelBytes = modelStream.readAllBytes();
        modelStream.close();
        
        if (modelBytes.length == 0) {
            throw new ModelLoadingException("Model file is empty");
        }
        
        logger.info("Model file loaded, size: {} bytes", modelBytes.length);
        
        // Create ONNX Runtime session with error handling
        OrtEnvironment env = OrtEnvironment.getEnvironment();
        SessionOptions sessionOptions = new SessionOptions();
        
        try {
            this.ortSession = env.createSession(modelBytes, sessionOptions);
        } catch (ai.onnxruntime.OrtException e) {
            throw new ModelLoadingException(
                "Failed to create ONNX Runtime session: " + e.getMessage(), e
            );
        }
        
        this.modelLoaded = true;
        logger.info("ONNX model loaded successfully");
        
    } catch (IOException e) {
        logger.error("I/O error while loading model: {}", e.getMessage());
        throw new ModelLoadingException("Failed to read model file", e);
    } catch (Exception e) {
        logger.error("Unexpected error during model loading: {}", e.getMessage(), e);
        throw new ModelLoadingException("Model loading failed", e);
    }
}

/**
 * Custom exception for model loading errors
 */
public class ModelLoadingException extends RuntimeException {
    public ModelLoadingException(String message) {
        super(message);
    }
    
    public ModelLoadingException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

### T4.2.4 - User-Friendly Error Messages
```java
/**
 * Global exception handler for user-friendly error responses
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex) {
        
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());
        
        ErrorResponse errorResponse = new ErrorResponse(
            "VALIDATION_ERROR",
            "Invalid input data",
            errors
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex) {
        
        logger.warn("Invalid argument: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            "INVALID_INPUT",
            "The provided input is invalid: " + ex.getMessage(),
            null
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    @ExceptionHandler(ModelLoadingException.class)
    public ResponseEntity<ErrorResponse> handleModelLoadingError(
            ModelLoadingException ex) {
        
        logger.error("Model loading error: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            "MODEL_UNAVAILABLE",
            "The inference model is currently unavailable. Please try again later.",
            null
        );
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(errorResponse);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericError(Exception ex) {
        
        logger.error("Unexpected error: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = new ErrorResponse(
            "INTERNAL_ERROR",
            "An unexpected error occurred. Please contact support if the problem persists.",
            null
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }
}

/**
 * Standard error response format
 */
public class ErrorResponse {
    private String code;
    private String message;
    private List<String> details;
    private LocalDateTime timestamp;
    
    public ErrorResponse(String code, String message, List<String> details) {
        this.code = code;
        this.message = message;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }
    
    // Getters and setters...
}
```

---

## 📝 DOCUMENTATION EXAMPLES

### T4.3.1 - Python Function Docstrings
```python
def train_model(model: nn.Module, X: torch.Tensor, y: torch.Tensor, 
                epochs: int = 100, learning_rate: float = 0.01) -> nn.Module:
    """
    Train a PyTorch model using binary cross entropy loss and Adam optimizer.
    
    This function implements a standard supervised learning training loop for
    binary classification tasks. The model is trained in-place and returned
    for convenience.
    
    Args:
        model (nn.Module): PyTorch model to train. Must output values in [0,1] range.
        X (torch.Tensor): Feature tensor of shape (num_samples, num_features).
        y (torch.Tensor): Label tensor of shape (num_samples, 1) with binary values.
        epochs (int, optional): Number of training epochs. Defaults to 100.
        learning_rate (float, optional): Learning rate for Adam optimizer. Defaults to 0.01.
    
    Returns:
        nn.Module: The trained model (same object as input, modified in-place).
    
    Raises:
        ValueError: If input tensors have incompatible shapes.
        RuntimeError: If model training fails due to numerical issues.
    
    Example:
        >>> model = SimpleClassifier(input_size=10)
        >>> X, y = generate_synthetic_data(num_samples=1000)
        >>> trained_model = train_model(model, X, y, epochs=50)
        >>> print(f"Training completed with final loss: {final_loss:.4f}")
    
    Note:
        The function prints training progress every 20 epochs. For silent
        training, capture stdout or modify the logging behavior.
    """
    # Implementation continues...
```

### T4.3.2 - Java Method Javadoc
```java
/**
 * Performs inference on input data using the loaded ONNX model.
 * 
 * <p>This method handles the complete inference pipeline including input validation,
 * tensor creation, model execution, and result processing. All operations are
 * thread-safe and include proper resource cleanup.</p>
 * 
 * <p>The method performs the following steps:
 * <ol>
 *   <li>Validates input array size matches model expectations</li>
 *   <li>Creates ONNX tensor from input data</li>
 *   <li>Executes inference using ONNX Runtime</li>
 *   <li>Processes and returns the prediction result</li>
 *   <li>Cleans up all temporary resources</li>
 * </ol>
 * 
 * @param inputData float array containing exactly 10 feature values
 * @return prediction value between 0.0 and 1.0 representing the model's output
 * 
 * @throws IllegalArgumentException if inputData is null or has incorrect length
 * @throws OrtException if ONNX Runtime encounters an error during inference
 * @throws RuntimeException if model is not loaded or inference fails unexpectedly
 * 
 * @since 1.0.0
 * 
 * @example
 * <pre>
 * float[] features = {1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 7.0f, 8.0f, 9.0f, 10.0f};
 * float prediction = inferenceService.predict(features);
 * System.out.println("Prediction: " + prediction);
 * </pre>
 * 
 * @see #loadModel() for model initialization
 * @see InferenceRequest for REST API usage
 */
public float predict(float[] inputData) throws OrtException {
    // Implementation continues...
}
```

---

## 📋 QUICK REFERENCE CHEAT SHEET

### Essential Commands
```bash
# Python Environment
python -m venv venv
source venv/bin/activate  # Linux/Mac
venv\Scripts\activate     # Windows
pip install -r requirements.txt

# Java Build
mvn clean compile
mvn spring-boot:run
mvn test

# Testing
curl -X GET http://localhost:8080/health
curl -X POST http://localhost:8080/infer -H "Content-Type: application/json" -d '{"input":[1,2,3,4,5,6,7,8,9,10]}'
```

### Common File Locations
```
python/requirements.txt          # Python dependencies
java/pom.xml                    # Maven dependencies  
java/src/main/resources/application.yml  # Spring configuration
python/models/exported_model.onnx       # Generated ONNX model
java/src/main/resources/models/          # ONNX model for Java app
```

### Key Classes and Functions
```python
# Python
SimpleClassifier()              # Neural network model
generate_synthetic_data()       # Training data creation
train_model()                   # Model training function
export_to_onnx()               # ONNX export function
```

```java
// Java
ONNXInferenceService           # Main inference service
InferenceController            # REST API controller
InferenceRequest/Response      # API DTOs
```

---

*This examples document provides concrete implementations for all major task steps. Refer to the corresponding task ID when you need specific code examples.*