# TalkingSmoke PRD - ONNX Runtime Integration Project

## 🚀 Project Overview

This project demonstrates seamless integration between Python machine learning models and Java web applications using ONNX Runtime. The implementation consists of two main components: a Python model preparation pipeline and a Java Spring Boot web application for inference serving.

## 📋 Table of Contents

- [Project Structure](#project-structure)
- [Phase 1 Goals & Status](#phase-1-goals--status)
- [Prerequisites](#prerequisites)
- [Python Component Setup](#python-component-setup)
- [Java Component Setup](#java-component-setup)
- [Development Workflow](#development-workflow)
- [API Documentation](#api-documentation)
- [Testing & Validation](#testing--validation)
- [Troubleshooting](#troubleshooting)
- [Performance Metrics](#performance-metrics)
- [Contributing](#contributing)

## 📁 Project Structure

```
talkingsmoke-pr/
├── python/
│   ├── model_trainer.py          # PyTorch model definition and training
│   ├── onnx_exporter.py          # ONNX model export utilities
│   ├── model_validator.py        # ONNX model validation
│   ├── requirements.txt          # Python dependencies
│   └── models/
│       └── exported_model.onnx   # Generated ONNX model
├── java/
│   ├── pom.xml                   # Maven configuration
│   ├── src/main/java/
│   │   └── com/talkingsmoke/
│   │       ├── TalkingSmokeApplication.java
│   │       ├── controller/
│   │       │   └── InferenceController.java
│   │       ├── service/
│   │       │   └── ONNXInferenceService.java
│   │       └── model/
│   │           └── InferenceRequest.java
│   └── src/main/resources/
│       ├── application.yml
│       └── models/
│           └── exported_model.onnx  # Copied from Python output
└── README.md
```

## 🎯 Phase 1 Goals & Status

### 1. Model Preparation (Python-side)
- ✅ **Export PyTorch model to ONNX format**
  - Implementation: Simple feedforward classifier
  - Input shape: 1x10 fixed-size tensor
  - Output: Single classification score
- ✅ **Validate ONNX model**
  - Using `onnx.checker` for structure validation
  - Using `onnxruntime.InferenceSession` for runtime validation
  - Reproducibility testing across multiple runs

### 2. Java Web App Setup
- ✅ **Spring Boot application created**
  - Maven dependency management
  - ONNX Runtime Java API integration (v1.16.3)
  - Clean project structure with proper separation of concerns

### 3. Model Loading and Inference (Java-side)
- ✅ **ONNX model loading**
  - Runtime model loading from classpath/filesystem
  - Error handling for model loading failures
- ✅ **Tensor preparation and inference**
  - Dynamic input tensor creation
  - Proper memory management
  - Output tensor processing
- ✅ **REST API endpoint**
  - `/infer` endpoint for inference requests
  - JSON request/response handling
  - Comprehensive error responses

### 4. Validation and Diagnostics
- ✅ **Model metadata logging**
  - Input/output names and shapes
  - Model version and provider information
- ✅ **Runtime introspection**
  - Model load time measurement
  - Inference latency tracking
  - Memory usage monitoring
- ✅ **Reproducibility confirmation**
  - Deterministic output validation
  - Cross-platform consistency checks

### 5. Testing First Contact
- ✅ **Local deployment**
  - Spring Boot DevTools integration
  - Hot reload capabilities
- ✅ **Endpoint validation**
  - Successful model loading
  - Inference result generation
  - Comprehensive logging with timestamps

## 🔧 Prerequisites

### Python Environment
- Python 3.10.0
- PyTorch 1.13+
- ONNX 1.12+
- ONNXRuntime 1.16+

### Java Environment
- Java 17+
- Maven 3.8+
- Spring Boot 3.2+

## 🐍 Python Component Setup

### 1. Install Dependencies
```bash
cd python/
pip install -r requirements.txt
```

### 2. Train and Export Model
```bash
python model_trainer.py    # Train the PyTorch model
python onnx_exporter.py    # Export to ONNX format
python model_validator.py # Validate the exported model
```

### 3. Key Python Files

**model_trainer.py**
- Defines a simple feedforward neural network
- Trains on synthetic data for demonstration
- Saves PyTorch model checkpoint

**onnx_exporter.py**
- Loads trained PyTorch model
- Exports to ONNX format with optimization
- Validates export success

**model_validator.py**
- Runs comprehensive ONNX model validation
- Tests inference with sample data
- Generates validation report

## ☕ Java Component Setup

### 1. Build and Run
```bash
cd java/
mvn clean install
mvn spring-boot:run
```

### 2. Maven Dependencies
```xml
<dependency>
    <groupId>com.microsoft.onnxruntime</groupId>
    <artifactId>onnxruntime</artifactId>
    <version>1.16.3</version>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

### 3. Key Java Components

**ONNXInferenceService**
- Manages ONNX model lifecycle
- Handles tensor creation and processing
- Provides thread-safe inference operations

**InferenceController**
- REST API endpoint definitions
- Request validation and response formatting
- Error handling and logging

## 🔄 Development Workflow

### 1. Model Development Cycle
```bash
# Python side - model iteration
python model_trainer.py --epochs 100 --learning-rate 0.01
python onnx_exporter.py --input-model checkpoint.pth --output-model model_v2.onnx
python model_validator.py --model-path model_v2.onnx

# Copy updated model to Java resources
cp python/models/model_v2.onnx java/src/main/resources/models/
```

### 2. Java Development Cycle
```bash
# Compile and test
mvn clean compile
mvn test

# Run with development profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## 📡 API Documentation

### Inference Endpoint

**POST** `/infer`

**Request Body:**
```json
{
  "input": [1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0]
}
```

**Response:**
```json
{
  "prediction": 0.85,
  "confidence": 0.92,
  "inferenceTime": 12,
  "modelVersion": "1.0.0",
  "timestamp": "2024-01-15T10:30:45Z"
}
```

**Health Check Endpoint**

**GET** `/health`

**Response:**
```json
{
  "status": "UP",
  "modelLoaded": true,
  "modelLoadTime": 145,
  "lastInference": "2024-01-15T10:30:45Z"
}
```

## 🧪 Testing & Validation

### Unit Tests
```bash
# Python tests
cd python/
python -m pytest tests/

# Java tests
cd java/
mvn test
```

### Integration Tests
```bash
# Start the application
mvn spring-boot:run

# Run integration tests
curl -X POST http://localhost:8080/infer \
  -H "Content-Type: application/json" \
  -d '{"input": [1,2,3,4,5,6,7,8,9,10]}'
```

### Performance Benchmarks
- Model loading time: < 200ms
- Inference latency: < 50ms (p95)
- Memory usage: < 100MB baseline
- Throughput: > 1000 requests/minute

## 🐛 Troubleshooting

### Common Issues

**ONNX Model Loading Errors**
```
Solution: Verify ONNX model compatibility and file path
Check: Model input/output shapes match application expectations
```

**Memory Issues**
```
Solution: Implement proper tensor disposal and session management
JVM Args: -Xmx2g -XX:+UseG1GC
```

**Native Library Issues**
```
Solution: Ensure ONNX Runtime native libraries are properly included
Check: System architecture compatibility (x64, ARM64)
```

## 📊 Performance Metrics

### Logging Configuration
The application includes comprehensive performance logging:

- **Model Load Time**: Time taken to initialize ONNX session
- **Inference Latency**: End-to-end inference time including preprocessing
- **Memory Usage**: Heap and off-heap memory consumption
- **Error Rates**: Failed inference attempts with categorization

### Monitoring Endpoints
- `/metrics` - Prometheus-compatible metrics
- `/health` - Application health status
- `/info` - Build and version information

## 🤝 Contributing

### Development Guidelines
1. Follow conventional commit messages
2. Ensure all tests pass before submitting PR
3. Update documentation for API changes
4. Benchmark performance impact of changes

### Code Style
- **Python**: Follow PEP 8, use Black formatter
- **Java**: Follow Google Java Style Guide, use Maven Checkstyle

### Pull Request Template
1. Description of changes
2. Performance impact assessment
3. Test coverage report
4. Breaking change notifications

---

## 📝 Changelog

### v1.0.0 (Current)
- ✅ Initial ONNX Runtime integration
- ✅ Basic inference pipeline
- ✅ REST API implementation
- ✅ Comprehensive logging and monitoring

### Planned Features (v1.1.0)
- 🔄 Batch inference support
- 🔄 Model versioning and hot-swapping
- 🔄 Advanced error handling and recovery
- 🔄 Performance optimization and caching

---

*Last Updated: January 2025*
*Project Status: Phase 1 Complete ✅*