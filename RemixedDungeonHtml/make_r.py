#!/usr/bin/env python3
import os
from lxml import etree as ElementTree


def makeRJava(strings, arrays):
    target_dir = 'src/html/java/com/nyrds/pixeldungeon/ml'
    os.makedirs(target_dir, exist_ok=True)
    rJava = open(f"{target_dir}/R.java", "w", encoding='utf8')
    rJava.write('''
    package com.nyrds.pixeldungeon.ml;

    public class R {
        public static class string { 
                ''')

    counter = 0

    for str in strings:
        rJava.write(f'''
                public static final int {str} = {counter};''')
        counter += 1

    rJava.write('''}
    
    public static class array {
    ''')

    for str in arrays:
        rJava.write(f'''
                public static final int {str} = {counter};''')
        counter += 1


    rJava.write('''
        }
    }''')
    rJava.close()


r_strings = set()
r_arrays = set()

d_strings = {}
d_arrays = {}

locales = []

strings_files = ['../RemixedDungeon/src/main/res/values/strings_not_translate.xml',
                 '../RemixedDungeon/src/main/res/values/strings_api_signature.xml',
                 '../RemixedDungeon/src/main/res/values/string_arrays.xml',
                 '../RemixedDungeon/src/main/res/values/strings_all.xml']

for file in strings_files:
    pfile = ElementTree.parse(file).getroot()

    for entry in pfile:
        if entry.tag not in ["string", "string-array"]:
            continue

        entry_name = entry.get("name")
        if entry.tag == 'string':
            r_strings.add(entry_name)

        if entry.tag == 'string-array':
            d_arrays[entry_name] = []
            for e in entry:
                d_arrays[entry_name].append(e.text.replace("@string/", ""))

            r_arrays.add(entry_name)
print("Making R.java")
makeRJava(r_strings, r_arrays)
print("Done")