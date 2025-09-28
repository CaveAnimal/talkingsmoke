param()
Write-Output "Running Jar E2E test (gated by RUN_JAR_E2E=true)"
$env:RUN_JAR_E2E = 'true'
mvn -DskipTests=false -Dtest=JarE2ETest test
