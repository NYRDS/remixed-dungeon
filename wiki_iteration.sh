#!/bin/bash

# Script to perform timed runs of a hardcoded command with 1-hour sleep intervals

# Hardcoded command to run
COMMAND="qwen -y  'read @docs/WIKI_DOCUMENTATION.md, pull repo and wiki data, resolve conflicts in wiki-data if necessary, check if any mr: pages missing if so create one or verify existing one, pick few random wiki pages and rigorously verify it against codebase (java, lua and json configs) insert references to data sources(code, configs, string resources), wikify (create wiki links, make sure to use proper naming scheme and dokuwiki syntax), check for redlinks and create new pages if necessary, check for any obsolete, incorrect or duplicated info and fix it, commit changed files in wiki-data one by one, push changes to wiki-data.'"

echo "Starting timed runner for command: $COMMAND"
echo "Will run the command, sleep for 1 hour, and repeat indefinitely."
echo "Press Ctrl+C to stop."
echo

while true; do
    # Print current time and run the command
    echo "[$(date)] Running command: $COMMAND"
    eval "time $COMMAND"

    # Check exit status of the command
    CMD_STATUS=$?
    if [ $CMD_STATUS -ne 0 ]; then
        echo "[$(date)] Command failed with exit status: $CMD_STATUS"
    else
        echo "[$(date)] Command completed successfully"
    fi

    echo "[$(date)] Sleeping for 1 hour (3600 seconds)..."
    sleep 3600
    echo "[$(date)] Sleep period completed, restarting loop"
done