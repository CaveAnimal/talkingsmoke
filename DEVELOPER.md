# Developer notes and troubleshooting

This project contains both a Python model exporter and a Java application that can run inference using ONNX models.

Quick steps (Windows PowerShell)

1. Ensure Python dependencies are installed:

```powershell
python -m pip install -r python/requirements.txt
```

2. Run the exporter and tests using the helper script:

```powershell
Set-Location -Path '<repo-root>'
powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\run_full_integration.ps1
```

Troubleshooting

- If Maven packaging or tests fail because of locked files, ensure no other Java process is running on the same project (use Task Manager or `netstat -ano | findstr ":8080"`).
- If ONNX Runtime reflective initialization fails, you can still run tests because they either skip or use the test hook. To validate native runtime, install a platform-appropriate `onnxruntime` Java artifact.

CI

A GitHub Actions workflow `.github/workflows/ci.yml` is included to run the exporter and Java tests on ubuntu-latest. Consider adding a Windows runner matrix if you need exact parity with local development.
