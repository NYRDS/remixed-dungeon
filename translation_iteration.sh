#!/bin/bash

# Script to perform timed runs for translation tasks with 1-hour sleep intervals

# Hardcoded command to run
COMMAND="qwen -y 'read @docs/TRANSLATION_TASK.md, pull repo master, identify few random missing strings in random languages (use tools/select_random_missing_string.py), find their context using tools/find_string_usage.py, translate them properly based on English reference and code context, add translations to appropriate strings_all.xml files, verify consistency with existing translations, also select few random strings and ensure it consistency among all languages, commit your changes, push it. Focus on maintaining consistency with existing translations, proper grammar, and cultural appropriateness for target languages.'"

echo "Will run the translation command, sleep for 1 hour, and repeat indefinitely."
echo "Press Ctrl+C to stop."
echo

while true; do
    # Print current time and run the command

    git clean -xfd
    git reset --hard

    echo "[$(date)] Running translation command: $COMMAND"
    eval 'time $COMMAND'

    # Check exit status of the command
    CMD_STATUS=$?
    if [ $CMD_STATUS -ne 0 ]; then
        echo "[$(date)] Command failed with exit status: $CMD_STATUS"
    else
        echo "[$(date)] Translation command completed successfully"
        
        # Check for changes in git
        if [[ -n $(git status --porcelain) ]]; then
            echo "[$(date)] Changes detected, committing..."

            # Add all changes to localization files
            git add RemixedDungeon/src/main/res/values-*/strings_all.xml

            # Commit with a descriptive message
            git commit -m "Auto-translation: Add missing string translations

            Automated commit to add missing translations identified during iteration."

            # Push changes to the current branch
            git push origin HEAD

            if [ $? -eq 0 ]; then
                echo "[$(date)] Translation changes committed and pushed successfully"
            else
                echo "[$(date)] Failed to push translation changes"
            fi
        else
            echo "[$(date)] No changes detected after translation iteration"
        fi
    fi

    echo "[$(date)] Sleeping for 1 hour (3600 seconds)..."
    sleep 3600
    echo "[$(date)] Sleep period completed, restarting loop"
done