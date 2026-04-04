#!/usr/bin/env python3
"""
Shared server management for HTTP API tests.

Provides a ServerManager class that handles starting/stopping the game server
with consistent behavior across all test files.

Usage:
    from test_server import ServerManager

    manager = ServerManager()
    if args.start_server:
        manager.start()
    try:
        # run tests
    finally:
        if args.start_server:
            manager.stop()
"""

import os
import sys
import time
import signal
import subprocess
import tempfile
from typing import Optional

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from game_client import GameClient
from log_monitor import LogMonitor


class ServerManager:
    """Manages game server lifecycle for tests."""

    def __init__(
        self, host: str = "localhost", port: int = 8080, test_prefix: str = "test"
    ):
        self.host = host
        self.port = port
        self.client = GameClient(host, port)
        self.server_process: Optional[subprocess.Popen] = None
        self.log_file: Optional[str] = None
        self.log_monitor: Optional[LogMonitor] = None
        self.test_prefix = test_prefix

    def start(self) -> bool:
        """Start the game server and wait for it to be ready."""
        print("=" * 60)
        print("STARTING GAME SERVER")
        print("=" * 60)

        script_dir = os.path.dirname(os.path.abspath(__file__))
        project_root = os.path.dirname(os.path.dirname(script_dir))
        self.log_file = tempfile.mktemp(prefix=f"{self.test_prefix}_", suffix=".log")
        print(f"Log file: {self.log_file}")

        cmd = [
            "./gradlew",
            "-p",
            "RemixedDungeonDesktop",
            "runDesktopGameWithWebServer",
            f"--args=--webserver={self.port} --minimized",
        ]
        log_handle = open(self.log_file, "w")
        self.server_process = subprocess.Popen(
            cmd,
            cwd=project_root,
            stdout=log_handle,
            stderr=subprocess.STDOUT,
            preexec_fn=os.setsid,
        )
        self.log_monitor = LogMonitor(self.log_file)
        self.log_monitor.start()

        print("Waiting for server...", end="", flush=True)
        max_wait = 240
        start_time = time.time()
        while time.time() - start_time < max_wait:
            if self.client.check_server():
                print(" READY!")
                return True
            print(".", end="", flush=True)
            time.sleep(2)
        print(" TIMEOUT!")
        return False

    def stop(self):
        """Stop the game server."""
        print("=" * 60)
        print("STOPPING GAME SERVER")
        print("=" * 60)
        if self.log_monitor:
            self.log_monitor.stop()
        if self.server_process:
            try:
                os.killpg(os.getpgid(self.server_process.pid), signal.SIGTERM)
                self.server_process.wait(timeout=5)
            except (subprocess.TimeoutExpired, ProcessLookupError):
                try:
                    os.killpg(os.getpgid(self.server_process.pid), signal.SIGKILL)
                except ProcessLookupError:
                    pass
            time.sleep(1)
        print("Server stopped")

    def set_test(self, name: Optional[str]):
        """Set the current test name for log monitoring."""
        if self.log_monitor:
            self.log_monitor.set_test(name)

    def get_test_errors(self, test_name: str) -> list:
        """Get errors for a specific test."""
        if self.log_monitor:
            return self.log_monitor.get_test_errors(test_name)
        return []

    def get_all_errors(self) -> list:
        """Get all errors from the log."""
        if self.log_monitor:
            return self.log_monitor.get_all_errors()
        return []

    def check_lua_errors(self, test_name: str) -> bool:
        """Check for Lua errors in the current test."""
        errors = self.get_test_errors(test_name)
        for err in errors:
            if "LuaError" in err or "attempt to call nil" in err:
                print(f"    LuaError detected: {err[:150]}")
                return False
        return True

    def print_log_summary(self):
        """Print a summary of log errors."""
        if not self.log_monitor:
            return
        all_errors = self.get_all_errors()
        lua_errors = [
            e for e in all_errors if "LuaError" in e or "attempt to call nil" in e
        ]
        if lua_errors:
            print(f"\n  Lua errors found in logs:")
            for err in lua_errors[:5]:
                print(f"    {err[:150]}")
