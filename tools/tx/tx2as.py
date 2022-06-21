#!/usr/bin/env python3
import json
import os
import pprint
import re
from langdetect import detect
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

changelog_prefix = "Welcome_Text_"
changelog_key = "Welcome_Text_0"
changelog = {}

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
    return arg.replace("\\\\", "\\")\
        .replace("\\\\", "\\")\
        .replace("&gt;", ">")\
        .replace("&lt;", "<")


def lang(arg):
    if arg is None:
        return None

    for_detect = arg.replace("%d", "").replace("%s", "").replace("%1$s", "").replace("%2$s", "")
    from langdetect.lang_detect_exception import LangDetectException
    ret = None
    try:
        ret = detect(for_detect)
    except LangDetectException as e:
        print(arg, "->", e)
    return ret


def processText(arg):
    if arg is None:
        return arg

    ret = unescape(arg)
    ret = escape_apostrophe.sub(r"\'", ret)
    return ret


def makeRJava(strings, arrays):
    rJava = open("R.java", "w", encoding='utf8')
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
    pfile = ElementTree.parse('../../' + file).getroot()

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


for _, _, files in os.walk(translations_dir + dir_name):

    arrays = ElementTree.Element("resources")
    for file_name in files:

        locale_code = file_name[:-4]

        if not locale_code in source_locales:
            continue

        if locale_code in locale_remap:
            locale_code = locale_remap[locale_code]

        d_strings[locale_code] = {}
        locales.append(locale_code)

        if locale_code not in totalCounter:
            totalCounter[locale_code] = 0

        counters[resource_name][locale_code] = 0

        if locale_code == 'en':
            resource_dir = dstDir + "values"
        else:
            resource_dir = dstDir + "values-" + locale_code

        if not os.path.isdir(resource_dir):
            os.makedirs(resource_dir)

        currentFilePath = translations_dir + dir_name + "/" + file_name

        print("file:", currentFilePath)
        changelog_key = "Welcome_Text_0"
        try:
            transifexData = ElementTree.parse(currentFilePath).getroot()

            jsonData = open("strings_" + locale_code + ".json", "w", encoding='utf8')

            for entry in transifexData:
                if entry.tag not in ["string", "string-array"]:
                    continue

                #detectedLang = lang(entry.text)
                #print(entry.text, detectedLang)

                entry_name = entry.get("name")

                counters[resource_name][locale_code] += 1
                totalCounter[locale_code] += 1

                if entry.tag == "string":
                    if entry_name.startswith(changelog_prefix):
                        if entry_name > changelog_key:
                            changelog_key = entry_name
                            changelog[locale_code] = entry.text
                    text = entry.text
                    if text is None:
                        text = ""

                    jsonStr = unescape(text).replace(r"\'", "'").replace(r"\’", "’").replace(r"\?","?")

                    d_strings[locale_code][entry.get("name")] = jsonStr

                entry.text = processText(entry.text)

            indent(transifexData)

            xml_out_name = resource_dir + "/" + resource_name
            ElementTree.ElementTree(transifexData).write(xml_out_name, encoding="utf-8", method="xml")

            with open(xml_out_name, "r") as fi:
                lines = fi.readlines()
                with open(xml_out_name, "w") as fo:
                    for line in lines:
                        fo.write(unescape(line))

        except ElementTree.ParseError as error:
            print("shit happens with " + currentFilePath)
            print(error)


for locale in locales:
    jsonData = open("strings_" + locale + ".json", "w", encoding='utf8')

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

#        jsonData.write(unescape(json.dumps([entry.get("name"), jsonStr], ensure_ascii=False)))
#        jsonData.write("\n")

pprint.pprint(totalCounter)

pprint.pprint(d_arrays)

makeRJava(r_strings, r_arrays)

for locale_code, text in changelog.items():
    print(locale_code)
    print(text.replace("\\n",""))