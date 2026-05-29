// Hero Loader - Handles loading hero sprite layers

import { BODY_TYPE_MAP, LAYERS_ORDER } from './config.js';

export class HeroLoader {
    constructor() {
        this.heroTextures = {};
        this.heroLayers = [];
    }

    async loadHero(style, heroClass, subClass, armor, weapon, accessory) {
        try {
            const basePath = style === 'modern' ? 'assets/hero_modern/' : 'assets/hero/';
            const jsonPath = basePath + 'spritesDesc/Hero.json';

            const response = await fetch(jsonPath);
            if (!response.ok) {
                throw new Error(`Hero sprite JSON not found: ${jsonPath}`);
            }
            const spriteData = await response.json();

            // Build layer configuration based on hero setup
            const classDescriptor = heroClass + '_' + subClass;
            const bodyType = this.getBodyType(heroClass, subClass, style);

            this.heroLayers = [];
            this.heroTextures = {};

            // Helper to convert to PascalCase with Armor suffix for armor names (matching Java getVisualName())
            const getArmorVisualName = (armorName) => {
                if (armorName === 'none') return 'none';
                // Map lowercase config names to actual file names (PascalCase + Armor suffix)
                const armorMap = {
                    'cloth': 'ClothArmor',
                    'leather': 'LeatherArmor',
                    'mail': 'MailArmor',
                    'scale': 'ScaleArmor',
                    'plate': 'PlateArmor',
                    'gothic': 'GothicArmor',
                    'rogue': 'RogueArmor',
                    'warrior': 'WarriorArmor',
                    'mage': 'MageArmor',
                    'huntress': 'HuntressArmor',
                    'scout': 'ScoutArmor',
                    'shaman': 'ShamanArmor',
                    'gladiator': 'GladiatorArmor',
                    'berserk': 'BerserkArmor',
                    'warlock': 'WarlockArmor',
                    'battlemage': 'BattleMageArmor',
                    'assasin': 'AssasinArmor',
                    'freerunner': 'FreeRunnerArmor',
                    'sniper': 'SniperArmor',
                    'warden': 'WardenArmor',
                    'necromancer': 'NecromancerArmor',
                    'gnoll': 'GnollArmor',
                    'spider': 'SpiderArmor',
                    'rat': 'RatArmor',
                    'chaos': 'ChaosArmor',
                    'elf': 'ElfArmor',
                    'necromancerrobe': 'NecromancerRobe',
                    'priest': 'PriestArmor',
                    'paladin': 'PaladinArmor',
                    'cleric': 'ClericArmor',
                    'witchdoctor': 'WitchdoctorArmor',
                    'alchemist': 'AlchemistArmor',
                    'transmuter': 'TransmuterArmor',
                    'plaguedoctor': 'PlagueDoctorArmor'
                };
                return armorMap[armorName.toLowerCase()] || armorName;
            };

            // Helper to convert weapon names to PascalCase visual names (matching Java getVisualName())
            const getWeaponVisualName = (weaponName) => {
                if (weaponName === 'none') return 'none';
                const weaponMap = {
                    'shortsword': 'ShortSword',
                    'longsword': 'Longsword',
                    'dagger': 'Dagger',
                    'mace': 'Mace',
                    'hammer': 'Hammer',
                    'sword': 'GoldenSword',
                    'wand': 'Wand',
                    'boomerang': 'Boomerang',
                    'bow': 'CompoundBow',
                    'crossbow': 'CompositeCrossbow',
                    'spear': 'Spear',
                    'glaive': 'Glaive',
                    'battleaxe': 'BattleAxe',
                    'claymore': 'Claymore',
                    'quarterstaff': 'Quarterstaff',
                    'knuckles': 'Knuckles',
                    'bonesaw': 'BoneSaw',
                    'tomahawk': 'GnollTamahawk',
                    'halberd': 'Halberd',
                    'kusarigama': 'Kusarigama',
                    'pickaxe': 'Pickaxe',
                    'royalshield': 'RoyalShield',
                    'chaosshield': 'ChaosShield',
                    'chaossword': 'ChaosSword',
                    'chaosbow': 'ChaosBow',
                    'chaosstaff': 'ChaosStaff'
                };
                return weaponMap[weaponName.toLowerCase()] || this.capitalizeFirst(weaponName);
            };

            // Helper to convert accessory names to PascalCase (matching file names)
            const getAccessoryVisualName = (accName) => {
                if (accName === 'none') return 'none';
                return this.capitalizeFirst(accName);
            };

            // Body - verify it exists first
            const bodyFile = `${basePath}body/${bodyType}.png`;
            if (await this.checkResourceExists(bodyFile)) {
                await this.loadHeroLayer('body', bodyFile);
            } else {
                console.warn(`Body texture not found: ${bodyFile}, trying fallback...`);
                const fallbackBody = `${basePath}body/man.png`;
                if (await this.checkResourceExists(fallbackBody)) {
                    await this.loadHeroLayer('body', fallbackBody);
                }
            }

            // Collar (loaded early as per Java layer order - after body, before head)
            if (armor !== 'none') {
                const armorVisualName = getArmorVisualName(armor);
                const collarFile = `${basePath}armor/collar/${armorVisualName}.png`;
                if (await this.checkResourceExists(collarFile)) {
                    await this.loadHeroLayer('collar', collarFile);
                }
            }

            // Head
            const headFile = `${basePath}head/${classDescriptor}.png`;
            if (await this.checkResourceExists(headFile)) {
                await this.loadHeroLayer('head', headFile);
            }

            // Hair
            const hairFile = `${basePath}head/hair/${classDescriptor}_HAIR.png`;
            if (await this.checkResourceExists(hairFile)) {
                await this.loadHeroLayer('hair', hairFile);
            }

            // Facial hair (between hair and helmet as per Java layer order)
            const facialHairFile = `${basePath}head/facial_hair/${classDescriptor}_FACIAL_HAIR.png`;
            if (await this.checkResourceExists(facialHairFile)) {
                await this.loadHeroLayer('facial_hair', facialHairFile);
            }

            // Armor
            if (armor !== 'none') {
                const armorVisualName = getArmorVisualName(armor);
                const armorFile = `${basePath}armor/${armorVisualName}.png`;
                if (await this.checkResourceExists(armorFile)) {
                    await this.loadHeroLayer('armor', armorFile);

                    // Armor boots (note: file naming is ArmorName_bodyType.png, NOT ArmorName_boots_bodyType.png)
                    const armorBootsFile = `${basePath}armor/boots/${armorVisualName}_${bodyType}.png`;
                    if (await this.checkResourceExists(armorBootsFile)) {
                        await this.loadHeroLayer('armor_boots', armorBootsFile);
                    }

                    // Helmet
                    const helmetFile = `${basePath}armor/helmet/${armorVisualName}.png`;
                    if (await this.checkResourceExists(helmetFile)) {
                        await this.loadHeroLayer('helmet', helmetFile);
                    }
                }
            }

            // Hands (using lowercase weapon animation class as per Java)
            const animClass = weapon !== 'none' ? weapon.toLowerCase() : 'none';
            const leftHandFile = `${basePath}body/hands/${bodyType}_${animClass}_left.png`;
            const rightHandFile = `${basePath}body/hands/${bodyType}_${animClass}_right.png`;
            
            if (await this.checkResourceExists(leftHandFile)) {
                await this.loadHeroLayer('left_hand', leftHandFile);
            }
            if (await this.checkResourceExists(rightHandFile)) {
                await this.loadHeroLayer('right_hand', rightHandFile);
            }

            // Weapon item (using PascalCase visual names as per Java getVisualName())
            if (weapon !== 'none') {
                const weaponVisualName = getWeaponVisualName(weapon);
                const weaponRightFile = `${basePath}items/${weaponVisualName}_right.png`;
                const weaponLeftFile = `${basePath}items/${weaponVisualName}_left.png`;
                
                if (await this.checkResourceExists(weaponRightFile)) {
                    await this.loadHeroLayer('right_hand_item', weaponRightFile);
                }
                if (await this.checkResourceExists(weaponLeftFile)) {
                    await this.loadHeroLayer('left_hand_item', weaponLeftFile);
                }
            }

            // Accessory (using PascalCase names matching file names)
            if (accessory !== 'none') {
                const accessoryVisualName = getAccessoryVisualName(accessory);
                const accessoryFile = `${basePath}accessories/${accessoryVisualName}.png`;
                if (await this.checkResourceExists(accessoryFile)) {
                    await this.loadHeroLayer('accessory', accessoryFile);
                }
            }

            // Set up hero data
            const currentHero = {
                name: `${heroClass} (${subClass})`,
                data: spriteData,
                width: spriteData.width,
                height: spriteData.height,
                visualWidth: spriteData.visualWidth || spriteData.width,
                visualHeight: spriteData.visualHeight || spriteData.height,
                visualOffsetX: spriteData.visualOffsetX || 0,
                visualOffsetY: spriteData.visualOffsetY || 0,
                style: style,
                class: heroClass,
                subClass: subClass,
                layers: this.heroLayers
            };

            return {
                hero: currentHero,
                textures: this.heroTextures,
                layers: this.heroLayers
            };

        } catch (error) {
            console.error(`Failed to load hero:`, error);
            throw error;
        }
    }
    
    async checkResourceExists(path) {
        try {
            const response = await fetch(path, { method: 'HEAD' });
            return response.ok;
        } catch (e) {
            return false;
        }
    }

    async loadHeroLayer(layerName, filePath) {
        return new Promise((resolve) => {
            const img = new Image();
            img.onload = () => {
                this.heroTextures[layerName] = img;
                this.heroLayers.push(layerName);
                resolve();
            };
            img.onerror = () => {
                // Layer file doesn't exist, skip it
                resolve();
            };
            img.src = filePath;
        });
    }

    getBodyType(heroClass, subClass, style) {
        // Check subclass first, then class (matching Java bodyDescriptor)
        if (BODY_TYPE_MAP[subClass]) return BODY_TYPE_MAP[subClass];
        if (BODY_TYPE_MAP[heroClass]) return BODY_TYPE_MAP[heroClass];

        // Default based on class
        if (heroClass === 'HUNTRESS') return 'woman';
        if (heroClass === 'NECROMANCER' && subClass === 'LICH') return 'lich';
        if (heroClass === 'GNOLL') return 'gnoll';

        return style === 'modern' ? 'man' : 'man';
    }

    capitalizeFirst(str) {
        if (!str) return str;
        return str.charAt(0).toUpperCase() + str.slice(1);
    }
}
