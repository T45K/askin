---
name: verify
description: Run the project's verification path (unit tests + debug APK assembly) for this Android MVP. Use whenever the user asks to verify, validate, run the build, run tests, or sanity-check changes before committing.
---

# verify

Run the canonical verification command for this project:

```shell
gradle :app:testDebugUnitTest :app:assembleDebug
```

This is the same command documented as the verification path for the repo. Run it from the project root (the worktree root, not the primary repo). Report the result back: pass/fail, and on failure include the relevant error excerpt (compilation error, failing test name, AGP/Gradle complaint) so the user can act on it without scrolling through the full log.
