#!/bin/bash

# wiki_translation_maintenance_hermes.sh — Hermes variant of wiki_translation_maintenance.sh
# Same loop as the qwen-based original, but runs tasks via `hermes chat -q` (non-interactive).
#
# Differences from the qwen version:
#   - Uses hermes chat -q (single query, non-interactive) with --yolo for unattended runs
#   - Prompts passed via --query-file to avoid shell-quoting issues with nested quotes
#   - Wall-clock budget (--run-budget 18000 = 5h) replaces `timeout 300m`
#   - Toolsets restricted to what the tasks need
#   - Language list includes recently added locales (nl, vi, ar, he)

set -u

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROMPT_DIR="$(mktemp -d /tmp/hermes_maint.XXXXXX)"
trap 'rm -rf "$PROMPT_DIR"' EXIT

RUN_BUDGET="${RUN_BUDGET:-18000}"   # seconds per task run (default 5h)
TOOLSETS="${TOOLSETS:-terminal,file,web,delegation}"
SLEEP_SECONDS="${SLEEP_SECONDS:-3600}"
LOG_FILE="${LOG_FILE:-$REPO_ROOT/hermes_maintenance.log}"

WIKI_LANGS="en, ru, es, fr, de, it, pl, pt-rBR, ja, ko, zh-rCN, zh-rTW, uk, hu, tr, el, in, ms, nl, vi, ar, he"

cat > "$PROMPT_DIR/wiki.txt" <<EOF
Read @docs/WIKI_DOCUMENTATION.md, pull repo master, pick 5 random wiki pages using ./pick_random_wiki_pages.sh (script lives in the repo root, not tools/), analyze them for compliance with wiki standards, identify issues like missing images, invalid headers, incorrect links, improper formatting, run the dokuwiki linter on them (python3 tools/py-tools/dokuwiki_linter.py), fix identified issues based on documentation standards, verify all links point to existing lowercase files, ensure proper image references exist. For mr: namespace pages and whenever entity facts (stats, mechanics, drops, behavior) are changed on any page, use tools/find_entity_usage.py to ground changes in entity implementation and usage in code. Also check wiki pages in all supported languages (${WIKI_LANGS}) to ensure consistency with game translations. Commit your changes to wiki-data and push it; don't commit or push into the main repo beyond what the task requires. Focus on maintaining consistency with wiki documentation standards.
EOF

cat > "$PROMPT_DIR/translation.txt" <<EOF
Read @docs/TRANSLATION_TASK.md, pull repo master, identify a few random missing strings in random languages (use tools/select_random_missing_string.py — note that nl and vi currently have large gaps), find their context using tools/find_string_usage.py, translate them properly based on the English reference and code context, add translations to the appropriate strings_all.xml files using tools/insert_translated_string.py, verify consistency with existing translations, run tools/validate_translations.py --auto-fix before committing, also select a few random strings and ensure consistency among all languages. Commit your changes and push. Focus on maintaining consistency with existing translations, proper grammar, cultural appropriateness for target languages, and proper string formatting following Android XML standards.
EOF

run_task() {
    local prompt_file="$1"
    hermes chat -q \
        --query-file "$prompt_file" \
        --in "$REPO_ROOT" \
        --toolsets "$TOOLSETS" \
        --run-budget "$RUN_BUDGET" \
        --yolo \
        --quiet
}

log() {
    echo "[$(date)] $*" | tee -a "$LOG_FILE"
}

echo "Hermes maintenance script that randomly chooses between wiki and translation tasks"
echo "Run budget per task: ${RUN_BUDGET}s, sleep between iterations: ${SLEEP_SECONDS}s"
echo "Log file: $LOG_FILE"
echo "Press Ctrl+C to stop."
echo

while true; do
    TASK_CHOICE=$((RANDOM % 2))

    git -C "$REPO_ROOT" clean -xfdq
    git -C "$REPO_ROOT" reset --hard -q

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

    if [ $CMD_STATUS -ne 0 ]; then
        log "$TASK_NAME failed with exit status: $CMD_STATUS"
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

    log "Sleeping for ${SLEEP_SECONDS} seconds..."
    sleep "$SLEEP_SECONDS"
    log "Sleep period completed, restarting loop"
done
