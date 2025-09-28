param()
function Write-Stamp($m){ $t = Get-Date -Format 'yyyy-MM-dd HH:mm:ss'; Write-Output "[$t] $m" }

Write-Stamp "Starting local app test"

# find jar in java/target
$jar = Get-ChildItem -Path .\java\target -Filter *.jar -Recurse -ErrorAction SilentlyContinue | Where-Object { $_.Name -notlike '*.jar.original' } | Select-Object -First 1
if (-not $jar) {
    Write-Stamp "No packaged JAR found in java/target — running mvn package"
    Push-Location .\java
    mvn -DskipTests package
    Pop-Location
    $jar = Get-ChildItem -Path .\java\target -Filter *.jar -Recurse | Where-Object { $_.Name -notlike '*.jar.original' } | Select-Object -First 1
}

if (-not $jar) { Write-Stamp "ERROR: No JAR available after build"; exit 2 }

$jarPath = $jar.FullName
Write-Stamp "Using JAR: $jarPath"

$port = 8081
$proc = Start-Process -FilePath 'java' -ArgumentList '-jar', "`"$jarPath`"", "--server.port=$port" -PassThru -WindowStyle Hidden
Write-Stamp "Started process PID=$($proc.Id)"

try {
    # wait for health
    $timeout = 30
    $start = Get-Date
    $healthy = $false
    while (((Get-Date) - $start).TotalSeconds -lt $timeout) {
        try {
            $resp = Invoke-WebRequest -Uri "http://localhost:$port/health" -UseBasicParsing -TimeoutSec 2 -ErrorAction Stop
            if (@(200,204,500) -contains $resp.StatusCode) { $healthy = $true; break }
        } catch { }
        Start-Sleep -Seconds 1
    }
    if (-not $healthy) { Write-Stamp "Health check failed"; exit 3 }
    Write-Stamp "Health OK"

    # POST to /infer
    $payload = '{"input":[0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1.0]}'
    try {
        $res = Invoke-RestMethod -Uri "http://localhost:$port/infer" -Method Post -Body $payload -ContentType 'application/json' -TimeoutSec 10 -ErrorAction Stop
        # expect object with 'output' array when 200
        if ($null -eq $res) { Write-Stamp "/infer returned null"; exit 4 }
        if ($res.output -and $res.output.Count -gt 0) {
            Write-Stamp "/infer returned output array with $($res.output.Count) elements"
        } else {
            Write-Stamp "/infer response did not contain expected output array"
            exit 5
        }
    } catch {
        Write-Stamp "POST /infer failed: $_"
        exit 6
    }
    Write-Stamp "Local app test succeeded"
    exit 0
} finally {
    if ($proc -and !$proc.HasExited) {
        Write-Stamp "Stopping process PID=$($proc.Id)"
        try { Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue } catch { }
    }
}
