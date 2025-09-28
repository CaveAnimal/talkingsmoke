Title: docs(validation): input validation, tests, and task updates

Summary:
- Replace boolean compatibility check with a richer `InputValidationResult` type.
- Add unit tests for parsing and validateInputCompatibility behavior.
- Add MockMvc integration test to assert structured 400 payload on validation failure.
- Improve ONNX inference service testability (public parse helper and `setTestInputInfo` hook).
- Update TASKS, TODO, and LESSONS_LEARNED with progress and guidance about large binary files and Git LFS.
- Add `.gitignore` and `MODELS.md` to prevent accidentally committing large models or build outputs.

Files changed:
- java/src/main/java/com/talkingsmoke/service/InputValidationResult.java (new)
- java/src/main/java/com/talkingsmoke/service/ONNXInferenceService.java (existing; unit-testable helpers)
- java/src/test/... (new/updated tests)
- talkingsmoke_TASKS.md (updated)
- talkingsmoke_TODO.md (updated)
- talkingsmoke_LESSONS_LEARNED.md (updated)
- .gitignore (new)
- MODELS.md (new)

Notes:
- I pushed these changes to branch `feature/validation-parse-tests-clean-2` which was created from `origin/main` to avoid re-pushing large binary files in history.
- Maven tests currently fail during test bootstrap due to one test (`TalkingSmokeApplicationTests`) requiring an explicit SpringBootTest configuration; I can either adjust the test to include the main application class or skip that test temporarily. I can implement the minimal fix in this PR if you want the tests to run in CI.

How to review:
1. Open the branch and review code changes in `java/src/main/java/com/talkingsmoke/service` and the new tests.
2. Verify the documentation (.md files) and .gitignore changes.
3. Approve & merge; after merge we can decide whether to add Git LFS or perform history cleanup for the large model file.
