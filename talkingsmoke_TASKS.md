# TalkingSmoke TASKS - Entry Level Developer Guide

## 📋 Overview

This document breaks down the ONNX Runtime integration project into small, manageable tasks for entry-level developers. Each task includes time estimates and progress tracking.

**Total Estimated Time: 16-20 hours**


### Status key (how to mark tasks)

- Use a square bracket placeholder in front of each task id: `[   ]` so team members or agents can update the status.
- Suggested symbols to place inside the brackets (pick one consistent style):
  - `[ ]` or `[   ]` — Not started
  - `[~]` or `[>]` — In progress
  - `[x]` — Completed
  - `[✓]` — Tested / Verified
  - `[!]` — Blocked / Needs attention

Keep status updates short and consistent (prefer single-character markers). When a task is finished and tested, update its bracket to `[✓]`.

---

 ## [X] 🎯 TASK 1: Python Environment Setup and Model Creation
**Estimated Total Time: 4-5 hours**

 ### [X] T1.1 - Environment Setup (Progress: 100%)
**Estimated Time: 45 minutes**

 - [X] **T1.1.1** - Install Python 3.10.0 and verify installation *(5 min)*
 - [X] **T1.1.2** - Create project directory structure *(10 min)*
 - [X] **T1.1.3** - Set up virtual environment *(10 min)*
 - [X] **T1.1.4** - Create and populate requirements.txt file *(10 min)*
 - [X] **T1.1.5** - Install Python dependencies using pip *(10 min)*

 ### [X] T1.2 - PyTorch Model Development (Progress: 100%)
**Estimated Time: 90 minutes**

 - [X] **T1.2.1** - Create model_trainer.py file structure *(15 min)*
 - [X] **T1.2.2** - Define simple feedforward neural network class *(30 min)*
 - [X] **T1.2.3** - Implement synthetic data generation function *(20 min)*
 - [X] **T1.2.4** - Write model training loop with loss calculation *(20 min)*
 - [X] **T1.2.5** - Add model checkpointing functionality *(5 min)*

 ### [X] T1.3 - ONNX Model Export (Progress: 100%)
**Estimated Time: 60 minutes**

 - [X] **T1.3.1** - Create onnx_exporter.py file *(10 min)*
 - [X] **T1.3.2** - Implement PyTorch model loading function *(15 min)*
 - [X] **T1.3.3** - Create dummy input tensor for export *(10 min)*
 - [X] **T1.3.4** - Write ONNX export function with torch.onnx.export *(20 min)*
 - [X] **T1.3.5** - Add export validation and file saving *(5 min)*

 ### [X] T1.4 - Model Validation System (Progress: 100%)
**Estimated Time: 75 minutes**

 - [X] **T1.4.1** - Create model_validator.py file structure *(10 min)*
 - [X] **T1.4.2** - Implement ONNX model structure validation *(20 min)*
 - [X] **T1.4.3** - Create ONNX Runtime inference session *(15 min)*
 - [X] **T1.4.4** - Write inference testing with sample data *(20 min)*
 - [X] **T1.4.5** - Add reproducibility testing across multiple runs *(10 min)*

---

 ## [~] ☕ TASK 2: Java Web Application Setup
**Estimated Total Time: 5-6 hours**

 ### [✓] T2.1 - Maven Project Initialization (Progress: 100%)
**Estimated Time: 60 minutes**

 - [X] **T2.1.1** - Create Maven project directory structure *(10 min)*
 - [X] **T2.1.2** - Write pom.xml with Spring Boot parent *(15 min)*
 - [X] **T2.1.3** - Add ONNX Runtime dependency to pom.xml *(10 min)*
 - [X] **T2.1.4** - Add Spring Boot web starter dependency *(5 min)*
 - [X] **T2.1.5** - Create application.yml configuration file *(10 min)*
 - [X] **T2.1.6** - Test Maven build with mvn clean compile *(10 min)*

### [~] T2.2 - Spring Boot Application Structure (Progress: 92%)
**Estimated Time: 90 minutes**

 - [X] **T2.2.1** - Create main Application class with @SpringBootApplication *(15 min)*
 - [X] **T2.2.2** - Create controller package and InferenceController class *(20 min)*
 - [X] **T2.2.3** - Create service package and ONNXInferenceService class *(20 min)*
 - [X] **T2.2.4** - Create model package with request/response DTOs *(20 min)*
 - [✓] **T2.2.5** - Add basic logging configuration *(10 min)*
 - [~] **T2.2.6** - Test application startup with mvn spring-boot:run *(5 min)*

### [~] T2.3 - ONNX Model Integration (Progress: 60%)
**Estimated Time: 120 minutes**

- [   ] **T2.3.1** - Create model loading method in service class *(30 min)*
- [   ] **T2.3.2** - Implement OrtSession initialization with error handling *(25 min)*
- [   ] **T2.3.3** - Create input tensor preparation method *(25 min)*
- [   ] **T2.3.4** - Write inference execution method *(25 min)*
- [   ] **T2.3.5** - Add output tensor processing and cleanup *(15 min)*

### [~] T2.4 - REST API Implementation (Progress: 60%)
**Estimated Time: 90 minutes**

 - [X] **T2.4.1** - Create InferenceRequest DTO class *(15 min)*
 - [X] **T2.4.2** - Create InferenceResponse DTO class *(15 min)*
 - [X] **T2.4.3** - Implement POST /infer endpoint method *(25 min)*
 - [-] **T2.4.4** - Add request validation and error handling *(20 min)*
 - [X] **T2.4.5** - Implement GET /health endpoint *(10 min)*
- [✓] **T2.4.6** - Add response formatting and JSON serialization *(5 min)*

---

## [   ] 🔧 TASK 3: Integration and Testing
**Estimated Total Time: 3-4 hours**

### [~] T3.1 - Model File Integration (Progress: 33%)
**Estimated Time: 30 minutes**

- [✓] **T3.1.1** - Copy ONNX model from Python to Java resources *(5 min)*
 - [✓] **T3.1.2** - Update application.yml with model file path *(10 min)*
- [✓] **T3.1.3** - Test model loading on application startup *(15 min)*

### [~] T3.2 - End-to-End Testing (Progress: 70%)
**Estimated Time: 90 minutes**

 - [✓] **T3.2.1** - Start Spring Boot application locally *(5 min)*
 - [✓] **T3.2.2** - Test /health endpoint with curl or Postman *(10 min)*
 - [✓] **T3.2.3** - Test /infer endpoint with sample data *(15 min)*
 - [✓] **T3.2.4** - Verify inference results match expected format *(10 min)*
- [   ] **T3.2.5** - Test error cases (invalid input, malformed JSON) *(20 min)*
 - [✓] **T3.2.5** - Test error cases (invalid input, malformed JSON) *(20 min)*
- [   ] **T3.2.6** - Performance testing with multiple requests *(20 min)*
 - [✓] **T3.2.6** - Performance testing with multiple requests *(20 min)*
- [✓] **T3.2.7** - Validate reproducibility across multiple calls *(10 min)*

### [✓] T3.2.0 - Jar-based end-to-end smoke test added (Progress: 100%)
**Estimated Time: 10 minutes**

- [✓] **T3.2.0.1** - Add `JarE2ETest` that starts packaged JAR and calls `/health` (disabled by default; requires RUN_JAR_E2E=true) *(10 min)*

### [✓] T3.3 - Logging and Monitoring Setup (Progress: 40%)
**Estimated Time: 60 minutes**

- [✓] **T3.3.1** - Add structured logging to model loading process *(15 min)*
- [✓] **T3.3.2** - Implement inference timing measurements *(15 min)*
- [✓] **T3.3.3** - Add model metadata logging (input/output shapes) *(15 min)*
- [   ] **T3.3.4** - Create performance metrics collection *(10 min)*
- [   ] **T3.3.5** - Test logging output and format *(5 min)*
 - [✓] **T3.3.4** - Create performance metrics collection *(10 min)*
 - [✓] **T3.3.5** - Test logging output and format *(5 min)*

---

## [~] 🧪 TASK 4: Validation and Documentation (Progress: 55%)
**Estimated Total Time: 4-5 hours**

### [   ] T4.1 - Unit Test Development (Progress: 0%)
**Estimated Time: 120 minutes**

- [   ] **T4.1.1** - Create test directory structure for Python *(10 min)*
- [✓] **T4.1.2** - Write unit tests for model training function *(25 min)*
- [✓] **T4.1.3** - Write unit tests for ONNX export functionality *(25 min)*
 - [✓] **T4.1.4** - Create Java test directory structure *(10 min)*
 - [✓] **T4.1.5** - Write unit tests for ONNXInferenceService *(30 min)*
 - [✓] **T4.1.6** - Write integration tests for REST endpoints *(20 min)*

### Recent progress (2025-09-26)

- [✓] **T4.1.5** - Write unit tests for ONNXInferenceService — added parsing and compatibility unit tests and made helper functions testable.
- [✓] **T4.1.6** - Write integration tests for REST endpoints — added MockMvc test to verify 400 payload for validation failures and updated controller advice.

- [✓] **T3.2.0** - Added `java/src/test/java/com/talkingsmoke/JarE2ETest.java` and packaged JAR produced by `mvn package` (used by the smoke test). 

### [   ] T4.2 - Error Handling and Edge Cases (Progress: 0%)
**Estimated Time: 90 minutes**

- [   ] **T4.2.1** - Add error handling for missing model files *(20 min)*
 - [✓] **T4.2.1** - Add error handling for missing model files *(20 min)*
 - [✓] **T4.2.2** - Implement validation for input tensor dimensions *(25 min)*
- [   ] **T4.2.3** - Add graceful handling of ONNX Runtime exceptions *(25 min)*
- [   ] **T4.2.4** - Create user-friendly error messages *(15 min)*
- [   ] **T4.2.5** - Test all error scenarios *(5 min)*

### [   ] T4.3 - Code Documentation (Progress: 0%)
**Estimated Time: 90 minutes**

- [✓] **T4.3.1** - Add docstrings to all Python functions *(30 min)*
- [✓] **T4.3.2** - Add Javadoc comments to all public methods *(30 min)*
- [✓] **T4.3.3** - Create inline code comments for complex logic *(20 min)*
 - [✓] **T4.3.4** - Update README with build and run instructions *(10 min)*


## 🛡️ Assistant helper: avoid getting stuck

Added `tools/ensure_not_stuck.ps1` — a small PowerShell watchdog to run long commands with a timeout and heartbeat messages. Use it when running exporters, packaging, or long test suites locally so your terminal doesn't sit idle for long periods.

- [✓] **T-Tools.1** - Add `ensure_not_stuck.ps1` watchdog script *(5 min)*
- [✓] **T-Tools.2** - Add wrapper scripts (`run_mvn_test`, `run_exporter`, `run_mvn_package`, `run_jar_e2e`) *(10 min)*
- [✓] **T-Tools.3** - Add VS Code tasks for common flows *(5 min)*
