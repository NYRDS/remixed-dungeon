// Hero Loader - builds layer stacks exactly like the game does.
// Ground truth: ModernHeroSpriteDef.java, RetroHeroSpriteDef.java.

import {
    BODY_TYPE_MAP, LAYERS_ORDER, ARMOR_MAP, ARMOR_FLAGS,
    WEAPON_DEFS
} from './config.js';
import { resolve } from './data-source.js';

export class HeroLoader {
    constructor() {
        this.heroTextures = {};
        this.heroLayers = [];
    }

    async loadHero(style, heroClass, subClass, armor, weapon, leftWeapon) {
        const basePath = style === 'modern' ? 'assets/hero_modern/' : 'assets/hero/';
        const jsonPath = resolve(basePath + 'spritesDesc/Hero.json');

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
        const leftDef = leftWeapon !== 'none'
            ? (WEAPON_DEFS[leftWeapon.toLowerCase()] || { visual: leftWeapon, anim: 'none', twoHanded: false })
            : null;

        // -- covering rules from ModernHeroSpriteDef.createLayersDesc --------
        // A hair-covering armor suppresses hair; availability of files gates
        // everything else, matching ModdingMode.isResourceExists.

        // Layer candidates: name -> path. Missing files are skipped at load.
        const wanted = new Map();

        if (style === 'modern') {
            // back items render behind the body
            if (weaponDef) {
                wanted.set('right_back_item', `${basePath}items/${weaponDef.visual}_back_${weaponDef.shield ? 'left' : 'right'}.png`);
            }
            if (leftDef) {
                wanted.set('left_back_item', `${basePath}items/${leftDef.visual}_back_left.png`);
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

        // hair per the Java branch structure: the armor's coverHair flag
        // decides (no accessory layer anymore). Retro suppresses hair only
        // for a hair-covering armor whose helmet is present.
        const helmetPath = armorVisual ? `${basePath}armor/helmet/${armorVisual}.png` : null;
        const hasHelmet = helmetPath ? await this.checkResourceExists(helmetPath) : false;
        if (hasHelmet) {
            wanted.set('helmet', helmetPath);
        }

        const armorFlags = armorVisual ? (ARMOR_FLAGS[armorVisual] || {}) : {};

        const drawHair = style === 'retro'
            ? !(hasHelmet && armorFlags.coverHair)
            : !armorFlags.coverHair;

        if (drawHair) {
            wanted.set('hair', `${basePath}head/hair/${classDescriptor}_HAIR.png`);
        }

        if (!armorFlags.coverFacialHair) {
            wanted.set('facial_hair', `${basePath}head/facial_hair/${classDescriptor}_FACIAL_HAIR.png`);
        }

        if (style === 'retro') {
            const deathDescriptor = classDescriptor === 'MAGE_WARLOCK' ? 'warlock' : 'common';
            wanted.set('death', `${basePath}death/${deathDescriptor}.png`);
        }

        if (style === 'modern') {
            const handAnim = weaponDef ? weaponDef.anim : 'none';
            // left hand pose: the left item's own animation class; a
            // two-handed right weapon engages the left hand in the same pose
            const leftHandAnim = weaponDef && weaponDef.twoHanded && !weaponDef.shield
                ? handAnim
                : leftDef ? leftDef.anim : 'none';
            wanted.set('left_hand', `${basePath}body/hands/${bodyType}_${leftHandAnim}_left.png`);
            wanted.set('right_hand', `${basePath}body/hands/${bodyType}_${handAnim}_right.png`);

            // shoulders (armorShoulderDescriptor): the weapon-pose variant is
            // used only by an item that BLOCKS the off-hand slot (two-handed
            // weapons); everything else takes the plain hand variant
            if (armorVisual) {
                const rightShoulder = weaponDef && weaponDef.twoHanded && !weaponDef.shield
                    ? `${armorVisual}_${weaponDef.anim}.png` : `${armorVisual}_right.png`;
                const leftShoulder = (weaponDef && weaponDef.twoHanded && !weaponDef.shield)
                        || (leftDef && leftDef.twoHanded && !leftDef.shield)
                    ? `${armorVisual}_${(leftDef && leftDef.twoHanded && !leftDef.shield) ? leftDef.anim : weaponDef.anim}.png`
                    : `${armorVisual}_left.png`;
                wanted.set('left_hand_armor', `${basePath}armor/shoulders/${leftShoulder}`);
                wanted.set('right_hand_armor', `${basePath}armor/shoulders/${rightShoulder}`);
            }

            // held items: shields always render in the left hand, everything
            // else in the hand that holds it
            if (weaponDef) {
                if (weaponDef.shield) {
                    wanted.set('left_hand_item', `${basePath}items/${weaponDef.visual}_left.png`);
                } else {
                    wanted.set('right_hand_item', `${basePath}items/${weaponDef.visual}_right.png`);
                }
            }
            if (leftDef && !(leftDef.shield && weaponDef && weaponDef.shield)) {
                wanted.set('left_hand_item', `${basePath}items/${leftDef.visual}_left.png`);
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

        // attack/zap override per held weapons, as heroUpdated() does in Java:
        // both hands melee -> dual, right melee -> right, left melee -> left;
        // a two-handed right weapon plays the dual (both-hands) sequence
        let attackOverride = null;
        if (style === 'modern') {
            const rightAttack = weaponDef && !weaponDef.shield
                ? (weaponDef.twoHanded ? 'dual' : 'right') : null;
            const leftAttack = leftDef && !leftDef.shield ? 'left' : null;
            attackOverride = rightAttack && leftAttack ? 'dual' : (rightAttack || leftAttack);
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
            const response = await fetch(resolve(path), { method: 'HEAD' });
            return response.ok;
        } catch (e) {
            return false;
        }
    }

    async loadHeroLayer(layerName, filePath) {
        return new Promise((resolvePromise) => {
            const img = new Image();
            img.onload = () => {
                this.heroTextures[layerName] = img;
                this.heroLayers.push(layerName);
                resolvePromise();
            };
            img.onerror = () => {
                // Layer file doesn't exist, skip it (game does the same)
                resolvePromise();
            };
            // keep the canvas untainted when layers come from GitHub
            img.crossOrigin = 'anonymous';
            img.src = resolve(filePath);
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
