The AI assistant will read the GOALS, PRD, TASKS, and LESSONS_LEARNED prior to beginning a task
The assistant will read EXAMPLES for the task it is currently working on.
The assistant will follow the roadmap laid out in these documents and complete the tasks in order.
The assistant will updated the tasks document with the following symbols:
	Not Started → [ ]
	In Progress → [-]
	Completed → [X]
	Tested & Verified → [V]
	Blocked → [!]
	Deferred → [>]
If the task is deferred for any reason, include the reason in the line beneath that task.
When asked for a status update on the project, the assistant will provide a list of each category and the percent of total tasks for that category.

The assistant normally keeps it's own internal TODO list.  Whenever the assistant changes it's own internal TODO list, it will also update the file *_TODO.md using 
the files found in the tools subfolder by invoking the PowerShell wrapper `tools/sync_todos.ps1`
The assistant will add or update a timestamp at the bottom of the *_TODO.md with the format:
	Last synced: YYYY-MM-DD hh:mm:ss AM/PM CDT
where CDT represent Central Daylight Time.
The assistant will keep the *_TODO.md up-to-date with its internal plan without user intervention.
This repository and its automated tests use an embedded H2 database (in-memory or file-backed) for testing and development.

##Continuous Improvement
Most AI assistants operate in a stateless environment.  This often leads to unnecessary work, and/or repeated attempts with the same solutions previously attempted.
In the interest of continuous improvement and Agile development, Stop all work and remind me to hold a sprint retrospective at about 20-25% completion milestones of the project, or about4 or 5 times during the project.

## Environmental Awareness
Detected tool versions (recorded by assistant):
- Java: 21.0.6
- Python: 3.10.0
- Maven: 3.9.11
- Git: 2.51.0
- CMake: 4.1.1

These versions have been recorded so the assistant no longer needs to run the version-check step on future runs unless the environment changes. If the environment is modified, the assistant will re-run the version probe and update this section.

Python compatibility guidance for core libraries (PyTorch / ONNX / ONNX Runtime):

- Recommended Python versions: 3.8 through 3.11 are the most compatible overall; the "sweet spot" for compatibility and pre-built wheels is Python 3.9 or 3.10.
- Safest single choice for this project: Python 3.10 — mature, widely supported by PyTorch, ONNX and ONNX Runtime wheels, and stable for CI.
- Python 3.9: also excellent and widely supported.
- Python 3.11: generally supported by recent releases; good performance, but some binary wheels may lag behind 3.10 depending on release timing.
- Avoid Python 3.12+ for now: binary/compiled packages may not have pre-built wheels immediately and support can be inconsistent.

Quick compatibility checklist when upgrading or installing environments:

1. Check the PyTorch installation page for officially supported Python versions and wheel availability for your platform.
2. Check the ONNX and ONNX Runtime release notes for any Python compatibility notes.
3. Prefer Python 3.10 in CI and developer environments for maximum reproducibility across platforms.


## Powershell

Rules for running PowerShell commands (interactive or automated):

- Test complex pipelines interactively first; break long one-liners into small steps. Run each diagnostic command separately.
- Verify cmdlet names exactly (for example use `ConvertTo-Json`, not `Convert-To-Json`). When unsure, run `Get-Command <name>`.
- Avoid printing raw `$env:PATH` as a single token; instead split and print entries safely:
	- `$env:PATH -split ';' | ForEach-Object { Write-Output $_ }`
- Prefer `where.exe cmake` or `Get-Command cmake` to check for tool availability, and `cmake --version` to confirm the installed version.
- After installing system-level tools (CMake, Visual Studio workloads), restart the terminal/IDE so the updated PATH is visible to new sessions.
- When automating PowerShell from another process, prefer small, quoted commands or temporary script files rather than one long semicolon-separated command that expands many variables.

These rules reduce quoting/parsing issues and make tool-probing reliable across sessions and automation contexts.


## When You Need Help

**Stuck on a task step?**
1. Check the examples document for task step ID
2. Review error messages carefully
3. Search Stack Overflow for specific error
4. Ask team lead with specific question and task ID

**Task taking longer than estimated?**
- Break it down further into smaller steps
- Update time estimate for future reference
- Document what made it complex

