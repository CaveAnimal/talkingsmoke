Running the Jar E2E test

Local (developer)

1. Export the ONNX model and copy into Java resources:

```powershell
cd /d E:\MyProjects\MyGitHubCopilot\talkingsmoke\tas-01
python python/onnx_exporter.py
Copy-Item python\models\exported_model.onnx -Destination java\src\main\resources\models\exported_model.onnx -Force
```

2. Package the Java project:

```powershell
cd /d E:\MyProjects\MyGitHubCopilot\talkingsmoke\tas-01\java
mvn -DskipTests package
```

3. Run the jar e2e test (this starts the packaged JAR, calls /health and /infer):

```powershell
$env:RUN_JAR_E2E = 'true'
mvn -DskipTests=false -Dtest=JarE2ETest test
```

CI (GitHub Actions)

- The repository includes `.github/workflows/jar-e2e.yml` which can be triggered manually via the Actions UI or runs nightly at 03:00 UTC. It exports the ONNX model, copies it into the Java module, packages the application, and runs the `JarE2ETest`.
