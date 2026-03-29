#!/usr/bin/env python3
"""
Real-time log file monitor with test context association.

Usage:
    from log_monitor import LogMonitor

    monitor = LogMonitor("/tmp/game.log")
    monitor.start()

    monitor.set_test("test_name")
    # ... run test ...
    errors = monitor.get_test_errors("test_name")

    monitor.stop()
"""

import os
import re
import time
import threading
from typing import Optional, List, Dict
from collections import defaultdict


class LogMonitor:
    """Monitors log file in real-time and associates logs with tests."""

    def __init__(self, log_file: str):
        self.log_file = log_file
        self.current_test: Optional[str] = None
        self.running = False
        self.thread: Optional[threading.Thread] = None
        self.position = 0
        # Store logs by test name
        self.test_logs: Dict[str, List[str]] = defaultdict(list)
        self.all_logs: List[str] = []
        # Patterns to highlight
        self.error_patterns = re.compile(r'\b(error|exception|fail|crash)\b', re.IGNORECASE)
        self.warning_patterns = re.compile(r'\b(warn)\b', re.IGNORECASE)
        # Filter patterns (noise to ignore)
        self.filter_patterns = re.compile(r'^(>|Downloading|Fetching|Configuration cache|BUILD)', re.IGNORECASE)

    def start(self):
        """Start monitoring in background thread."""
        self.running = True
        self.thread = threading.Thread(target=self._monitor_loop, daemon=True)
        self.thread.start()

    def stop(self):
        """Stop monitoring."""
        self.running = False
        if self.thread:
            self.thread.join(timeout=2)

    def set_test(self, test_name: Optional[str]):
        """Set the current test being executed."""
        self.current_test = test_name

    def _should_show(self, line: str) -> bool:
        """Check if line should be displayed."""
        if not line.strip():
            return False
        if self.filter_patterns.match(line):
            return False
        return bool(self.error_patterns.search(line) or self.warning_patterns.search(line))

    def _classify_line(self, line: str) -> str:
        """Classify line as error, warning, or info."""
        if self.error_patterns.search(line):
            return "ERROR"
        if self.warning_patterns.search(line):
            return "WARN"
        return "INFO"

    def _monitor_loop(self):
        """Background loop that reads new log lines."""
        while self.running:
            try:
                self._read_new_lines()
            except Exception:
                pass
            time.sleep(0.2)

    def _read_new_lines(self):
        """Read new lines from log file."""
        if not os.path.exists(self.log_file):
            return
        with open(self.log_file, 'r') as f:
            f.seek(self.position)
            new_lines = f.readlines()
            self.position = f.tell()
        for line in new_lines:
            line = line.rstrip()
            if not line:
                continue
            self.all_logs.append(line)
            if self.current_test:
                self.test_logs[self.current_test].append(line)
            # Print interesting lines in real-time
            if self._should_show(line):
                level = self._classify_line(line)
                prefix = f"[{self.current_test or 'STARTUP'}]"
                # Truncate long lines
                display = line[:200] if len(line) > 200 else line
                if level == "ERROR":
                    print(f"  {prefix} \033[91m{display}\033[0m", flush=True)
                elif level == "WARN":
                    print(f"  {prefix} \033[93m{display}\033[0m", flush=True)

    def get_test_errors(self, test_name: str) -> List[str]:
        """Get error lines for a specific test."""
        return [line for line in self.test_logs.get(test_name, [])
                if self.error_patterns.search(line)]

    def get_test_warnings(self, test_name: str) -> List[str]:
        """Get warning lines for a specific test."""
        return [line for line in self.test_logs.get(test_name, [])
                if self.warning_patterns.search(line)]

    def get_all_errors(self) -> List[str]:
        """Get all error lines."""
        return [line for line in self.all_logs if self.error_patterns.search(line)]

    def get_all_warnings(self) -> List[str]:
        """Get all warning lines."""
        return [line for line in self.all_logs if self.warning_patterns.search(line)]
