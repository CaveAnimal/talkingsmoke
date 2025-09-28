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

## TASKS (Project Roadmap)

- The assistant is responsible for keeping the project's TASKS file (`talkingsmoke_TASKS.md`) accurate and up-to-date. This is the primary human-facing roadmap and must reflect real project status.
- Use the same status symbols shown above when editing `talkingsmoke_TASKS.md` so the project and assistant use a consistent notation.
- Update `talkingsmoke_TASKS.md` immediately when making project-level status changes (start a task, mark complete, mark blocked, or defer). If deferring a task include the reason on the line beneath the task.
- When the assistant's internal TODO changes in a way that affects project-level status, the assistant must update `talkingsmoke_TASKS.md` as well as its internal snapshot (`tools/manage_todo_list_snapshot.json`).
- After updating the internal snapshot, the assistant must run the provided sync helper to regenerate the assistant-maintained TODO (`talkingsmoke_TODO.md`) by invoking the PowerShell wrapper: `tools/sync_todos.ps1`.
- Sync cadence: update TASKS immediately on any status change, and also after any burst of edits (≈3–5 tool calls or editing >3 files), before producing a formal status report, and at end-of-session.
- When asked for a status update, report percent-complete using the TASKS file as the authoritative source; also ensure the assistant TODO and snapshot reflect the same state.
- Every task step and subtask listed in `talkingsmoke_TASKS.md` must be updated individually as work progresses — not just the top-level task entries. Mark subtasks with the same status symbols and include reasons/notes for deferred or blocked subtasks.

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

## Use of the developer watchdog

- The project provides a small PowerShell watchdog script at `tools/ensure_not_stuck.ps1` that runs commands with periodic heartbeats and a timeout.
- Rule: When running non-interactive, potentially long-running commands locally (builds, test suites, exporters, packaging, model training, etc.), the assistant and contributors MUST use `ensure_not_stuck.ps1` as often as practicable. Mark exceptions explicitly in the task notes when a command is intentionally run without the watchdog (for example, when an interactive prompt is required or when launching a detached background service).
- The assistant will wrap its own long-running invocations with the watchdog by default and document any deliberate exceptions in `talkingsmoke_TASKS.md`.

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


## Code Quality & Testing

- Tests: every feature or bugfix should include unit tests covering the happy path and at least one edge case. Place python tests in `python/tests/` following existing patterns.
- Test runs: run unit tests locally before opening a PR. The assistant will run the project's test suite after making code changes as a smoke check.
- Linting/Formatting: follow project style (black/flake8 or equivalent). Run formatters as pre-commit hooks where possible.
- Coverage: document new behavior in tests; aim to keep or increase coverage for modified modules.

## Security & Secrets

- Never commit secrets or credentials to the repository. Use environment variables or secure vaults for CI secrets.
- If a secret is accidentally committed, rotate it immediately and create a task describing the incident and remediation steps.

## Data & Model Handling

- Do not commit large model files or training datasets to the repository. Use external storage (S3, Azure Blob, etc.) and store only reproducible export scripts and small examples in `python/models/`.
- ONNX export: include versioned exporter scripts and a small smoke test that verifies model load and inference using ONNX Runtime.


## Communication & Status

- Status updates: when asked, the assistant will summarize progress across the categories (GOALS, PRD, TASKS, LESSONS_LEARNED) and include percent complete per the task symbols in `TASKS`.
- Retrospectives: as earlier noted, stop work and remind the user for a sprint retrospective at ~20-25% completion milestones.


