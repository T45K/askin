---
name: verify
description: Run the project's full verification path for this Android MVP — build/tests plus consistency checks against the codebase and documentation. Use whenever the user asks to verify, validate, run the build, run tests, or sanity-check changes before committing.
---

# verify

Verification has three stages. Run them in order — the build fails fastest, while the consistency checks are slower but catch issues the compiler cannot.

## 1. Build & unit tests

Run the canonical verification command from the project root (the worktree root, not the primary repo):

```shell
gradle :app:testDebugUnitTest :app:assembleDebug
```

Report pass/fail. On failure, include the relevant error excerpt (compilation error, failing test name, AGP/Gradle complaint) so the user can act on it without scrolling through the full log.

## 2. Codebase consistency

Check that the modified code fits with the surrounding code. The compiler will not catch these, so look for them explicitly:

- Naming, file location, and package layout match nearby code (e.g., Kotlin sources under `app/src/main/kotlin`, package `io.github.t45k.askin`).
- Architectural patterns match what's already in use — Compose for UI, ViewModel + StateFlow for state, repository/use-case for data, Room for persistence. Do not introduce a parallel pattern when an equivalent one already exists.
- Existing helpers/components are reused instead of duplicated. Search for similar names or behavior before adding a new one.
- The style of nearby code (formatting, idioms, error handling) is preserved unless the change is specifically about updating it.

If something diverges from the surrounding code without a clear reason, surface it — either fix it or state why the divergence is intentional.

## 3. Documentation consistency

Check that the modified code does not contradict the project documentation, and update the docs in the same change if it does:

- **`AGENTS.md`** — per its own instructions, any contradiction with the code must be resolved by updating `AGENTS.md` in the same change. Re-read it after the change and reconcile.
- **`CLAUDE.md`** — apply the same reconciliation to any project-instruction section whose behavior the change altered.
- File paths, class names, package names, and external commands cited in docs are still accurate after the change.

Report what (if anything) you updated. If any contradiction remains unresolved, flag it so the user can decide.
