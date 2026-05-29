// Hero Loader - Handles loading hero sprite layers

import { BODY_TYPE_MAP } from './config.js';

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
            const spriteData = await response.json();

            // Build layer configuration based on hero setup
            const classDescriptor = heroClass + '_' + subClass;
            const bodyType = this.getBodyType(heroClass, subClass, style);

            this.heroLayers = [];
            this.heroTextures = {};

            // Body
            const bodyFile = `${basePath}body/${bodyType}.png`;
            await this.loadHeroLayer('body', bodyFile);

            // Head
            const headFile = `${basePath}head/${classDescriptor}.png`;
            await this.loadHeroLayer('head', headFile);

            // Hair (if not covered)
            const hairFile = `${basePath}head/hair/${classDescriptor}_HAIR.png`;
            await this.loadHeroLayer('hair', hairFile);

            // Armor
            if (armor !== 'none') {
                const armorFile = `${basePath}armor/${armor}.png`;
                await this.loadHeroLayer('armor', armorFile);

                // Armor boots
                const armorBootsFile = `${basePath}armor/boots/${armor}_${bodyType}.png`;
                await this.loadHeroLayer('armor_boots', armorBootsFile);

                // Helmet
                const helmetFile = `${basePath}armor/helmet/${armor}.png`;
                await this.loadHeroLayer('helmet', helmetFile);

                // Collar
                const collarFile = `${basePath}armor/collar/${armor}.png`;
                await this.loadHeroLayer('collar', collarFile);
            }

            // Hands
            const animClass = weapon !== 'none' ? weapon : 'none';
            const leftHandFile = `${basePath}body/hands/${bodyType}_${animClass}_left.png`;
            const rightHandFile = `${basePath}body/hands/${bodyType}_${animClass}_right.png`;
            await this.loadHeroLayer('left_hand', leftHandFile);
            await this.loadHeroLayer('right_hand', rightHandFile);

            // Weapon item
            if (weapon !== 'none') {
                const weaponFileRight = `${basePath}items/${weapon}_right.png`;
                const weaponFileLeft = `${basePath}items/${weapon}_left.png`;
                await this.loadHeroLayer('right_hand_item', weaponFileRight);
                await this.loadHeroLayer('left_hand_item', weaponFileLeft);
            }

            // Accessory
            if (accessory !== 'none') {
                const accessoryFile = `${basePath}accessories/${accessory}.png`;
                await this.loadHeroLayer('accessory', accessoryFile);
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
        // Check subclass first, then class
        if (BODY_TYPE_MAP[subClass]) return BODY_TYPE_MAP[subClass];
        if (BODY_TYPE_MAP[heroClass]) return BODY_TYPE_MAP[heroClass];

        // Default based on class
        if (heroClass === 'HUNTRESS') return 'woman';
        if (heroClass === 'NECROMANCER' && subClass === 'LICH') return 'lich';
        if (heroClass === 'GNOLL') return 'gnoll';

        return style === 'modern' ? 'man' : 'man';
    }
}
