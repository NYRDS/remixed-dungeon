// Hero Loader - builds layer stacks exactly like the game does.
// Ground truth: ModernHeroSpriteDef.java, RetroHeroSpriteDef.java.

import {
    BODY_TYPE_MAP, LAYERS_ORDER, ARMOR_MAP, ARMOR_FLAGS,
    WEAPON_DEFS, ACCESSORY_MAP, ACCESSORY_FLAGS
} from './config.js';

export class HeroLoader {
    constructor() {
        this.heroTextures = {};
        this.heroLayers = [];
    }

    async loadHero(style, heroClass, subClass, armor, weapon, accessory) {
        const basePath = style === 'modern' ? 'assets/hero_modern/' : 'assets/hero/';
        const jsonPath = basePath + 'spritesDesc/Hero.json';

        const response = await fetch(jsonPath);
        if (!response.ok) {
            throw new Error(`Hero sprite JSON not found: ${jsonPath}`);
        }
        const spriteData = await response.json();

        const classDescriptor = heroClass + '_' + subClass;
        const bodyType = this.getBodyType(heroClass, subClass, style);

        const armorVisual = armor !== 'none' ? (ARMOR_MAP[armor.toLowerCase()] || armor) : null;
        const weaponDef = weapon !== 'none'
            ? (WEAPON_DEFS[weapon.toLowerCase()] || { visual: weapon, anim: 'none', twoHanded: false })
            : null;
        const accVisual = accessory !== 'none'
            ? (ACCESSORY_MAP[accessory.toLowerCase()] || accessory)
            : null;

        // -- covering rules from ModernHeroSpriteDef.createLayersDesc --------
        // A helmet suppresses the accessory; a hair-covering helmet, armor or
        // accessory suppresses hair; an item-covering accessory suppresses
        // held items. Availability of files gates everything else, matching
        // ModdingMode.isResourceExists in applyLayersDesc.

        // Layer candidates: name -> path. Missing files are skipped at load.
        const wanted = new Map();

        if (style === 'modern') {
            // back items render behind the body
            if (weaponDef) {
                wanted.set('right_back_item', `${basePath}items/${weaponDef.visual}_back_right.png`);
                if (weaponDef.shield) {
                    wanted.set('left_back_item', `${basePath}items/${weaponDef.visual}_back_left.png`);
                }
            }
        }

        wanted.set('body', `${basePath}body/${bodyType}.png`);

        if (armorVisual) {
            wanted.set('collar', `${basePath}armor/collar/${armorVisual}.png`);
        }

        wanted.set('head', `${basePath}head/${classDescriptor}.png`);

        if (armorVisual) {
            wanted.set('armor', `${basePath}armor/${armorVisual}.png`);
            if (style === 'modern') {
                wanted.set('armor_boots', `${basePath}armor/boots/${armorVisual}_${bodyType}.png`);
            }
        }

        // helmet first (it decides hair/accessory suppression), then hair
        const helmetPath = armorVisual ? `${basePath}armor/helmet/${armorVisual}.png` : null;
        const hasHelmet = helmetPath ? await this.checkResourceExists(helmetPath) : false;
        if (hasHelmet) {
            wanted.set('helmet', helmetPath);
        }

        const accFlags = accVisual ? (ACCESSORY_FLAGS[accVisual] || {}) : {};
        const armorFlags = armorVisual ? (ARMOR_FLAGS[armorVisual] || {}) : {};
        const helmetCoversHair = hasHelmet; // approximation of armor.isCoveringHair()
        const hairSuppressed = hasHelmet || armorFlags.coverHair || accFlags.coverHair;

        if (!hairSuppressed) {
            wanted.set('hair', `${basePath}head/hair/${classDescriptor}_HAIR.png`);
        }

        if (!accFlags.coverHair) {
            wanted.set('facial_hair', `${basePath}head/facial_hair/${classDescriptor}_FACIAL_HAIR.png`);
        }

        if (style === 'retro') {
            const deathDescriptor = classDescriptor === 'MAGE_WARLOCK' ? 'warlock' : 'common';
            wanted.set('death', `${basePath}death/${deathDescriptor}.png`);
        }

        if (style === 'modern') {
            const handAnim = weaponDef ? weaponDef.anim : 'none';
            // one-handed weapon -> right hand in weapon pose, left hand free;
            // two-handed -> both hands in weapon pose; shield -> plain hands
            const leftAnim = weaponDef && weaponDef.twoHanded && !weaponDef.shield ? handAnim : 'none';
            wanted.set('left_hand', `${basePath}body/hands/${bodyType}_${leftAnim}_left.png`);
            wanted.set('right_hand', `${basePath}body/hands/${bodyType}_${handAnim}_right.png`);

            // shoulders: for a hand holding a two-handed weapon use the
            // weapon pose variant, otherwise the plain hand variant
            if (armorVisual) {
                wanted.set('left_hand_armor', weaponDef && weaponDef.twoHanded && !weaponDef.shield
                    ? `${basePath}armor/shoulders/${armorVisual}_${weaponDef.anim}.png`
                    : `${basePath}armor/shoulders/${armorVisual}_left.png`);
                wanted.set('right_hand_armor', weaponDef && !weaponDef.shield
                    ? `${basePath}armor/shoulders/${armorVisual}_${weaponDef.anim}.png`
                    : `${basePath}armor/shoulders/${armorVisual}_right.png`);
            }

            // accessory renders under the helmet (helmet replaces it)
            if (accVisual && !hasHelmet) {
                wanted.set('accessory', `${basePath}accessories/${accVisual}.png`);
            }

            // held items, unless the accessory is a costume covering them
            if (weaponDef && !accFlags.coverItems) {
                if (weaponDef.shield) {
                    wanted.set('left_hand_item', `${basePath}items/${weaponDef.visual}_left.png`);
                } else {
                    wanted.set('right_hand_item', `${basePath}items/${weaponDef.visual}_right.png`);
                    wanted.set('left_hand_item', `${basePath}items/${weaponDef.visual}_left.png`);
                }
            }
        }

        // -- load layers, keep canonical z-order -----------------------------
        this.heroTextures = {};
        this.heroLayers = [];

        const names = [...wanted.keys()]
            .sort((a, b) => LAYERS_ORDER.indexOf(a) - LAYERS_ORDER.indexOf(b));

        for (const name of names) {
            await this.loadHeroLayer(name, wanted.get(name));
        }

        // attack/zap override per weapon, as heroUpdated() does in Java
        let attackOverride = null;
        if (style === 'modern' && weaponDef && !weaponDef.shield) {
            attackOverride = weaponDef.twoHanded ? 'dual' : 'right';
        }

        const currentHero = {
            name: `${heroClass} (${subClass})`,
            data: spriteData,
            width: spriteData.width,
            height: spriteData.height,
            style: style,
            class: heroClass,
            subClass: subClass,
            attackOverride: attackOverride,
            layers: this.heroLayers
        };

        return {
            hero: currentHero,
            textures: this.heroTextures,
            layers: this.heroLayers
        };
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
                // Layer file doesn't exist, skip it (game does the same)
                resolve();
            };
            img.src = filePath;
        });
    }

    getBodyType(heroClass, subClass, style) {
        // subclass first, then class (matching Java bodyDescriptor)
        if (BODY_TYPE_MAP[subClass]) return BODY_TYPE_MAP[subClass];
        if (BODY_TYPE_MAP[heroClass]) return BODY_TYPE_MAP[heroClass];

        // retro uses the hero's gender: only HUNTRESS is feminine (HeroClass.getGender)
        if (style === 'retro' && heroClass === 'HUNTRESS') return 'woman';

        return 'man';
    }
}
