#!/usr/bin/env python3
"""Headless Crashlytics access for crash triage rounds.

Auth: firebase-tools OAuth login (done 2026-10-23 as mikhaeldanilov@gmail.com,
scopes include cloud-platform). The refresh token lives in
~/.config/configstore/firebase-tools.json; this tool silently refreshes it and
caches access tokens in /tmp.

API surface reality (verified 2026-10-23):
  - firebase.googleapis.com/v1alpha .../reports/{FATAL,ALL}/issues — RETIRED (404).
  - Console dashboard RPCs (crashlytics-pa.clients6.google.com/$rpc/...) are
    bound to the console's own OAuth client; CLI/user tokens get
    "Crashlytics Private API has not been used in project 563584335869".
  - The supported programmatic path is the Crashlytics -> BigQuery export.
    ONE-TIME SETUP (console, Mike): firebase console -> Integrations ->
    BigQuery -> link, enable Crashlytics export. After that:

      crashlytics.py bq "SELECT ... FROM `30037695623.firebase_crashlytics.*`"

    Tables: <app_id> (e.g. com_nyrds_pixeldungeon_ml_ANDROIDAPP), columns
    crash_info, stack_trace, breadcrumbs, custom_keys, device, eventsLog etc.
  - Until the export is enabled, use the opencli browser recipe: open the
    issue URL in the logged-in console and read the cached
    GetFirebaseSingleIssue / GetProcessedEvent RPC bodies via
    `opencli browser <session> network` (see memory crashlytics-telemetry-access).

Usage:
  crashlytics.py token                # print a fresh access token
  crashlytics.py bq "<standard SQL>"  # run a BigQuery query
"""
import json
import os
import sys
import time
import urllib.request

CONFIG = os.path.expanduser("~/.config/configstore/firebase-tools.json")
CACHE = "/tmp/fb_at.json"
PROJECT = "30037695623"  # admob-app-id-7480901647


def _oauth_client():
    """Public installed-app client credentials, read from the local
    firebase-tools install (lib/api.js) — nothing credential-shaped is
    committed here. Override with FIREBASE_CLIENT_ID/FIREBASE_CLIENT_SECRET."""
    cid, csec = os.environ.get("FIREBASE_CLIENT_ID"), os.environ.get("FIREBASE_CLIENT_SECRET")
    if cid and csec:
        return cid, csec
    import glob
    for api in glob.glob(os.path.expanduser(
            "~/.nvm/versions/node/*/lib/node_modules/firebase-tools/lib/api.js")):
        src = open(api).read()
        import re
        m_id = re.search(r'FIREBASE_CLIENT_ID",\s*"([^"]+)"', src)
        m_sec = re.search(r'FIREBASE_CLIENT_SECRET",\s*"([^"]+)"', src)
        if m_id and m_sec:
            return m_id.group(1), m_sec.group(1)
    raise SystemExit("firebase-tools install not found; set FIREBASE_CLIENT_ID/FIREBASE_CLIENT_SECRET")


def access_token():
    if os.path.exists(CACHE):
        c = json.load(open(CACHE))
        if c.get("expires_at", 0) > time.time() + 60:
            return c["access_token"]
    cfg = json.load(open(CONFIG))
    cid, csec = _oauth_client()
    data = urllib.parse.urlencode({
        "client_id": cid,
        "client_secret": csec,
        "refresh_token": cfg["tokens"]["refresh_token"],
        "grant_type": "refresh_token",
    }).encode()
    tok = json.load(urllib.request.urlopen(urllib.request.Request(
        "https://oauth2.googleapis.com/token", data=data)))
    json.dump({"access_token": tok["access_token"],
               "expires_at": time.time() + tok.get("expires_in", 3600)}, open(CACHE, "w"))
    return tok["access_token"]


def bq(sql):
    body = json.dumps({"query": sql, "useLegacySql": False,
                       "defaultDataset": None}).encode()
    req = urllib.request.Request(
        f"https://bigquery.googleapis.com/bigquery/v2/projects/{PROJECT}/queries",
        data=body, method="POST",
        headers={"Authorization": "Bearer " + access_token(),
                 "Content-Type": "application/json"})
    r = json.load(urllib.request.urlopen(req))
    rows = [[f.get("v") for f in row.get("f", [])] for row in r.get("rows", [])]
    print(json.dumps({"total_rows": r.get("totalRows"), "jobComplete": r.get("jobComplete"),
                      "rows": rows[:50]}, indent=1))


import urllib.parse  # noqa: E402 (used in access_token)

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print(__doc__)
        sys.exit(2)
    if sys.argv[1] == "token":
        print(access_token())
    elif sys.argv[1] == "bq" and len(sys.argv) > 2:
        bq(sys.argv[2])
    else:
        print(__doc__)
        sys.exit(2)
