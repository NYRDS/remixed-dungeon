#!/usr/bin/env python3
"""Serve the assembled webapp dir with no-store caching headers.

python3 make_webapp.py produces build/webapp; run:
  python3 serve.py [--dir build/webapp] [--port 8081]
"""
import argparse
import http.server
import os

HERE = os.path.dirname(os.path.abspath(__file__))


class NoStoreHandler(http.server.SimpleHTTPRequestHandler):
    def end_headers(self):
        if self.path.startswith("/fonts/"):
            # the lazy CJK fallback font is big; let the browser cache it
            self.send_header("Cache-Control", "public, max-age=86400")
        else:
            self.send_header("Cache-Control", "no-store, must-revalidate")
            self.send_header("Pragma", "no-cache")
            self.send_header("Expires", "0")
        super().end_headers()


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--dir", default=os.path.join(HERE, "build", "webapp"))
    parser.add_argument("--port", type=int, default=8081)
    args = parser.parse_args()
    webroot = os.path.abspath(args.dir)
    if not os.path.isfile(os.path.join(webroot, "index.html")):
        raise SystemExit("no index.html under %s - run make_webapp.py first" % webroot)
    handler = lambda *h_args, **kw: NoStoreHandler(*h_args, directory=webroot, **kw)
    server = http.server.ThreadingHTTPServer(("127.0.0.1", args.port), handler)
    print("serving %s at http://127.0.0.1:%d" % (webroot, args.port))
    server.serve_forever()


if __name__ == "__main__":
    main()
