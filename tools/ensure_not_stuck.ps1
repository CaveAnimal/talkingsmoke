<#
.SYNOPSIS
  Run a command with a timeout and periodic heartbeat output to avoid long silent waits.

.PARAMETER Command
  The command to run (as a single string). It will be executed by PowerShell.

.PARAMETER TimeoutSec
  Maximum number of seconds to allow the command to run (default 1800 = 30m).

.PARAMETER HeartbeatSec
  Interval in seconds to print a heartbeat and tail of output (default 30s).

Examples:
  .\ensure_not_stuck.ps1 -Command 'mvn -DskipTests=false test' -TimeoutSec 1800

This script helps prevent long, silent blocking runs by printing progress and
terminating the process if it exceeds the timeout.
#>
param(
  [Parameter(Mandatory=$false)] [string]$Command,
  [Parameter(Mandatory=$false)] [string]$ScriptFile,
  [int]$TimeoutSec = 1800,
  [int]$HeartbeatSec = 30,
  [int]$TailChars = 2000
)

function Write-Stamp($msg) {
    $t = Get-Date -Format 'yyyy-MM-dd HH:mm:ss'
    Write-Output "[$t] $msg"
}

Write-Stamp "Starting command with timeout ${TimeoutSec}s: $Command"

$outFile = [System.IO.Path]::GetTempFileName()
$errFile = [System.IO.Path]::GetTempFileName()

# Start process via PowerShell so complex commands are supported
  $psi = New-Object System.Diagnostics.ProcessStartInfo
  $psi.FileName = "powershell"
  if ($ScriptFile) {
    # Run a script file to avoid complex quoting issues
  $abs = (Resolve-Path -Path $ScriptFile).ProviderPath
  $psi.Arguments = '-NoProfile -NonInteractive -File "' + $abs + '"'
  } elseif ($Command) {
    $psi.Arguments = "-NoProfile -NonInteractive -Command ${Command}"
  } else {
    Write-Stamp "Either -Command or -ScriptFile must be provided."
    exit 2
  }
$psi.RedirectStandardOutput = $true
$psi.RedirectStandardError = $true
$psi.UseShellExecute = $false
$psi.CreateNoWindow = $true

$proc = New-Object System.Diagnostics.Process
$proc.StartInfo = $psi

try {
    $proc.Start() | Out-Null
    # Asynchronously copy streams to files so we can tail them
  # Create temporary files to capture stdout/stderr from the child process
  if (-not (Test-Path $outFile)) { New-Item -Path $outFile -ItemType File | Out-Null }
  if (-not (Test-Path $errFile)) { New-Item -Path $errFile -ItemType File | Out-Null }

  # Start async readers to copy streams to files continuously
  $stdOutStream = New-Object System.IO.FileStream($outFile, [System.IO.FileMode]::Open, [System.IO.FileAccess]::Write, [System.IO.FileShare]::ReadWrite)
  $stdOutWriter = New-Object System.IO.StreamWriter($stdOutStream)
  $stdErrStream = New-Object System.IO.FileStream($errFile, [System.IO.FileMode]::Open, [System.IO.FileAccess]::Write, [System.IO.FileShare]::ReadWrite)
  $stdErrWriter = New-Object System.IO.StreamWriter($stdErrStream)
  $stdOutWriter.AutoFlush = $true
  $stdErrWriter.AutoFlush = $true

  Start-Job -ScriptBlock {
    param($pidArg, $oFile, $eFile)
    try {
      $p = Get-Process -Id $pidArg -ErrorAction Stop
      while (-not $p.HasExited) {
        try {
          $out = $p.StandardOutput.ReadLine()
          if ($null -ne $out) { [System.IO.File]::AppendAllText($oFile, $out + [Environment]::NewLine) }
        } catch { Start-Sleep -Milliseconds 50 }
        try {
          $err = $p.StandardError.ReadLine()
          if ($null -ne $err) { [System.IO.File]::AppendAllText($eFile, $err + [Environment]::NewLine) }
        } catch { Start-Sleep -Milliseconds 50 }
        Start-Sleep -Milliseconds 50
      }
    } catch { }
  } -ArgumentList $proc.Id, $outFile, $errFile | Out-Null

    $start = Get-Date
  while (-not $proc.HasExited) {
    $elapsed = (Get-Date) - $start
    if ($elapsed.TotalSeconds -ge $TimeoutSec) {
      Write-Stamp "Timeout reached (${TimeoutSec}s). Killing process tree (Id=$($proc.Id))."
      try {
        # On Windows, use taskkill to kill the process tree; on other platforms fall back to Kill()
        if ($IsWindows) {
          Start-Process -FilePath "taskkill" -ArgumentList "/PID $($proc.Id) /T /F" -NoNewWindow -Wait
        } else {
          try { $proc.Kill() } catch { }
        }
      } catch { }
      break
    }
    Write-Stamp "Heartbeat: process running for $([int]$elapsed.TotalSeconds)s (pid=$($proc.Id)). Showing recent output..."
    # Print last TailChars of stdout/stderr files to give live context
    try {
      $sOut = Get-Content -Path $outFile -Raw -ErrorAction SilentlyContinue
      if ($sOut) {
        if ($sOut.Length -gt $TailChars) { $sOut = $sOut.Substring($sOut.Length - $TailChars) }
        Write-Output "--- STDOUT (last ${TailChars} chars) ---"
        Write-Output $sOut
      }
    } catch { }
    try {
      $sErr = Get-Content -Path $errFile -Raw -ErrorAction SilentlyContinue
      if ($sErr) {
        if ($sErr.Length -gt $TailChars) { $sErr = $sErr.Substring($sErr.Length - $TailChars) }
        Write-Output "--- STDERR (last ${TailChars} chars) ---"
        Write-Output $sErr
      }
    } catch { }
    Start-Sleep -Seconds $HeartbeatSec
  }

    # capture any remaining output
  # Read final outputs from files
  try { $out = Get-Content -Path $outFile -Raw -ErrorAction SilentlyContinue } catch { $out = "(failed to read stdout)" }
  try { $err = Get-Content -Path $errFile -Raw -ErrorAction SilentlyContinue } catch { $err = "(failed to read stderr)" }

  Write-Stamp "Process exited with code $($proc.ExitCode)"
  if ($out) {
    Write-Stamp "--- STDOUT (truncated to last $TailChars chars) ---"
    $outTail = if ($out.Length -gt $TailChars) { $out.Substring($out.Length-$TailChars) } else { $out }
    Write-Output $outTail
  }
  if ($err) {
    Write-Stamp "--- STDERR (truncated to last $TailChars chars) ---"
    $errTail = if ($err.Length -gt $TailChars) { $err.Substring($err.Length-$TailChars) } else { $err }
    Write-Output $errTail
  }

  exit $proc.ExitCode
} catch {
    Write-Stamp "Failed to start or monitor process: $_"
    exit 2
}
