// Mob Loader - Handles loading mob sprite data

export class MobLoader {
    constructor() {
        this.textureImage = null;
    }

    async loadMobList() {
        try {
            // Fetch the list of sprite JSON files
            const response = await fetch('assets/spritesDesc/');
            
            // This won't work directly in browser, so we'll use a predefined list
            // In production, you'd need a server endpoint or build step
            return await this.getMobNames();
        } catch (error) {
            console.log('Directory listing not available, loading mob data directly...');
            return await this.getMobNames();
        }
    }

    getMobNames() {
        // List of available mobs based on the JSON files found
        return [
            'Bat', 'Brute', 'Crab', 'Eye', 'Ghost', 'Gnoll', 'Golem', 'Goo',
            'King', 'Rat', 'Skeleton', 'SpiderQueen', 'Tengu', 'Zombie',
            'Sheep', 'Hedgehog', 'Bee', 'Wraith', 'Shadow', 'Mimic',
            'Lich', 'Monk', 'Thief', 'Shaman', 'Scorpio', 'DM300'
        ];
    }

    async selectMob(mobName) {
        try {
            // Load sprite JSON
            const jsonPath = `assets/spritesDesc/${mobName}.json`;
            const response = await fetch(jsonPath);
            const spriteData = await response.json();

            // Load texture
            const texturePath = `assets/${spriteData.texture}`;
            await this.loadTexture(texturePath);

            const currentMob = {
                name: mobName,
                data: spriteData,
                width: spriteData.width,
                height: spriteData.height
            };

            return {
                mob: currentMob,
                texture: this.textureImage
            };

        } catch (error) {
            console.error(`Failed to load ${mobName}:`, error);
            throw new Error(`Failed to load ${mobName}. Make sure you're serving this from a web server.`);
        }
    }

    async loadTexture(path) {
        return new Promise((resolve, reject) => {
            this.textureImage = new Image();
            this.textureImage.onload = () => resolve();
            this.textureImage.onerror = () => {
                console.error('Failed to load texture:', path);
                reject(new Error('Failed to load texture'));
            };
            this.textureImage.src = path;
        });
    }

    getAvailableAnimations(mobData) {
        return Object.keys(mobData).filter(key => 
            key !== 'texture' && key !== 'width' && key !== 'height' &&
            key !== 'scale' && key !== 'visualWidth' && key !== 'visualHeight' &&
            key !== 'visualOffsetX' && key !== 'visualOffsetY' &&
            key !== 'bloodColor' && key !== 'alpha' && key !== 'blendMode'
        );
    }

    getAnimationFrames(mobData, animName) {
        if (!mobData[animName]) {
            return null;
        }
        return mobData[animName];
    }
}
