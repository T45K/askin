# Agent Rules

## Text Files

- Ensure every generated or edited text file ends with a trailing newline.

## Git Commits

- When creating a commit, include Junie as a co-author using this trailer: `Co-authored-by: Junie <junie@jetbrains.com>`.

## Claude Code Hooks (`.claude/settings.json`)

The following operational rules are enforced for Claude Code via hooks defined in `.claude/settings.json`. Junie does not execute Claude Code hooks, so Junie must continue to follow these rules manually. The hooks themselves are documented here as the canonical source of truth for what they do — keep this section in sync if `.claude/settings.json` changes.

### 1. `PostToolUse` on `Write|Edit` — append trailing newline

After Claude writes or edits a file, the hook reads the resulting file path from the tool result, and if the path matches one of the recognized text-file extensions (`.kt`, `.kts`, `.md`, `.json`, `.toml`, `.xml`, `.yml`, `.yaml`, `.gradle`, `.properties`, `.txt`, `.gitignore`, `.gitattributes`, `.lock`, `.cfg`, `.conf`, `.ini`, `.sh`), it inspects the last byte of the file. If that byte is **not** a newline (`0x0a`), the hook appends a single `\n`. Files with extensions outside the list, non-existent paths, and any errors are ignored — the hook never blocks. The result is that every text file Claude touches ends with a trailing newline without the agent having to remember.

### 2. `PreToolUse` on `Write|Edit` — block edits to the primary repo

Before Claude writes or edits a file, the hook reads the target absolute path and applies these rules in order:

1. If the path is under `/Users/t45k/prog/askin/.claude/worktrees/*`, the edit is **allowed** (internal worktree).
2. If the path is under `/Users/t45k/prog/askin-*` (sibling-directory worktrees such as `askin-add-master-description`, `askin-make-masters-deletable`), the edit is **allowed** (external worktree).
3. Otherwise, if the path is under `/Users/t45k/prog/askin/`, the edit is **blocked**: the hook outputs `{"continue":false,"stopReason":"..."}` instructing the agent to work inside a git worktree.
4. Any path outside `/Users/t45k/prog/askin*` is **allowed** (paths in `/tmp`, the user's home, etc. are unaffected).

This prevents accidental edits in the primary checkout while leaving every legitimate worktree workflow untouched.

### 3. `PreToolUse` on `Bash` — fetch & merge `origin/main` before commit/push

Before Claude runs any Bash command, the hook reads the command string. If the command does **not** contain a `git commit` or `git push` invocation (matched with word-boundary regex — so `git status`, `git committer-fake`, etc. do not trigger), the hook exits without action. If it does match, the hook `cd`s to the repo root (via `git rev-parse --show-toplevel`) and runs `git fetch origin main` followed by `git merge origin/main --no-edit`. If either step fails — most commonly because of a merge conflict, but also for any other failure — the hook outputs `{"continue":false,"stopReason":"..."}` to block the commit/push, surfacing the conflict so Claude (or the user) can resolve it before retrying. If both succeed (including the "Already up to date" no-op case), the original `git commit` or `git push` proceeds normally.

### 4. `SessionStart` — best-effort fast-forward sync with `origin/main`

When a Claude Code session starts, the hook resolves the repo root, then runs `git fetch origin main && git merge --ff-only origin/main`. The `--ff-only` flag means a non-fast-forwardable branch (e.g. a feature branch with diverging local commits) is left untouched rather than producing a merge commit. The hook never blocks session start: any failure (no remote, dirty working tree, non-ff branch, network error) is silently swallowed. The intent is to keep checkouts up to date when it is trivially safe, and to do nothing when it is not.
