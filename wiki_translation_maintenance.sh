#!/bin/bash

# Script to perform timed runs for both wiki and translation tasks with 1-hour sleep intervals
# Randomly chooses between wiki maintenance and translation tasks

echo "Maintenance script that randomly chooses between wiki and translation tasks"
echo "Will run a random task, sleep for 1 hour, and repeat indefinitely."
echo "Press Ctrl+C to stop."
echo

while true; do
    # Randomly choose between wiki task (0) and translation task (1)
    TASK_CHOICE=$((RANDOM % 2))

    git clean -xfd
    git reset --hard

    if [ $TASK_CHOICE -eq 0 ]; then
        # Wiki task
        echo "[$(date)] Running wiki maintenance command"
        COMMAND="qwen -y 'read @docs/WIKI_DOCUMENTATION.md, pull repo master, pick 5 random wiki pages using tools/pick_random_wiki_pages.sh, analyze them for compliance with wiki standards, identify issues like missing images, invalid headers, incorrect links, improper formatting, run dokuwiki linter on them, fix identified issues based on documentation standards, verify all links point to existing lowercase files, ensure proper image references exist, for mr: namespace pages use tools/find_entity_usage.py to analyze entity implementation and usage in code, also check wiki pages in all supported languages (en, ru, es, fr, de, it, pl, pt-rBR, ja, ko, zh-rCN, zh-rTW, uk, hu, tr, el, in, ms) to ensure consistency with game translations, commit your changes to wiki-data and push it, don't commit or push into main repo. Focus on maintaining consistency with wiki documentation standards.'"
        TASK_NAME="Wiki maintenance"
    else
        # Translation task
        COMMAND="qwen -y 'read @docs/TRANSLATION_TASK.md, pull repo master, identify few random missing strings in random languages (use tools/select_random_missing_string.py), find their context using tools/find_string_usage.py, translate them properly based on English reference and code context, add translations to appropriate strings_all.xml files using tools/insert_translated_string.py, verify consistency with existing translations, run tools/validate_translations.py to check for formatting issues, also select few random strings and ensure consistency among all languages, commit your changes, push it. Focus on maintaining consistency with existing translations, proper grammar, cultural appropriateness for target languages, and proper string formatting following Android XML standards.'"
        TASK_NAME="Translation task"
    fi

    echo "[$(date)] Running $TASK_NAME: $COMMAND"
    eval 'timeout 30m time $COMMAND'

    # Check exit status of the command
    CMD_STATUS=$?
    if [ $CMD_STATUS -ne 0 ]; then
        echo "[$(date)] Command failed with exit status: $CMD_STATUS"
    else
        echo "[$(date)] $TASK_NAME completed successfully"

        # Check for changes in git
        if [[ -n $(git status --porcelain) ]]; then
            echo "[$(date)] Changes detected, committing..."

            if [ $TASK_CHOICE -eq 0 ]; then
                # Wiki task - commit wiki changes
                git add wiki-data/
                
                # Also add any code changes that might have been made during wiki analysis
                git add RemixedDungeon/src/main/java/ 2>/dev/null || true
                git add tools/py-tools/ 2>/dev/null || true
                git add docs/ 2>/dev/null || true
                
                COMMIT_MSG="Auto-wiki: Update wiki pages based on maintenance iteration"
            else
                # Translation task - commit translation changes
                git add RemixedDungeon/src/main/res/values-*/strings_all.xml

                COMMIT_MSG="Auto-translation: Add missing string translations"
            fi

            # Commit with a descriptive message
            git commit -m "$COMMIT_MSG

            Automated commit to $([ $TASK_CHOICE -eq 0 ] && echo 'update wiki pages' || echo 'add missing translations') identified during iteration."

            # Push changes to the current branch
            git push origin HEAD

            if [ $? -eq 0 ]; then
                echo "[$(date)] $TASK_NAME changes committed and pushed successfully"
            else
                echo "[$(date)] Failed to push $([ $TASK_CHOICE -eq 0 ] && echo 'wiki' || echo 'translation') changes"
            fi
        else
            echo "[$(date)] No changes detected after $([ $TASK_CHOICE -eq 0 ] && echo 'wiki' || echo 'translation') iteration"
        fi
    fi

    echo "[$(date)] Sleeping for 1 hour (3600 seconds)..."
    sleep 3600
    echo "[$(date)] Sleep period completed, restarting loop"
done