#!/bin/bash

# wiki_translation_maintenance_hermes.sh — Hermes variant of wiki_translation_maintenance.sh
# Same loop as the qwen-based original, but runs tasks via `hermes chat -q` (non-interactive).
#
# Differences from the qwen version:
#   - Uses hermes chat -q (single query, non-interactive) with --yolo for unattended runs
#   - Prompts passed via --query-file to avoid shell-quoting issues with nested quotes
#   - Wall-clock budget (--run-budget 3600 = 1h) replaces `timeout 300m`
#   - Toolsets restricted to what the tasks need (no delegation: free-model
#     children hang on model calls and the parent loses the whole run)
#   - A failed run is retried once with --resume latest
#   - Uncommitted work is never wiped: a failed run's partial edits are
#     stashed before the next iteration; unexplained dirt skips the iteration
#   - Every LEARN_EVERY iterations, a learning run lets hermes update its own
#     skill/memory files from observed maintenance history
#   - Language list includes recently added locales (nl, vi, ar, he)

set -u

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROMPT_DIR="$(mktemp -d /tmp/hermes_maint.XXXXXX)"
trap 'rm -rf "$PROMPT_DIR"' EXIT

RUN_BUDGET="${RUN_BUDGET:-3600}"    # seconds per task run; generous for a ~2-min task, tight enough to keep the hourly cadence
TOOLSETS="${TOOLSETS:-terminal,file,web}"
SLEEP_SECONDS="${SLEEP_SECONDS:-3600}"
LOG_FILE="${LOG_FILE:-$HOME/.hermes/logs/hermes_maintenance.log}"   # outside the repo: must survive repo-side resets and cleans
LEARN_EVERY="${LEARN_EVERY:-12}"    # run a skill/memory consolidation every N maintenance iterations

WIKI_LANGS="en, ru, es, fr, de, it, pl, pt-rBR, ja, ko, zh-rCN, zh-rTW, uk, hu, tr, el, in, ms, nl, vi, ar, he"

cat > "$PROMPT_DIR/wiki.txt" <<EOF
Read @docs/WIKI_DOCUMENTATION.md, pull repo master, pick 5 random wiki pages using ./pick_random_wiki_pages.sh (script lives in the repo root, not tools/), analyze them for compliance with wiki standards, identify issues like missing images, invalid headers, incorrect links, improper formatting, run the dokuwiki linter on them (python3 tools/py-tools/dokuwiki_linter.py), fix identified issues based on documentation standards, verify all links point to existing lowercase files, ensure proper image references exist. For mr: namespace pages and whenever entity facts (stats, mechanics, drops, behavior) are changed on any page, use tools/find_entity_usage.py to ground changes in entity implementation and usage in code. Also check wiki pages in all supported languages (${WIKI_LANGS}) to ensure consistency with game translations. Commit your changes to wiki-data and push it; don't commit or push into the main repo beyond what the task requires. Focus on maintaining consistency with wiki documentation standards.
EOF

cat > "$PROMPT_DIR/translation.txt" <<EOF
Read @docs/TRANSLATION_TASK.md, pull repo master, identify a few random missing strings in random languages (use tools/select_random_missing_string.py — note that nl and vi currently have large gaps), find their context using tools/find_string_usage.py, translate them properly based on the English reference and code context, add translations to the appropriate strings_all.xml files using tools/insert_translated_string.py, verify consistency with existing translations, run tools/validate_translations.py --auto-fix before committing, also select a few random strings and ensure consistency among all languages. Commit your changes and push. Focus on maintaining consistency with existing translations, proper grammar, cultural appropriateness for target languages, and proper string formatting following Android XML standards.
EOF

cat > "$PROMPT_DIR/learn.txt" <<EOF
Periodic self-maintenance for the Remixed Dungeon automation you operate.

Review what actually happened in recent unattended maintenance runs:
- git -C /home/nyrds/remixed-dungeon log (recent commits, including the wiki-data submodule)
- /home/nyrds/.hermes/logs/hermes_maintenance.log (per-iteration outcomes, failures, retries)
- /home/nyrds/remixed-dungeon/wiki_translation_maintenance_hermes.sh (the loop script — source of truth for current parameters)

Then bring your learned knowledge up to date:
1. Update your skill at /home/nyrds/.hermes/skills/software-development/remixed-dungeon-maintenance/ (SKILL.md and references/) so every fact matches current reality: script parameters (RUN_BUDGET, TOOLSETS, LEARN_EVERY, retry-on-failure behavior), log location, tool paths, and any recurring pitfall visible in the log or git history. Fix wrong facts, add only durable reusable knowledge, keep it concise.
2. Update your persistent memories in /home/nyrds/.hermes/memories/ the same way (e.g. current model/fallback configuration, node-specific facts).

Constraints: do NOT modify anything under /home/nyrds/remixed-dungeon, do not commit or push anything, and do not invent problems that the evidence does not support.
EOF

run_task() {
    local prompt_file="$1"
    shift
    hermes chat \
        --query-file "$prompt_file" \
        --in "$REPO_ROOT" \
        --toolsets "$TOOLSETS" \
        --skills remixed-dungeon-maintenance \
        --run-budget "$RUN_BUDGET" \
        --yolo \
        --quiet \
        "$@"
}

run_learning() {
    hermes chat \
        --query-file "$PROMPT_DIR/learn.txt" \
        --in "$HOME" \
        --toolsets terminal,file \
        --run-budget "$RUN_BUDGET" \
        --yolo \
        --quiet
}

log() {
    echo "[$(date)] $*" | tee -a "$LOG_FILE"
}

stash_partial_work() {
    # $1 = repo dir, $2 = human-readable label
    git -C "$1" stash push -u -m "hermes-maint: partial work from failed run ($2), $(date '+%F %T')" >/dev/null \
        && log "Stashed partial work in $2 left by the failed run (recover with: git -C '$1' stash list)" \
        || log "WARNING: failed to stash partial work in $2"
}

echo "Hermes maintenance script that randomly chooses between wiki and translation tasks"
echo "Run budget per task: ${RUN_BUDGET}s, sleep between iterations: ${SLEEP_SECONDS}s"
echo "Log file: $LOG_FILE"
echo "Press Ctrl+C to stop."
echo

LEFTOVER_POSSIBLE=0
ITERATION=0
while true; do
    TASK_CHOICE=$((RANDOM % 2))

    # Never wipe uncommitted work. Dirt left by a failed run is stashed
    # (recoverable) so the loop can proceed; any other dirt belongs to a
    # human, and the iteration is skipped rather than touching it.
    if [ "$LEFTOVER_POSSIBLE" -eq 1 ]; then
        [ -n "$(git -C "$REPO_ROOT/wiki-data" status --porcelain 2>/dev/null)" ] && stash_partial_work "$REPO_ROOT/wiki-data" "wiki-data"
        [ -n "$(git -C "$REPO_ROOT" status --porcelain)" ] && stash_partial_work "$REPO_ROOT" "main repo"
        LEFTOVER_POSSIBLE=0
    elif [ -n "$(git -C "$REPO_ROOT" status --porcelain)" ]; then
        log "WARNING: uncommitted changes in $REPO_ROOT that no failed run accounts for — skipping this iteration to protect them (commit or stash manually to resume maintenance)"
        sleep "$SLEEP_SECONDS"
        continue
    fi

    if [ $TASK_CHOICE -eq 0 ]; then
        log "Running wiki maintenance via hermes"
        PROMPT_FILE="$PROMPT_DIR/wiki.txt"
        TASK_NAME="Wiki maintenance"
    else
        log "Running translation task via hermes"
        PROMPT_FILE="$PROMPT_DIR/translation.txt"
        TASK_NAME="Translation task"
    fi

    run_task "$PROMPT_FILE"
    CMD_STATUS=$?

    # Retry once by resuming the failed run's session so a run that died
    # mid-task continues where it stopped instead of losing the work.
    # --resume latest is safe here: hermes is not used manually on this node.
    if [ $CMD_STATUS -ne 0 ]; then
        log "$TASK_NAME failed with exit status: $CMD_STATUS; retrying once with --resume latest"
        run_task "$PROMPT_FILE" --resume latest
        CMD_STATUS=$?
    fi

    if [ $CMD_STATUS -ne 0 ]; then
        log "$TASK_NAME failed with exit status: $CMD_STATUS (retry exhausted)"
    else
        log "$TASK_NAME completed successfully"

        if [[ -n $(git -C "$REPO_ROOT" status --porcelain) ]]; then
            log "Changes detected, committing..."

            if [ $TASK_CHOICE -eq 0 ]; then
                git -C "$REPO_ROOT" add wiki-data/
                git -C "$REPO_ROOT" add RemixedDungeon/src/main/java/ 2>/dev/null || true
                git -C "$REPO_ROOT" add tools/py-tools/ 2>/dev/null || true
                git -C "$REPO_ROOT" add docs/ 2>/dev/null || true
                COMMIT_MSG="Auto-wiki: Update wiki pages based on maintenance iteration"
            else
                git -C "$REPO_ROOT" add RemixedDungeon/src/main/res/values-*/strings_all.xml
                COMMIT_MSG="Auto-translation: Add missing string translations"
            fi

            git -C "$REPO_ROOT" commit -m "$COMMIT_MSG

Automated commit to $([ $TASK_CHOICE -eq 0 ] && echo 'update wiki pages' || echo 'add missing translations') identified during iteration."

            if git -C "$REPO_ROOT" pull --rebase -q; then
                if git -C "$REPO_ROOT" push origin HEAD; then
                    log "$TASK_NAME changes committed and pushed successfully"
                else
                    log "Failed to push $([ $TASK_CHOICE -eq 0 ] && echo 'wiki' || echo 'translation') changes"
                fi
            else
                log "Rebase against origin failed; leaving changes committed locally"
            fi
        else
            log "No changes detected after $([ $TASK_CHOICE -eq 0 ] && echo 'wiki' || echo 'translation') iteration"
        fi
    fi

    if [ $CMD_STATUS -ne 0 ] && [ -n "$(git -C "$REPO_ROOT" status --porcelain)" ]; then
        LEFTOVER_POSSIBLE=1
        log "Failed run left uncommitted changes; they will be stashed before the next iteration"
    fi

    ITERATION=$((ITERATION + 1))
    if [ $((ITERATION % LEARN_EVERY)) -eq 0 ]; then
        log "Running periodic learning task via hermes"
        if run_learning; then
            log "Learning task completed"
        else
            log "Learning task failed (non-fatal; continuing loop)"
        fi
    fi

    log "Sleeping for ${SLEEP_SECONDS} seconds..."
    sleep "$SLEEP_SECONDS"
    log "Sleep period completed, restarting loop"
done
