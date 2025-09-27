```markdown
<!-- talkingsmoke_TODO.md - Assistant-maintained todo list for project planning and actions -->
# Assistant TODOs (tracked by GitHub Copilot assistant)

This file records the assistant's working todo items, their status, and short notes. The assistant will update this file as tasks are started, completed, or deferred so the project owner can see planned work.

## Current Assistant Plan

- [x] T1.1 - Python environment setup -- Create venv, populate requirements.txt, and install Python dependencies (quick-start).
- [x] T1.2 - Basic model structure (SimpleClassifier and synthetic data) -- Implement SimpleClassifier, generate synthetic data, and scaffold training loop (T1.2.1-T1.2.3).
- [-] T2.1 - Maven project initialization -- Create Java/Maven project skeleton and add ONNX Runtime + Spring Boot dependencies.
- [-] T2.2 - Spring Boot basic structure -- Add main application class, controller, and service packages (T2.2.1-T2.2.3).
- [x] Sync TODO file -- Run tools/sync_todos.py to regenerate talkingsmoke_TODO.md from this snapshot.
- [x] T1.1.5 - Run Python unit tests -- Run unit tests for Python trainer to validate imports and basic behavior (may require installing dependencies).
- [x] T1.1.6 - Install Python dependencies -- Install packages from python/requirements.txt into the environment (pip install).

Last synced: 2025-09-25 08:34:23 PM CDT — ran by assistant

```

## Recent internal todo changes
- Status changed: T2.2 - Spring Boot basic structure (id=4) not-started -> in-progress

## Assistant follow-ups (2025-09-26)

- [ ] Create a clean feature branch from `origin/main` and commit only safe source/tests/docs changes; push the branch and open a PR.
- [ ] Add/confirm `.gitignore` entries for `/java/target/` and common model filenames (e.g. `**/exported_model.onnx`).
- [ ] Consider Git LFS or external hosting for large model files; document in `MODELS.md` or README.
- [ ] If required, coordinate a history-rewrite (BFG or git filter-repo) to remove large files from prior commits. This is invasive and should be scheduled.
