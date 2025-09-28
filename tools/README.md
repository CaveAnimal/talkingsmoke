# Tools

This folder contains small helper scripts used while developing and testing the project.

ensure_not_stuck.ps1
- Runs a command with a timeout and prints periodic heartbeats and a live-tail
  of output to avoid long silent runs. Supports two invocation styles:
  -Command '<one-liner>' or -ScriptFile '<path-to-powershell-script>'.
  Use `-ScriptFile` for complex commands to avoid quoting issues.

Example (command):

```powershell
# Run mvn test with a 30 minute timeout and heartbeats every 30s
.\ensure_not_stuck.ps1 -Command 'mvn -DskipTests=false test' -TimeoutSec 1800 -HeartbeatSec 30
```

Example (script file):

```powershell
# Use the provided wrapper to avoid quoting pitfalls
.\ensure_not_stuck.ps1 -ScriptFile .\tools\run_mvn_test.ps1 -TimeoutSec 1800 -HeartbeatSec 30
```

VS Code
- A `.vscode/tasks.json` is included with sample tasks that call the watchdog for
  common developer flows (mvn test and the Python exporter). Run from the
  VS Code Command Palette: "Tasks: Run Task" -> pick the Watchdog task.
