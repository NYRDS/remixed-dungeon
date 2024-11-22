#!/usr/bin/env python3
import pprint
import re
from lxml import etree as ElementTree

dstDir = "../../RemixedDungeon/src/main/res/"

xml_ext = '.xml'
translations_dir = 'translations/'


source_locales = {"en","tr","ko","hu","it",'de_DE', 'es', 'fr_FR', 'pl_PL', 'ru',
                  'uk_UA', 'pt_BR', "ms_MY","zh_CN", "zh_TW", "id", 'el'}

locale_remap = {'de_DE': 'de', 'fr_FR': 'fr', 'pl_PL': 'pl', 'nl_NL': 'nl', 'ro_RO': 'ro',
                'uk_UA': 'uk', 'pt_BR': 'pt-rBR', 'pt_PT': 'pt-rPT', 'es_MX': 'es-rMX', "ms_MY": "ms", "zh_CN":'zh-rCN',
                "zh_TW":'zh-rTW', 'id':'in'}

counters = {}
totalCounter = {}

dir_name = "remixed-dungeon.strings-all-xml--master"
resource_name = "strings_all.xml"

escape_apostrophe = re.compile(r"(?<!\\)'")

print("Processing:", dir_name, resource_name)

counters[resource_name] = {}


def indent(elem, level=0):
    i = "\n" + level*"  "
    if len(elem):
        if not elem.text or not elem.text.strip():
            elem.text = i + "  "
        if not elem.tail or not elem.tail.strip():
            elem.tail = i
        for elem in elem:
            indent(elem, level+1)
        if not elem.tail or not elem.tail.strip():
            elem.tail = i
    else:
        if level and (not elem.tail or not elem.tail.strip()):
            elem.tail = i


def unescape(arg):
    return arg.replace("\\\\", "\\") \
        .replace("\\\\", "\\") \
        .replace("&gt;", ">") \
        .replace("&lt;", "<")


def processText(arg):
    if arg is None:
        return arg

    ret = unescape(arg)
    ret = escape_apostrophe.sub(r"\'", ret)
    return ret


def makeRJava(strings, arrays):
    rJava = open("src/market_none/java/com/nyrds/pixeldungeon/ml/R.java", "w", encoding='utf8')
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

strings_files = ['RemixedDungeon/src/main/res/values/strings_not_translate.xml',
                 'RemixedDungeon/src/main/res/values/strings_api_signature.xml',
                 'RemixedDungeon/src/main/res/values/string_arrays.xml',
                 'RemixedDungeon/src/main/res/values/strings_all.xml']

for file in strings_files:
    pfile = ElementTree.parse('../' + file).getroot()

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

pprint.pprint(totalCounter)

pprint.pprint(d_arrays)

makeRJava(r_strings, r_arrays)