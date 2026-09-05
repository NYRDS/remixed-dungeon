#!/usr/bin/env python3
"""
Static file server for mob-viewer development with caching disabled.

python3 -m http.server heuristically caches in the browser (no Cache-Control
header), so sprite/desc edits appear stale after a normal reload. This
server sends `Cache-Control: no-store` on every response.

Usage: python3 mob-viewer/serve.py [port]   (default 8099, serves repo root)
"""
import http.server
import sys


class NoCacheHandler(http.server.SimpleHTTPRequestHandler):
    def end_headers(self):
        self.send_header("Cache-Control", "no-store, must-revalidate")
        self.send_header("Pragma", "no-cache")
        self.send_header("Expires", "0")
        super().end_headers()


if __name__ == "__main__":
    port = int(sys.argv[1]) if len(sys.argv) > 1 else 8099
    server = http.server.ThreadingHTTPServer(
        ("", port), NoCacheHandler)
    print(f"serving repo root at http://localhost:{port}/ (no-cache)")
    server.serve_forever()
