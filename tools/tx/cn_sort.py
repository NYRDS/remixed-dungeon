#!/usr/bin/env python3
import os
import pprint
import re
from langdetect import detect
from lxml import etree as ElementTree
from lxml.etree import Element
from lxml.etree import SubElement
from mafan import text

dstDir = "../../RemixedDungeon/src/main/res/"

xml_ext = '.xml'
translations_dir = 'translations/'

# source_locales = {"zh", "zh-Hans", "zh_CN", "zh_HK", "zh_TW"}
source_locales = {"zh_CN"}

locale_remap = {}

counters = {}
totalCounter = {}

dir_name = "remixed-dungeon.strings-all-xml--master"
resource_name = "strings_all.xml"

escape_apostrophe = re.compile(r"(?<!\\)'")

print("Processing:", dir_name, resource_name)

counters[resource_name] = {}


def indent(elem, level=0):
    i = "\n" + level * "  "
    if len(elem):
        if not elem.text or not elem.text.strip():
            elem.text = i + "  "
        if not elem.tail or not elem.tail.strip():
            elem.tail = i
        for elem in elem:
            indent(elem, level + 1)
        if not elem.tail or not elem.tail.strip():
            elem.tail = i
    else:
        if level and (not elem.tail or not elem.tail.strip()):
            elem.tail = i


def unescape(arg):
    return arg.replace("\\\\", "\\").replace("\\\\", "\\")


def lang(arg):
    if arg is None:
        return None

    for_detect = arg.replace("%d", "").replace("%s", "").replace("%1$s", "").replace("%2$s", "")
    from langdetect.lang_detect_exception import LangDetectException
    ret = None
    try:
        ret = detect(for_detect)
        print(arg, "->", ret, text.identify(for_detect))
    except LangDetectException as e:
        print(arg, "->", e)
    return ret


def process_text(arg):
    if arg is None:
        return arg

    ret = unescape(arg)
    ret = escape_apostrophe.sub(r"\'", ret)

    return ret


zh_cn = Element('resources')
zh_tw = Element('resources')

used_names = {}

for _, _, files in os.walk(translations_dir + dir_name):

    arrays = ElementTree.Element("resources")

    for file_name in files:

        locale_code = file_name[:-4]

        if not locale_code in source_locales:
            continue

        if locale_code in locale_remap:
            locale_code = locale_remap[locale_code]

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
        try:
            transifexData = ElementTree.parse(currentFilePath).getroot()

            for entry in transifexData:
                if entry.tag not in ["string", "string-array"]:
                    continue

                entryName = None

                entryName = entry.get('name')

                if entryName in used_names.keys():
                    continue

                used_names[entryName] = True

                counters[resource_name][locale_code] += 1
                totalCounter[locale_code] += 1

                entry.text = process_text(entry.text)

                if entry.text is not None:
                    outEntry = SubElement(zh_cn, entry.tag, {'name': entryName})
                    outEntry.text = text.simplify(entry.text)

                    outEntry = SubElement(zh_tw, entry.tag, {'name': entryName})
                    outEntry.text = text.tradify(entry.text)
                else:
                    print(entry.tag, entry.text)

            indent(zh_cn)
            ElementTree.ElementTree(zh_cn).write('zh_CN.xml', encoding="utf-8",
                                                 method="xml")

            indent(zh_tw)
            ElementTree.ElementTree(zh_tw).write('zh_TW.xml', encoding="utf-8",
                                                 method="xml")

        except ElementTree.ParseError as error:
            print("shit happens with " + currentFilePath)
            print(error)

pprint.pprint(totalCounter)
