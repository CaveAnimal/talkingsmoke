# Model management

This project relies on potentially large ONNX models which should not be stored in the Git repository history.

Recommended approaches:

- Use Git LFS to track large model files if they must be stored alongside source. Example:

  ```bash
  git lfs track "**/*.onnx"
  git add .gitattributes
  git commit -m "chore: track ONNX models with Git LFS"
  ```

- Prefer hosting production or large models in an external artifact store (S3, Azure Blob Storage, or GitHub Releases). Keep only a small JSON/YAML manifest or a checksum and URL in the repo.

- Example manifest entry (models/manifest.yml):

  ```yaml
  models:
    - name: all-MiniLM-L6-v2
      version: 2025-09-25
      url: https://example-bucket.s3.amazonaws.com/models/all-MiniLM-L6-v2/exported_model.onnx
      sha256: <checksum>
  ```

- When running locally for development, download the model to `java/src/main/resources/models/<name>/exported_model.onnx` and ensure `.gitignore` excludes it.

- Document model expectations (input size, dtype, tokenizer) in the manifest or README so the Java service can validate compatibility at runtime.
