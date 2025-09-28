talking smoke means radio

[![CI](https://github.com/CaveAnimal/talkingsmoke/actions/workflows/ci.yml/badge.svg)](https://github.com/CaveAnimal/talkingsmoke/actions/workflows/ci.yml)

## Developer Quickstart (export ONNX model and run Java integration tests)

This project includes a Python trainer and ONNX exporter (`python/model_trainer.py` and `python/onnx_exporter.py`). To run the full local integration flow (export model, copy into Java resources, and run Java tests), do the following on Windows PowerShell:

1. Create a Python environment with the required packages (see `python/requirements.txt`).
2. From the repo root run the exporter and then run tests via the helper script:

```powershell
# Run these in PowerShell from the repo root
python .\python\onnx_exporter.py
; if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
$env:JAVA_HOME = $env:JAVA_HOME # ensure Java is available
; .\tools\run_full_integration.ps1
```

The helper script copies the exported model into `java/src/main/resources/models/exported_model.onnx` and runs `mvn test` in the `java/` module. The integration test that requires a classpath model will be skipped automatically if the model is not present.

Run the test suite and view progress:

```powershell
# Run Java tests (from repo root)
cd .\java ; mvn test

# Check task progress (uses tools/tmp_calc_percent.ps1)
powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\tmp_calc_percent.ps1
```

