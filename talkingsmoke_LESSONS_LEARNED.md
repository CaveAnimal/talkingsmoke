## Lesson: PowerShell command quoting & PATH printing caused command parsing errors

Date: 2025-09-25

Summary
-------
While attempting to verify and use CMake so pip could build `onnx`, several PowerShell one-liners executed from the assistant ran into parsing errors. The shell output contained many messages like "The term 'C:\Program' is not recognized..." which made it appear that PATH entries were being interpreted as separate commands.

What happened (reproduction)
----------------------------
- I executed long, single-line PowerShell commands that both printed labels and then attempted to expand and print `$env:PATH` or run `cmake --version` in the same command string.
- Example pattern that triggered issues when used from an automated session or embedded runner:
  - Write-Host '...' ; $env:PATH.Split(';') | ForEach-Object { Write-Host $_ }
- Observed errors included many "The term 'C:\Program' is not recognized as the name of a cmdlet..." messages.

Root cause
----------
- When complex command strings are assembled and executed in one call (especially when passed through wrappers that do additional parsing), PATH entries that contain spaces (for example "C:\Program Files\...") can be interpreted incorrectly if the combined command string is not quoted/escaped properly.
- Some of the earlier attempted constructs also used parameters or operators that behaved differently across PowerShell editions or when the command string was forwarded through a remote/automation context.

Solution applied and recommended safe commands
--------------------------------------------
1. Keep checks small and separate. Use short commands and test each step in an interactive shell first:

   - Check for cmake on PATH:

     ```powershell
     where.exe cmake
     Get-Command cmake -ErrorAction SilentlyContinue | Format-List -Property Name,Source,Path
     & cmake --version
     ```

   - Print PATH entries safely (don’t let the shell re-parse the line):

     ```powershell
     $env:PATH -split ';' | ForEach-Object { Write-Output "PATH: $_" }
     ```

   - If you need to investigate only entries containing a substring, filter the array (safe):

     ```powershell
     $env:PATH -split ';' | Where-Object { $_ -match 'CMake|Program Files' } | ForEach-Object { Write-Output $_ }
     ```

2. If you just installed CMake (or Visual Studio components), restart your PowerShell/VS Code session so the updated PATH from the installer is picked up.

3. To add CMake to the current session PATH without restarting (temporary):

   ```powershell
   $cmakeDir = 'C:\Program Files\CMake\bin'  # replace with actual install path
   $env:PATH = "$cmakeDir;" + $env:PATH
   cmake --version
   ```

4. After CMake is visible to the session, re-run pip install:

   ```powershell
   python -m pip install -r python/requirements.txt
   ```

Notes and follow-ups
--------------------
- In automation contexts (CI, or when invoking PowerShell from another process), avoid building a single, very long command string that mixes label printing and unescaped variable expansion. Break steps into separate invocations or ensure proper quoting.
- If pip build still fails after CMake is available, check for a C/C++ toolchain and other build deps. On Windows, installing the "Desktop development with C++" workload in Visual Studio or the Build Tools can be necessary for some packages.
- Alternative: Use conda/mamba for reproducible environments that supply prebuilt `onnx` artifacts and native deps.

Action recorded
---------------
- Incident recorded by assistant on 2025-09-25. Root cause: command-string quoting/parsing and session PATH not updated after install.
- Recommendation: restart shells after installing tools; use the safe commands above when diagnosing PATH/command availability.

### First PowerShell command failure (typo + log)

On the first attempt to probe installed tools I issued a PowerShell one-liner that attempted to pipe command information into a JSON converter. The command contained a typo: `Convert-To-Json` (with an extra hyphen) instead of the correct cmdlet name `ConvertTo-Json`.

Observed log excerpt (original run):

Convert-To-Json : The term 'Convert-To-Json' is not recognized as the name of a cmdlet, function, script file, or operable program. Check the spelling of the 
name, or if a path was included, verify that the path is correct and try again.

This error immediately stopped the probe and produced confusing output. The root cause was a simple typo in the cmdlet name; PowerShell cmdlet names are sensitive to correct tokenization (no extra hyphens in this case).

Correct command (example):

```powershell
Get-Command java, python, mvn -ErrorAction SilentlyContinue | Select-Object Name, Version, Source | ConvertTo-Json
```

Or, safer when interacting with PATH values and varied shells, prefer breaking steps into simpler commands and printing values explicitly (see earlier recommendations in this file).

## Powershell (rules)

To avoid repeated mistakes when running PowerShell from automation or in long one-liners, follow these rules:

- Always test complex PowerShell pipelines interactively before running them from automation. Break commands into smaller steps.
- Use correct cmdlet names and casing/spacing (e.g., `ConvertTo-Json`, not `Convert-To-Json`). When in doubt, run `Get-Command <name>` first.
- Avoid single-line commands that both print labels and expand large environment variables; instead run separate commands to inspect `$env:PATH` or to call external tools.
- When printing PATH, use a safe split approach: `$env:PATH -split ';' | ForEach-Object { Write-Output $_ }` to avoid re-parsing path fragments as commands.
- After installing system-level tools (CMake, Visual Studio workloads), restart the terminal/IDE so the updated PATH is visible to new sessions.
- When automating Powershell commands from another process, prefer invoking small, quoted commands (or use a temporary script file) rather than a very long one-line with many semicolons and unescaped expansions.

These simple rules will reduce typos, quoting/parsing issues, and PATH visibility errors in future runs.
