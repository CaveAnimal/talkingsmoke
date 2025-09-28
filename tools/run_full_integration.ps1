# Helper script: export ONNX model, copy into Java resources, and run Java tests
# Usage: from repo root in PowerShell: .\tools\run_full_integration.ps1

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$repoRoot = Split-Path -Parent $scriptDir
$pythonExporter = Join-Path $repoRoot "python\onnx_exporter.py"
$exported = Join-Path $repoRoot "python\models\exported_model.onnx"
$dest = Join-Path $repoRoot "java\src\main\resources\models\exported_model.onnx"

Write-Host "Running Python ONNX exporter..."
python $pythonExporter
if ($LASTEXITCODE -ne 0) {
    Write-Error "Python exporter failed with exit code $LASTEXITCODE"
    exit $LASTEXITCODE
}

if (-Not (Test-Path $exported)) {
    Write-Error "Exporter did not produce $exported"
    exit 2
}

# Ensure models directory exists in Java resources
$destDir = Split-Path $dest -Parent
if (-Not (Test-Path $destDir)) { New-Item -ItemType Directory -Path $destDir -Force | Out-Null }

Write-Host "Copying exported ONNX to Java resources..."
Copy-Item -Path $exported -Destination $dest -Force
if ($LASTEXITCODE -ne 0) {
    Write-Error "Failed to copy exported model to $dest"
    exit $LASTEXITCODE
}

Write-Host "Running Maven tests in java module..."
Push-Location (Join-Path $repoRoot "java")
$mvn = "mvn"
& $mvn -DskipTests=false test
$rc = $LASTEXITCODE
Pop-Location

exit $rc
