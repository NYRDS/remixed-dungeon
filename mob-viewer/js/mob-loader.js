// Mob Loader - Handles loading mob sprite data

export class MobLoader {
    constructor() {
        this.textureImage = null;
        this.availableMobs = [];
        this.mobTexturesMap = {}; // Maps mob name to texture path
    }

    async loadMobList() {
        try {
            // Fetch the list of sprite JSON files dynamically
            const response = await fetch('assets/spritesDesc/');
            if (response.ok) {
                const text = await response.text();
                // Parse directory listing if available (some servers provide this)
                const parser = new DOMParser();
                const doc = parser.parseFromString(text, 'text/html');
                const links = doc.querySelectorAll('a[href$=".json"]');
                if (links.length > 0) {
                    this.availableMobs = Array.from(links).map(link => 
                        decodeURIComponent(link.getAttribute('href').replace('.json', ''))
                    );
                }
            }
        } catch (error) {
            console.log('Directory listing not available, using fallback method...');
        }
        
        // If directory listing didn't work, use predefined list as fallback
        if (this.availableMobs.length === 0) {
            await this.loadMobNamesFromConfig();
        }
        
        return this.availableMobs;
    }
    
    async loadMobNamesFromConfig() {
        // Predefined list of available mobs based on actual JSON files
        this.availableMobs = [
            'Acidic', 'AirElemental', 'Albino', 'ArtificerNPC', 'AzuterronNPC',
            'Bandit', 'BardNPC', 'BarmanNPC', 'Bat', 'Bee', 'BeeHive', 'BellaNPC',
            'BishopNPC', 'BlackCat', 'BlackRat', 'Blacksmith', 'Brute', 'BurningFist',
            'CagedKobold', 'ColdSpirit', 'Crab', 'Crystal', 'DM300', 'DeathKnight',
            'Deathling', 'DeepSnail', 'DreadKnight', 'DrunkardNPC', 'EarthElemental',
            'EnslavedSoul', 'ExplodingSkull', 'Eye', 'FetidRat', 'FireElemental',
            'FortuneTellerNPC', 'Ghost', 'Gnoll', 'GoldenStatue', 'Golem', 'Goo',
            'HealerNPC', 'Hedgehog', 'IceElemental', 'IceGuardian', 'IceGuardianCore',
            'Imp', 'InnKeeperNPC', 'InquirerNPC', 'JarOfSouls', 'King', 'Kobold',
            'KoboldIcemancer', 'Larva', 'LibrarianNPC', 'Lich', 'MercenaryNPC',
            'Mimic', 'MimicAmulet', 'MimicPie', 'Monk', 'NecromancerNPC', 'Nightmare',
            'Piranha', 'PlagueDoctorNPC', 'PseudoRat', 'Rat', 'RatKing', 'RottingFist',
            'RunicSkull', 'ScarecrowNPC', 'Scorpio', 'Senior', 'ServantNPC',
            'ServiceManNPC', 'Shadow', 'ShadowLord', 'Shaman', 'ShamanElder', 'Sheep',
            'Shielded', 'Shopkeeper', 'Skeleton', 'Snail', 'SociologistNPC',
            'SpiderEgg', 'SpiderElite', 'SpiderExploding', 'SpiderGuard', 'SpiderMind',
            'SpiderMindAmber', 'SpiderNest', 'SpiderQueen', 'SpiderServant', 'Spinner',
            'SpiritOfPain', 'Statue', 'Succubus', 'SuspiciousRat', 'Swarm', 'Tengu',
            'Thief', 'TownGuardNPC', 'TownsfolkMovieNPC', 'TownsfolkNPC',
            'TownsfolkSilentNPC', 'TreacherousSpirit', 'Undead', 'WandMaker',
            'Warlock', 'WaterElemental', 'Worm', 'Wraith', 'YogsBrain', 'YogsEye',
            'YogsHeart', 'YogsTeeth', 'Zombie', 'ZombieGnoll'
        ];
    }

    async selectMob(mobName) {
        try {
            // Load sprite JSON
            const jsonPath = `assets/spritesDesc/${mobName}.json`;
            const jsonResponse = await fetch(jsonPath);
            if (!jsonResponse.ok) {
                throw new Error(`Sprite JSON not found: ${jsonPath}`);
            }
            const spriteData = await jsonResponse.json();

            // Build texture path from the texture field in JSON
            let texturePath = `assets/${spriteData.texture}`;
            
            // Verify texture exists before loading
            const textureExists = await this.checkResourceExists(texturePath);
            if (!textureExists) {
                console.warn(`Texture not found at ${texturePath}, checking alternative paths...`);
                
                // Try alternative paths for mobs that use subdirectories
                const textureName = spriteData.texture;
                const altPaths = [
                    `assets/mobs/${textureName}`,
                    `assets/${textureName.toLowerCase()}`,
                    `assets/mobs/${textureName.toLowerCase()}`
                ];
                
                for (const altPath of altPaths) {
                    if (await this.checkResourceExists(altPath)) {
                        texturePath = altPath;
                        break;
                    }
                }
            }
            
            await this.loadTexture(texturePath);

            const currentMob = {
                name: mobName,
                data: spriteData,
                width: spriteData.width,
                height: spriteData.height,
                texturePath: texturePath
            };

            return {
                mob: currentMob,
                texture: this.textureImage
            };

        } catch (error) {
            console.error(`Failed to load ${mobName}:`, error);
            throw new Error(`Failed to load ${mobName}: ${error.message}`);
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
