# [BUG] PreToolUse and SessionStart hooks not firing — configured in settings.json but never executed

## What happened?

Hooks configured in `~/.qwen/settings.json` are never executed, regardless of matcher pattern or hook event type.

## What did you expect to happen?

PreToolUse hooks should fire before every tool execution (as documented in [docs/users/features/hooks.md](https://github.com/QwenLM/qwen-code/blob/main/docs/users/features/hooks.md)). SessionStart hooks should fire when a new session begins.

## Configuration

```json
{
  "hooks": {
    "PreToolUse": [
      {
        "matcher": "Bash",
        "hooks": [
          {
            "type": "command",
            "command": "/home/mike/.claude/hooks/rtk-rewrite.sh"
          }
        ]
      }
    ]
  }
}
```

Also tested with wildcard matcher `"matcher": ".*"` and a simple debug script — neither fires.

## Reproduction Steps

1. Add a PreToolUse hook to `~/.qwen/settings.json` matching `"Bash"` or `".*"`
2. Restart Qwen Code
3. Run any shell command (e.g. `ls`)
4. Observe: hook script is never executed

Also tested `SessionStart` hook with `echo 'SessionStart hook fired' >&2` — no output on session start.

## Hook Script Verification

The hook scripts work correctly when invoked manually:

```bash
$ echo '{"hook_event_name":"PreToolUse","tool_name":"Bash","tool_input":{"command":"ls"}}' | /home/mike/.claude/hooks/rtk-rewrite.sh
{
  "decision": "allow",
  "reason": "RTK auto-rewrite",
  "hookSpecificOutput": {
    "hookEventName": "PreToolUse",
    "permissionDecision": "allow",
    "permissionDecisionReason": "RTK auto-rewrite",
    "updatedInput": {
      "command": "rtk ls"
    }
  }
}
```

Script is executable (`-rwxr-xr-x`), outputs valid JSON with required `decision` field matching the documented output format.

## Environment

- **Qwen Code version**: 0.14.0 (latest, released April 3, 2026 — same day hooks experimental flag was removed)
- **OS**: Linux
- **Auth type**: Ollama local endpoint (`http://localhost:11434/v1/`)
- **Model**: coder-model (via Ollama)
- **No project-level `.qwen/settings.json`** — only global `~/.qwen/settings.json`
- **No `disableAllHooks`** set

## Debugging Performed

1. ✅ Hook script manually invoked — works correctly
2. ✅ Output JSON format matches docs (has `decision`, `reason`, `hookSpecificOutput.permissionDecision`, `hookSpecificOutput.updatedInput`)
3. ✅ Script permissions verified (executable)
4. ✅ Settings.json syntax validated
5. ✅ Tested `"matcher": "Bash"` and `"matcher": ".*"` — neither fires
6. ✅ Tested `SessionStart` hook — also doesn't fire
7. ✅ Removed permissions allow rules — still no hook execution
8. ✅ No project-level settings overriding global config

## Notes

- Hooks documentation states they are enabled by default
- v0.14.0 changelog mentions `feat(hooks): remove experimental flag and add disabled state UI`
- There are no application logs showing hook execution attempts
- The `"Bash(ls *)"` permission allow rule does not appear to be the cause (tested without it)
- Unclear if hooks have limited support with Ollama local endpoint vs. cloud API

## Expected Behavior

Hook scripts should execute before tool calls, receive JSON via stdin, and return JSON via stdout to modify tool behavior.

## Actual Behavior

Hook scripts are never invoked. Tool calls execute directly without any hook interception.