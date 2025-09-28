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

## Lesson: Git history, large binary files, and safe publishing

Date: 2025-09-26

Summary
-------
During the recent work to add validation, tests, and improved ONNX handling I ran into failures when trying to push a local branch to the remote repository. The push was rejected by the remote pre-receive hook because the local commit history included large binary artifacts (an ONNX model in `java/src/main/resources/models/...` and the packaged jar under `java/target/`). This blocked publishing the new source and tests for review.

What happened
-------------
- I made many helpful local edits and ran tests until green, then committed them on a local feature branch that also (accidentally) included built artifacts and the model file in the commit history.
- When pushing, GitHub's size checks rejected the push because one file exceeded their 100MB hard limit and another exceeded recommended size thresholds.

Root cause
-----------
- Built artifacts and large model files were present in the working tree and were staged/committed. Because they were included in commits, simply deleting them and committing the deletions did not remove the large objects from history — the remote still refused the new push.

What I changed and why it helped
--------------------------------
- Implemented focused changes that modify only source, tests, and docs (no binaries).
- Created a new clean branch based on `origin/main` so the branch's history does not contain the large objects.
- Committed only the safe files (source, tests, docs) to the new branch and pushed that branch to remote. This avoids rewriting history and is the safest way to publish small, reviewable changes.

Recommendations / Best practices
--------------------------------
1. Never commit build outputs or large binary models into the main Git history. Add a `.gitignore` rule to exclude:
  - `/java/target/`
  - `**/exported_model.onnx`
  - Other temporary/build artifacts created during packaging or local experiments.

2. Store large model artifacts outside the Git repository, for example:
  - Use Git LFS for model files that must live alongside source (remember to track them before committing).
  - Use a cloud object store (S3, Azure Blob) or a release artifact (GitHub Releases) for large models, and store only the URL or checksum in the repo.

3. To remove large files that already exist in history, use a dedicated history-rewrite tool (BFG Repo-Cleaner or git filter-repo). This is invasive and will change commit hashes, so coordinate with collaborators.

4. When possible prefer the non-destructive approach: create a new branch from the remote default branch and cherry-pick or re-apply the safe changes there, then push that branch and open a PR.

5. For CI / automation, add a small pre-commit hook or CI check that ensures `/java/target` and model files are not accidentally committed.

6. Document where and how models should be published and consumed (e.g., a short `MODELS.md` or README section describing hosting, expected path, and checksum verification).

Action items (short-term)
------------------------
- Add `.gitignore` entries for `java/target` and common model filenames.
- Add an assistant-maintained TODO entry describing the push/cleanup steps and who will perform history rewrite (if needed).
- Create a short README note about model management and linking to the canonical model hosting location.

Action recorded
---------------
- Incident recorded on 2025-09-26. Root cause: large binaries included in local commits. Resolution strategy: publish safe changes on a fresh branch created from `origin/main`, and follow the recommendations above for long-term fixes.

## Lesson: PowerShell parser error when using shell-style `||` operator

Date: 2025-09-27

Summary
-------
During an automated attempt to create a branch and open a GitHub PR, a one-line PowerShell command used the POSIX-style `||` operator for a fallback action (for example: `gh --version 2>$null || Write-Output 'no-remote-branch'`). On Windows PowerShell 5.1 this produced a parser error:

```
The token '||' is not a valid statement separator in this version.
```

What happened (reproduction)
----------------------------
- Running a one-liner like this in Windows PowerShell 5.1 triggers the parser error:

  ```powershell
  gh --version 2>$null || Write-Output 'no-remote-branch'
  ```

  The same line works in POSIX shells (bash) and in PowerShell 7+ where `||` and `&&` are supported as logical operators.

Root cause
----------
- The command mixed shell idioms. `||` is a Bash/CMD conditional operator (and supported in PowerShell 7+), but it is not valid syntax in Windows PowerShell 5.1, which is the user's default shell in this environment. The PowerShell parser rejected the token, causing the entire compound command to fail before any of the intended checks or fallbacks could run.

Why this caused the script to "spin"
-------------------------------------
- The automation wrapped multiple checks and fallbacks in a single line. Because the parser failed at syntax level, none of the conditional logic executed and the script aborted earlier than the outer automation expected. This left the assistant waiting for a result that never arrived.

Safe alternatives and recommended fixes
--------------------------------------
1. Prefer PowerShell-native checks (portable across PowerShell versions):

   ```powershell
   if (Get-Command gh -ErrorAction SilentlyContinue) {
     gh --version
   } else {
     Write-Output 'gh-not-found'
   }
   ```

2. Use `Try/Catch` to handle non-zero exit behaviour without relying on `||`:

   ```powershell
   try {
     gh --version | Out-Null
   } catch {
     Write-Output 'gh-not-found'
   }
   ```

3. Use `cmd.exe` if you intentionally want POSIX-style `||` behavior inside a Windows session (explicit, but less idiomatic):

   ```powershell
   cmd /c "gh --version || echo gh-not-found"
   ```

4. Upgrade the environment to PowerShell 7+ if you prefer short `&&`/`||` chains and are able to rely on that runtime. Be explicit about which shell is being used when running automation.

5. Test one-liners interactively in the target shell before running them as an automated script. If you must pass a compound command into a wrapper, prefer a small script file and execute that to avoid quoting/parsing pitfalls.

Action taken
------------
- Added this analysis and the recommended PowerShell-safe snippets to `talkingsmoke_LESSONS_LEARNED.md` so future automation uses PowerShell-native patterns.

Recommended follow-ups
----------------------
- Update any automation snippets that currently use `||`/`&&` so they use PowerShell-native checks or run under PowerShell 7.
- Add a short comment in the automation wrapper to assert the shell version or to explicitly invoke `pwsh` when PowerShell 7 semantics are required.
