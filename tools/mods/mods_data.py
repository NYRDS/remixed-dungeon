import codecs
import json
import os
import requests
import zipfile
from urllib.parse import urlparse

properties = ["Mod_Author", "Mod_Link","Mod_Email","Mod_Name","Mod_Description"]

# Try both URLs - first one to respond successfully will be used
urls_to_try = [
    "https://ru.nyrds.net/rpd/mods2.json",
    "https://nyrds.net/rpd/mods2.json"
]

mods = None

# First try to load from local file if it exists
if os.path.exists("mods2.json"):
    print("Loading mods list from local file")
    try:
        with open("mods2.json", 'r') as f:
            mods = json.load(f)
        print("Successfully loaded mods list from local file")
    except Exception as e:
        print(f"Failed to load from local file: {e}")

# If local file failed or doesn't exist, try downloading
if mods is None:
    print("Attempting to download mods list from remote servers...")
    for url in urls_to_try:
        try:
            print(f"Trying to download mods list from {url}")
            # Disable proxies to avoid SOCKS issues
            response = requests.get(url, timeout=10, proxies={})
            response.raise_for_status()  # Raise an exception for bad status codes
            mods = json.loads(response.text)
            print(f"Successfully downloaded mods list from {url}")
            
            # Save to local file for future use
            with open("mods2.json", 'w') as f:
                json.dump(mods, f, indent=2, ensure_ascii=False)
            print("Saved mods list to local file")
            break
        except Exception as e:
            print(f"Failed to download from {url}: {e}")
            continue

if mods is None:
    print("Failed to download mods list from all URLs and no local file available")
    exit(1)

if 'info' not in mods:
    mods['info'] = {}

# Create mods directory if it doesn't exist
if not os.path.exists("mods"):
    os.makedirs("mods")

print("Downloading mods...")
for cat in mods['known_mods']:
    for mod in mods['known_mods'][cat]:
        print(mod)

        try:
            response = requests.get(mod['url'], timeout=15, proxies={})
            response.raise_for_status()

            with open(mod['name'], 'wb') as f:
                f.write(response.content)

            with zipfile.ZipFile(mod['name'], 'r') as zip_ref:
                print("Extracting", mod['name'])
                zip_ref.extractall(f"./mods")
        except Exception as e:
            print(f"Failed to download/extract {mod['name']}: {e}")

print("Parsing...")
if os.path.exists("mods") and os.listdir("mods"):
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
else:
    print("No mods directory or mods directory is empty, skipping parsing")

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