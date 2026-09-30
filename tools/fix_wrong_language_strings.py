#!/usr/bin/env python3
"""Detect and repair wrong-language strings in res/values-*/strings_all.xml.

Wrong-language fills land there when an automated translation sync maps a
translation to the wrong locale (e.g. Indonesian text in values-ko). Because
the locale file is still complete, Android never falls back to the English
default and players see a third language.

Repair strategy per contaminated entry:
  - restore the most recent historical value from git history that passes the
    locale's language test, or
  - drop the entry entirely so resolution falls back to values/ (English),
    when no good historical value exists.

Usage:
  fix_wrong_language_strings.py --dry-run [--lang ko]   # report only (default)
  fix_wrong_language_strings.py --apply    [--lang ko]   # rewrite files
"""
import argparse
import html
import re
import subprocess
import sys
from collections import OrderedDict

RES = "RemixedDungeon/src/main/res"

SCRIPT_TESTS = {
    "ko": re.compile("[\uAC00-\uD7A3]"),            # Hangul
    "ja": re.compile("[\u3040-\u30FF\u4E00-\u9FFF]"),  # Kana / Han
    "zh-rCN": re.compile("[\u4E00-\u9FFF]"),
    "zh-rTW": re.compile("[\u4E00-\u9FFF]"),
    "ru": re.compile("[\u0400-\u04FF]"),             # Cyrillic
    "uk": re.compile("[\u0400-\u04FF]"),
    "el": re.compile("[\u0370-\u03FF]"),             # Greek
    "he": re.compile("[\u0590-\u05FF]"),
    "ar": re.compile("[\u0600-\u06FF]"),
}
# Locales using Latin script: contamination detected via identical-to-Indonesian
LATIN_LOCALES = ["de", "es", "fr", "hu", "it", "nl", "pl", "pt-rBR", "tr", "vi"]
# ms/in are the Indonesian family itself - never treat matching as contamination.
SKIP_LOCALES = ["ms", "in"]

INDO_WORDS = re.compile(
    r"\b(yang|dengan|untuk|adalah|akan|tidak|bisa|dari|pada|saat|jika|oleh|lebih|"
    r"sangat|menjadi|memberikan|Kirim|Selamat|harus|Tanpa|dapat|Mereka|permainan|"
    r"kerusakan|serangan|musuh|Petualang|Pembedahan|Meleset|Dukung|Kaustik)\b")

FORMAT_ONLY = re.compile(r"[%\s\d\$\.,:;/()+'\-A-Za-z]*")
ENTRY_RE = re.compile(r'^(\s*)<string name="([^"]+)">(.*?)</string>\s*$')

# Entries that legitimately carry no target-language text.
WHITELIST_RE = re.compile(
    r"^(AboutScene_Translation_Names|Welcome_Text_\w+|.*_Gender)$")
# Locale-neutral tokens ("MOD", "OK") and letter-free values (punctuation,
# formats) are not wrong-language contamination.
NEUTRAL_TOKEN = re.compile(r"^[A-Z]{1,4}$")
HAS_LETTER = re.compile(r"[^\W\d_]", re.UNICODE)


def git(*args):
    return subprocess.run(["git", *args], capture_output=True, text=True,
                          check=True).stdout


def parse_file(path):
    entries = OrderedDict()
    with open(path, encoding="utf-8") as f:
        for line in f:
            m = ENTRY_RE.match(line.rstrip("\n"))
            if m:
                entries[m.group(2)] = m.group(3)
    return entries


def has_indo_word(value):
    plain = html.unescape(html.unescape(value))
    return bool(INDO_WORDS.search(plain))


class LocaleFixer:
    def __init__(self, locale, all_locale_values):
        self.locale = locale
        self.path = f"{RES}/values-{locale}/strings_all.xml"
        self.script = SCRIPT_TESTS.get(locale)
        self.others = {loc: vals for loc, vals in all_locale_values.items()
                       if loc != locale}

    def looks_right(self, value):
        if self.script is None:
            return None  # cannot judge Latin-script locales by script
        return bool(self.script.search(html.unescape(value)))

    def is_contaminated(self, key, value, en_value):
        if WHITELIST_RE.match(key) or value == en_value:
            return False
        if not value.strip() or not HAS_LETTER.search(value):
            return False
        if NEUTRAL_TOKEN.match(value.strip()):
            return False
        if self.script is not None:
            if self.looks_right(value):
                return False
            matches_other = any(value == vals.get(key)
                                for vals in self.others.values())
            return has_indo_word(value) or matches_other
        # Latin-script locale: require an exact match against the Indonesian
        # file plus a distinctive Indonesian word - keeps false positives out.
        return (self.others.get("in", {}).get(key) == value
                and has_indo_word(value))

    def historical_candidates(self, key):
        hashes = git("log", "--format=%H", "--follow", "--", self.path).split()
        for h in hashes:
            try:
                blob = git("show", f"{h}:{self.path}")
            except subprocess.CalledProcessError:
                continue
            for line in blob.splitlines():
                m = ENTRY_RE.match(line)
                if m and m.group(2) == key:
                    yield m.group(3)

    def find_restore(self, key, bad_value, en_value):
        for value in self.historical_candidates(key):  # newest first
            if value == bad_value or value == en_value:
                continue
            if self.script is not None:
                if self.looks_right(value):
                    return value
            elif not has_indo_word(value):
                return value
        return None


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--apply", action="store_true",
                    help="rewrite files (default: dry-run report)")
    ap.add_argument("--lang", help="single locale, e.g. ko (default: all)")
    args = ap.parse_args()

    en = parse_file(f"{RES}/values/strings_all.xml")

    import os
    all_locales = sorted(d[len("values-"):] for d in os.listdir(RES)
                         if d.startswith("values-"))
    all_locale_values = {loc: parse_file(f"{RES}/values-{loc}/strings_all.xml")
                         for loc in all_locales}

    locales = ([args.lang] if args.lang else
               [l for l in all_locales if l not in SKIP_LOCALES])

    total_fixed = total_dropped = 0
    for loc in locales:
        fixer = LocaleFixer(loc, all_locale_values)
        cur = all_locale_values[loc]
        bad = {k: v for k, v in cur.items()
               if fixer.is_contaminated(k, v, en.get(k, "\0"))}
        if not bad:
            continue
        restores, drops = {}, []
        for key, value in bad.items():
            r = fixer.find_restore(key, value, en.get(key, "\0"))
            if r is not None:
                restores[key] = r
            else:
                drops.append(key)
        print(f"\n== {loc}: {len(bad)} contaminated "
              f"({len(restores)} restore, {len(drops)} drop-to-English)")
        for key in bad:
            if key in restores:
                print(f"  restore {key}")
                print(f"    -  {bad[key][:70]}")
                print(f"    +  {restores[key][:70]}")
            else:
                print(f"  drop    {key}: {bad[key][:60]}")
        total_fixed += len(restores)
        total_dropped += len(drops)

        if args.apply:
            out, changed = [], False
            drop_set = set(drops)
            with open(fixer.path, encoding="utf-8") as f:
                for line in f:
                    m = ENTRY_RE.match(line.rstrip("\n"))
                    if m and m.group(2) in drop_set:
                        if m.group(2) not in en:
                            print(f"  !! {loc}/{m.group(2)} not in default "
                                  f"values/ - keeping to avoid aapt failure",
                                  file=sys.stderr)
                            out.append(line)
                            continue
                        changed = True
                        continue
                    if m and m.group(2) in restores:
                        nv = restores[m.group(2)]
                        out.append(f'{m.group(1)}<string name="{m.group(2)}">'
                                   f'{nv}</string>\n')
                        changed = True
                        continue
                    out.append(line)
            if changed:
                with open(fixer.path, "w", encoding="utf-8") as f:
                    f.writelines(out)

    print(f"\nTotals: {total_fixed} restored, {total_dropped} dropped")
    if not args.apply:
        print("dry-run only - pass --apply to rewrite")


if __name__ == "__main__":
    main()
