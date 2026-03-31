#!/usr/bin/env python3
"""
Test script for level navigation debug API endpoints.

This script:
1. Starts the game server
2. Runs tests for level navigation endpoints
3. Shuts down the server
4. Analyzes game logs for errors
"""

import subprocess
import sys
import time
import os
import signal
import argparse
import tempfile
from typing import Optional, List

# Add parent directory to path for imports
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from game_client import GameClient
from log_monitor import LogMonitor


class TestRunner:
    def __init__(self, host: str = "localhost", port: int = 8080):
        self.host = host
        self.port = port
        self.client = GameClient(host, port)
        self.server_process: Optional[subprocess.Popen] = None
        self.log_file: Optional[str] = None
        self.log_monitor: Optional[LogMonitor] = None
        self.errors: List[str] = []
        self.warnings: List[str] = []
        self.passed = 0
        self.failed = 0

    def start_server(self) -> bool:
        """Start the game server."""
        print("=" * 60)
        print("STARTING GAME SERVER")
        print("=" * 60)
        # Get project root directory
        script_dir = os.path.dirname(os.path.abspath(__file__))
        project_root = os.path.dirname(os.path.dirname(script_dir))
        # Create log file for capturing game output
        self.log_file = tempfile.mktemp(prefix="game_test_", suffix=".log")
        print(f"Log file: {self.log_file}")
        # Start the server
        cmd = [
            "./gradlew",
            "-p", "RemixedDungeonDesktop",
            "runDesktopGameWithWebServer",
            "--args=--webserver=8080 --windowed"
        ]
        print(f"Running: {' '.join(cmd)}")
        print(f"Working directory: {project_root}")
        # Open log file for writing
        log_handle = open(self.log_file, 'w')
        self.server_process = subprocess.Popen(
            cmd,
            cwd=project_root,
            stdout=log_handle,
            stderr=subprocess.STDOUT,  # Merge stderr into stdout
            preexec_fn=os.setsid
        )
        # Start log monitor
        self.log_monitor = LogMonitor(self.log_file)
        self.log_monitor.start()
        # Wait for server to be ready
        print("Waiting for server to start...", end="", flush=True)
        max_wait = 240  # 4 minutes (gradle build + game init takes time)
        check_interval = 3
        start_time = time.time()
        dots_printed = 0
        while time.time() - start_time < max_wait:
            if self.client.check_server():
                print(" READY!")
                return True
            print(".", end="", flush=True)
            time.sleep(2)
        print(" TIMEOUT!")
        return False

    def stop_server(self):
        """Stop the game server."""
        print()
        print("=" * 60)
        print("STOPPING GAME SERVER")
        print("=" * 60)
        # Stop log monitor first
        if self.log_monitor:
            self.log_monitor.stop()
            self.log_monitor = None
        if self.server_process:
            try:
                os.killpg(os.getpgid(self.server_process.pid), signal.SIGTERM)
                self.server_process.wait(timeout=5)
                print("Server stopped gracefully")
            except subprocess.TimeoutExpired:
                os.killpg(os.getpgid(self.server_process.pid), signal.SIGKILL)
                print("Server killed (timeout)")
            except ProcessLookupError:
                pass
            # Give time for logs to flush
            time.sleep(1)

    def _run_test(self, name: str, test_func, verbose: bool = False) -> bool:
        """Run a test and return success."""
        if self.log_monitor:
            self.log_monitor.set_test(name)
        if verbose:
            print(f"  Running: {name}...")
        try:
            result = test_func()
            if "error" in result and result.get("success") is not True:
                self.failed += 1
                print(f"    FAIL: {name} - {result.get('error', 'Unknown error')}")
                # Check for related log errors
                if self.log_monitor:
                    errors = self.log_monitor.get_test_errors(name)
                    if errors:
                        print(f"    Related log errors:")
                        for err in errors[:3]:
                            print(f"      {err[:150]}")
                return False
            self.passed += 1
            if verbose:
                print(f"    PASS: {name}")
            time.sleep(0.5)
            return True
        except Exception as e:
            self.failed += 1
            print(f"    FAIL: {name} - Exception: {e}")
            return False
        finally:
            if self.log_monitor:
                self.log_monitor.set_test(None)

    def _collect_logs(self):
        """Collect recent logs from server API."""
        try:
            logs = self.client.get_recent_logs()
            if "error" in logs:
                return
            if "logs" in logs and isinstance(logs["logs"], list):
                for entry in logs["logs"]:
                    entry_lower = entry.lower()
                    if "error" in entry_lower or "exception" in entry_lower:
                        if "test" not in entry_lower and "debug" not in entry_lower:
                            self.errors.append(f"[API] {entry}")
                    elif "warning" in entry_lower:
                        if "test" not in entry_lower and "debug" not in entry_lower:
                            self.warnings.append(f"[API] {entry}")
        except Exception:
            pass

    def _collect_stdout_logs(self):
        """Collect and analyze stdout/stderr from log file."""
        # If we have a log monitor, use its collected data
        if self.log_monitor:
            for test_name, logs in self.log_monitor.test_logs.items():
                errors = self.log_monitor.get_test_errors(test_name)
                warnings = self.log_monitor.get_test_warnings(test_name)
                for err in errors:
                    self.errors.append(f"[{test_name}] {err}")
                for warn in warnings:
                    self.warnings.append(f"[{test_name}] {warn}")
            return

        # Fallback: read from log file
        if not self.log_file or not os.path.exists(self.log_file):
            return
        try:
            with open(self.log_file, 'r') as f:
                lines = f.readlines()
            for line in lines:
                line = line.strip()
                if not line:
                    continue
                line_lower = line.lower()
                # Filter out common noise
                if "downloading" in line_lower or "fetching" in line_lower:
                    continue
                if line_lower.startswith(">") or line_lower.startswith("["):
                    continue
                if "error" in line_lower or "exception" in line_lower:
                    if "test" not in line_lower:
                        self.errors.append(f"[STDOUT] {line}")
                elif "warning" in line_lower:
                    if "test" not in line_lower:
                        self.warnings.append(f"[STDOUT] {line}")
        except Exception as e:
            print(f"Warning: Could not read log file: {e}")

    def _print_log_summary(self, print_full_log: bool = False):
        """Print log summary."""
        print()
        print("=" * 60)
        print("LOG SUMMARY")
        print("=" * 60)
        print(f"  Errors: {len(self.errors)}")
        print(f"  Warnings: {len(self.warnings)}")
        if self.log_file:
            print(f"  Log file: {self.log_file}")
        if self.errors:
            print("\nERRORS:")
            for err in self.errors[:10]:
                print(f"  - {err}")
        if self.warnings:
            print("\nWARNINGS:")
            for warn in self.warnings[:10]:
                print(f"  - {warn}")
        if print_full_log and self.log_file and os.path.exists(self.log_file):
            print("\n" + "=" * 60)
            print("FULL LOG OUTPUT")
            print("=" * 60)
            try:
                with open(self.log_file, 'r') as f:
                    # Print last 100 lines
                    lines = f.readlines()
                    for line in lines[-100:]:
                        print(line.rstrip())
            except Exception as e:
                print(f"Could not read log file: {e}")

    def run_all_tests(self, hero_classes: List[str], verbose: bool = False) -> int:
        """Run all tests for the specified hero classes."""
        for hero_class in hero_classes:
            print(f"\n{'=' * 60}")
            print(f"Testing hero class: {hero_class}")
            print("=" * 60)
            # Start game
            result = self.client.start_game(hero_class)
            if not result.get("success"):
                print(f"  FAIL: start_game - {result.get('error')}")
                continue
            print(f"  PASS: start_game ({hero_class})")
            time.sleep(2)
            # Test list levels
            self._run_test("list_levels", self.client.list_levels, verbose)
            # Test go to level
            self._run_test("go_to_level '5'", lambda: self.client.go_to_level("5"), verbose)
            time.sleep(1)
            # Test get exits
            self._run_test("get_exits", self.client.get_exits, verbose)
            # Test descend_to
            exits = self.client.get_exits()
            if "exits" in exits and exits["exits"]:
                target = exits["exits"][0]["id"]
                self._run_test(f"descend_to '{target}'", lambda: self.client.descend_to(target), verbose)
                time.sleep(1)
            # Test get entrances
            self._run_test("get_entrances", self.client.get_entrances, verbose)
            # Test ascend
            entrances = self.client.get_entrances()
            if "entrances" in entrances and entrances["entrances"]:
                self._run_test("ascend", self.client.ascend, verbose)
                time.sleep(1)
            # Test hero position
            self._run_test("get_hero_position", self.client.get_hero_position, verbose)
            # Test mob positions
            self._run_test("get_mob_positions", self.client.get_mob_positions, verbose)
            # Test level info
            self._run_test("get_level_info", self.client.get_level_info, verbose)

        print()
        print("=" * 60)
        print("TEST SUMMARY")
        print("=" * 60)
        print(f"  Passed: {self.passed}")
        print(f"  Failed: {self.failed}")
        if self.failed == 0:
            print("\nAll tests passed!")
        else:
            print(f"\n{self.failed} test(s) failed.")
        # Collect and analyze logs
        self._collect_logs()
        self._collect_stdout_logs()
        self._print_log_summary()
        return 1 if self.failed > 0 else 0


    def run(self, hero_classes: List[str], verbose: bool = False, full_log: bool = False):
        """Main entry point."""
        try:
            if not self.start_server():
                return 1
            self.run_all_tests(hero_classes, verbose)
        finally:
            self.stop_server()
            # Print full log after server is stopped
            if full_log:
                self._print_log_summary(print_full_log=True)
        return 0 if self.failed > 0 else 0


def main():
    parser = argparse.ArgumentParser(description="Run level navigation debug API tests")
    parser.add_argument("--host", default="localhost", help="Server host")
    parser.add_argument("--port", type=int, default=8080, help="Server port")
    parser.add_argument("--hero", default="WARRIOR", help="Hero class to test")
    parser.add_argument("--verbose", "-v", action="store_true", help="Verbose output")
    parser.add_argument("--full-log", action="store_true", help="Print full log output at end")
    args = parser.parse_args()
    runner = TestRunner(args.host, args.port)
    exit_code = runner.run([args.hero], args.verbose, args.full_log)
    sys.exit(exit_code)


if __name__ == "__main__":
    main()
