#!/bin/bash

# Script to perform timed runs of a hardcoded command with 1-hour sleep intervals

# Hardcoded command to run
COMMAND="qwen -y  'read @docs/WIKI_DOCUMENTATION.md, pull repo and wiki-data master branches prefer remote changes, resolve conflicts in wiki-data if necessary, pick few random wiki pages(ensure it follow naming rules, rename|merge|delete if necessary, if already good check same page in other langs (chose two from es, pt, ru, cn) ) and rigorously verify it against codebase (java, lua and json configs) insert references to data sources(code, configs, string resources, but don't overbloat it - no need to mention source file more than once), wikify (create wiki links, make sure to use proper naming scheme and dokuwiki syntax), check for redlinks (create new pages if necessary but only relevant ones, if link lead to irrelevant or improperly named entity fix link instead), check for any obsolete, incorrect or duplicated info and fix it, commit changed files in wiki-data one by one, push changes to wiki-data master branch. Don't create additional scripts or tools on this step, however you cloud document your suggestions on additional tools on ai_wiki wiki page.'"

echo "Will run the command, sleep for 1 hour, and repeat indefinitely."
echo "Press Ctrl+C to stop."
echo

while true; do
    # Print current time and run the command

    git clean -xfd
    git reset --hard

    cd wiki-data || { echo "Failed to change to wiki-data directory"; exit 1; }

    git clean -xfd
    git reset --hard
    git checkout master
    git pull origin master

    cd ..

    echo "[$(date)] Running command: $COMMAND"
    eval 'time $COMMAND'

    # Check exit status of the command
    CMD_STATUS=$?
    if [ $CMD_STATUS -ne 0 ]; then
        echo "[$(date)] Command failed with exit status: $CMD_STATUS"
    else
        echo "[$(date)] Command completed successfully"

        # Change to wiki-data directory
        cd wiki-data || { echo "Failed to change to wiki-data directory"; exit 1; }

        # Run the dokuwiki linter with fix mode
        echo "[$(date)] Running dokuwiki linter with fix mode..."
        python3 ../tools/py-tools/dokuwiki_linter.py . --fix

        # Check for changes in git
        if [[ -n $(git status --porcelain) ]]; then
            echo "[$(date)] Changes detected, committing and pushing..."

            # Add all changes
            git add .

            # Commit with a descriptive message
            git commit -m "Auto-fix: Apply dokuwiki linter fixes

            Automated commit to apply fixes from dokuwiki linter."

            # Push changes to the current branch
            git push origin HEAD

            if [ $? -eq 0 ]; then
                echo "[$(date)] Changes committed and pushed successfully"
            else
                echo "[$(date)] Failed to push changes"
            fi
        else
            echo "[$(date)] No changes detected after linting"
        fi

        # Return to the original directory
        cd ..
    fi

    echo "[$(date)] Sleeping for 1 hour (3600 seconds)..."
    sleep 3600
    echo "[$(date)] Sleep period completed, restarting loop"
done
