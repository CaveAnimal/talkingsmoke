## Collaboration & Workflow

- Branching: use feature branches for new work: `feature/<short-description>`; use `hotfix/<short>` for urgent fixes and `chore/<short>` for maintenance.
- Pull requests: open PRs against `main`. Include a short description, link to task/issue ID, list of changed files, and a brief testing checklist.
- Review rules: at least one approving review required before merging. Prefer a reviewer who did not author the change.
- Merge strategy: prefer squash-and-merge for feature work to keep history compact. Use merge commits only when preserving branch history is important.

## Continuous Integration / Continuous Delivery (CI/CD)

- CI must run tests and linters on each PR. If CI is not present, the assistant will add a minimal workflow that runs tests on Python 3.10.
- Artifacts: build artifacts (for example ONNX exports) must be produced in CI only when explicitly requested and stored in a stable location if needed for downstream stages.

## Releases & Versioning

- Use semantic versioning for released artifacts (MAJOR.MINOR.PATCH). Tag releases in git and include a short changelog entry.

## How the Assistant Will Continue Now

- I read `TheRules.md` and appended pragmatic collaboration, testing, CI, security, and release rules to help the project move forward.
- Next I can (pick one or do all):
    - Update `TASKS` to reflect the assistant's current internal TODOs and run `tools/sync_todos.ps1` to persist them.
    - Add a minimal GitHub Actions workflow to run tests on Python 3.10 if CI is missing.
    - Create a small ONNX smoke-test that loads an exported model and runs one inference.

If you'd like me to proceed with any of those, tell me which and I'll do it.
