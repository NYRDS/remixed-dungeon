import codecs
import json
import os
import requests
import zipfile

properties = ["Mod_Author", "Mod_Link","Mod_Email","Mod_Name","Mod_Description"]

#mods_json = urllib.request.urlopen("https://nyrds.github.io/NYRDS/mods2.json").read().decode()

mods_json = open("mods2.json").read()
mods = json.loads(mods_json)
mods['info'] = {}

print("Downloading mods...")
for cat in mods['known_mods']:
    for mod in mods['known_mods'][cat]:
        print(mod)

        response = requests.get(mod['url'], timeout=5)

        with open(mod['name'], 'wb') as f:
            f.write(response.content)

        with zipfile.ZipFile(mod['name'], 'r') as zip_ref:
            print("Extracting", mod['name'])
            zip_ref.extractall(f"./mods")

print("Parsing...")
for dir in os.listdir("mods"):
    print(dir)

    for lang in ["en","ru","fr","es"]:
        try:
            mod_data = {}

            with open(f"mods/{dir}/strings_{lang}.json", 'rb') as strings_lang:
                for line in strings_lang:
                    str = None
                    try:
                        str = codecs.decode(line, encoding='utf_8_sig')
                        str = str.strip(',\n\r')
                        data = json.loads(str)
                        for prop in properties:
                            if prop == data[0]:
                                mod_data[data[0]] = data[1]

                    except Exception as e:
                        print(e, str)

            if mod_data['Mod_Name'] not in mods['info']:
                mods['info'][mod_data['Mod_Name']] = {}

            mods['info'][mod_data['Mod_Name']][lang] = mod_data

        except Exception as e:
            print(e)

with open("mods2.json", 'w') as mods_json:
    mods_json.write(json.dumps(mods,indent=2,ensure_ascii=False))