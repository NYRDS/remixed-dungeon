#!/usr/bin/env python3
import json
import os
import re
from lxml import etree as ElementTree

xml_ext = '.xml'
values_dir = '../RemixedDungeon/src/main/res/'

escape_apostrophe = re.compile(r"(?<!\\)'")

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

def unescape(arg):
    return arg.replace("\\\\", "\\")\
        .replace("\\\\", "\\")\
        .replace("&gt;", ">")\
        .replace("&lt;", "<")

def process_xml(in_file_name:str, jsonData):
    xml_data = ElementTree.parse(in_file_name).getroot()

    for entry in xml_data:

        if entry.tag not in ["string", "string-array"]:
            continue

        entry_name = entry.get("name")

        if entry.tag == "string" and entry_name is not None:
            jsonStr = (unescape(str(entry.text)).replace(r"\'", "'")
                       .replace(r"\’", "’")
                       .replace(r"\?", "?")
                       .replace(r"\ n","\n")
                       .replace(r"\%", "%")
                       .replace(r"\ "," "))
            if jsonStr == '""':
                jsonStr = ""

            d_strings[locale_code][entry.get("name")] = jsonStr
        
            jsonData.write(json.dumps([entry_name, jsonStr], ensure_ascii=False))
            jsonData.write("\n")

locale_remap = { 'pt-rBR': 'pt_BR', 'zh-rCN':'zh_CN', 'zh-rTW':'zh_TW'}
for root, dirs, files in os.walk(values_dir):
    if 'values' not in root:
        continue

    locale_code = 'en'
    parts = root.split("values-")
    if len(parts) == 2:
        locale_code = parts[1]

    if locale_code in locale_remap:
        locale_code = locale_remap[locale_code]

    locales.append(locale_code)
    d_strings[locale_code] = {}

    jsonData = open(f"src/desktop/l10ns/strings_{locale_code}.json", "w", encoding='utf8')
    for file in files:

        if file.endswith(xml_ext):
            #print(root, file, locale_code)
            process_xml(root + "/" + file, jsonData)

    jsonData.close()


for locale in locales:
    jsonData = open(f"src/desktop/l10ns/strings_{locale}.json", "w", encoding='utf8')

    for key, jsonStr in d_strings[locale].items():
        jsonData.write(unescape(json.dumps([key, jsonStr], ensure_ascii=False)))
        jsonData.write("\n")

    for key, array in d_arrays.items():
        localizedArray = [key]
        for s_key in array:
            value = d_strings["en"][s_key]
            if s_key in d_strings[locale]:
                value = d_strings[locale][s_key]

            localizedArray.append(value)

        jsonData.write(unescape(json.dumps(localizedArray, ensure_ascii=False)))
        jsonData.write("\n")

    jsonData.close()
