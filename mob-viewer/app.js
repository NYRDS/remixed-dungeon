// Mob & Hero Animation Viewer - Main Application
class SpriteViewer {
    constructor() {
        this.canvas = document.getElementById('spriteCanvas');
        this.ctx = this.canvas.getContext('2d');
        this.mobs = [];
        this.currentMob = null;
        this.currentHero = null;
        this.currentAnim = 'idle';
        this.isPlaying = true;
        this.isLooping = true;
        this.scale = 4;
        this.speedMultiplier = 1;
        this.animationFrame = 0;
        this.lastFrameTime = 0;
        this.textureImage = null;
        this.heroTextures = {}; // Map of layer names to loaded images
        this.heroLayers = [];   // Ordered list of layer names for current hero
        this.isHeroMode = false;
        
        // Hero configuration data
        this.heroClasses = ['WARRIOR', 'MAGE', 'ROGUE', 'HUNTRESS', 'ELF', 'NECROMANCER', 'GNOLL', 'PRIEST', 'DOCTOR'];
        this.subclasses = {
            'WARRIOR': ['NONE', 'GLADIATOR', 'BERSERKER'],
            'MAGE': ['NONE', 'WARLOCK', 'BATTLEMAGE'],
            'ROGUE': ['NONE', 'ASSASSIN', 'FREERUNNER'],
            'HUNTRESS': ['NONE', 'SNIPER', 'WARDEN'],
            'ELF': ['NONE', 'SCOUT', 'SHAMAN'],
            'NECROMANCER': ['NONE', 'LICH'],
            'GNOLL': ['NONE', 'GUARDIAN', 'WITCHDOCTOR'],
            'PRIEST': ['NONE', 'CLERIC', 'PALADIN'],
            'DOCTOR': ['NONE', 'ALCHEMIST', 'TRANSMUTER']
        };
        this.armorList = ['none', 'cloth', 'leather', 'mail', 'scale', 'plate', 'gothic', 
                          'rogue', 'warrior', 'mage', 'huntress', 'scout', 'shaman', 
                          'gladiator', 'berserk', 'warlock', 'battlemage', 'assasin', 
                          'freerunner', 'sniper', 'warden', 'necromancer', 'lich', 
                          'gnoll', 'spider', 'rat', 'chaos', 'elf', 'necromancerRobe'];
        this.weaponList = ['none', 'shortsword', 'longsword', 'dagger', 'mace', 'hammer', 
                           'sword', 'wand', 'boomerang', 'bow', 'crossbow', 'spear', 
                           'glaive', 'battleaxe', 'claymore', 'quarterstaff', 'knuckles',
                           'bonesaw', 'tomahawk', 'halberd', 'kusarigama', 'pickaxe'];
        this.accessoryList = ['none', 'plaguedoctormask', 'wizardhat', 'nightcap', 'ushanka',
                              'santahat', 'pumpkin', 'fez', 'shades', 'fullfacemask',
                              'pirateset', 'dogemask', 'nekoears', 'rabbitears', 'rudolph',
                              'vampireskull', 'krampushead', 'zombiemask', 'filteredmask',
                              'medicineMask', 'bowknot', 'capotain', 'chaoshelmet'];
        
        this.init();
    }
    
    async init() {
        await this.loadMobList();
        this.setupEventListeners();
        this.setupHeroSelectors();
        this.hideLoading();
        this.startRenderLoop();
    }
    
    hideLoading() {
        document.getElementById('loading').style.display = 'none';
    }
    
    async loadMobList() {
        try {
            // Fetch the list of sprite JSON files
            const response = await fetch('assets/spritesDesc/');
            
            // This won't work directly in browser, so we'll use a predefined list
            // In production, you'd need a server endpoint or build step
            await this.loadMobData();
        } catch (error) {
            console.log('Directory listing not available, loading mob data directly...');
            await this.loadMobData();
        }
    }
    
    async loadMobData() {
        // List of available mobs based on the JSON files found
        const mobNames = [
            'Bat', 'Brute', 'Crab', 'Eye', 'Ghost', 'Gnoll', 'Golem', 'Goo',
            'King', 'Rat', 'Skeleton', 'SpiderQueen', 'Tengu', 'Zombie',
            'Sheep', 'Hedgehog', 'Bee', 'Wraith', 'Shadow', 'Mimic',
            'Lich', 'Monk', 'Thief', 'Shaman', 'Scorpio', 'DM300'
        ];
        
        const mobListEl = document.getElementById('mobList');
        mobListEl.innerHTML = '';
        
        for (const mobName of mobNames) {
            const li = document.createElement('li');
            li.textContent = mobName;
            li.onclick = () => this.selectMob(mobName);
            mobListEl.appendChild(li);
        }
        
        // Load first mob by default
        if (mobNames.length > 0) {
            await this.selectMob(mobNames[0]);
        }
    }
    
    async selectMob(mobName) {
        // Update UI selection
        document.querySelectorAll('.mob-list li').forEach(li => {
            li.classList.remove('selected');
            if (li.textContent === mobName) {
                li.classList.add('selected');
            }
        });
        
        try {
            // Load sprite JSON
            const jsonPath = `assets/spritesDesc/${mobName}.json`;
            const response = await fetch(jsonPath);
            const spriteData = await response.json();
            
            // Load texture
            const texturePath = `assets/${spriteData.texture}`;
            await this.loadTexture(texturePath);
            
            this.currentMob = {
                name: mobName,
                data: spriteData,
                width: spriteData.width,
                height: spriteData.height
            };
            
            this.updateAnimationSelect();
            this.updateInfoPanel();
            this.resetAnimation();
            
        } catch (error) {
            console.error(`Failed to load ${mobName}:`, error);
            alert(`Failed to load ${mobName}. Make sure you're serving this from a web server.`);
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
    
    updateAnimationSelect() {
        const select = document.getElementById('animationSelect');
        select.innerHTML = '';
        
        const animations = ['idle', 'run', 'attack', 'die'];
        const availableAnims = Object.keys(this.currentMob.data).filter(key => 
            key !== 'texture' && key !== 'width' && key !== 'height' &&
            key !== 'scale' && key !== 'visualWidth' && key !== 'visualHeight' &&
            key !== 'visualOffsetX' && key !== 'visualOffsetY' &&
            key !== 'bloodColor' && key !== 'alpha' && key !== 'blendMode'
        );
        
        for (const anim of availableAnims) {
            const option = document.createElement('option');
            option.value = anim;
            option.textContent = anim.charAt(0).toUpperCase() + anim.slice(1);
            select.appendChild(option);
        }
        
        // Set to idle if available, otherwise first animation
        if (availableAnims.includes('idle')) {
            select.value = 'idle';
        } else if (availableAnims.length > 0) {
            select.value = availableAnims[0];
        }
        
        this.currentAnim = select.value;
    }
    
    updateInfoPanel() {
        const infoPanel = document.getElementById('animInfo');
        infoPanel.style.display = 'block';
        
        document.getElementById('infoName').textContent = this.currentMob.name;
        document.getElementById('infoSize').textContent = `${this.currentMob.width}x${this.currentMob.height}`;
        document.getElementById('infoTexture').textContent = this.currentMob.data.texture;
        
        const animations = Object.keys(this.currentMob.data).filter(key => 
            typeof this.currentMob.data[key] === 'object' && 
            this.currentMob.data[key].frames
        );
        document.getElementById('infoAnims').textContent = animations.join(', ');
    }
    
    setupEventListeners() {
        // Mode switching
        document.getElementById('modeMobBtn').addEventListener('click', () => {
            this.switchToMobMode();
        });
        document.getElementById('modeHeroBtn').addEventListener('click', () => {
            this.switchToHeroMode();
        });
        
        // Apply hero button
        document.getElementById('applyHeroBtn').addEventListener('click', () => {
            this.applyHeroConfiguration();
        });
        
        // Hero class change - update subclasses
        document.getElementById('heroClassSelect').addEventListener('change', (e) => {
            this.updateSubclassOptions(e.target.value);
        });
        
        // Animation selection
        document.getElementById('animationSelect').addEventListener('change', (e) => {
            this.currentAnim = e.target.value;
            this.resetAnimation();
        });
        
        // Play/Pause buttons
        document.getElementById('playBtn').addEventListener('click', () => {
            this.isPlaying = true;
        });
        
        document.getElementById('pauseBtn').addEventListener('click', () => {
            this.isPlaying = false;
        });
        
        // Loop toggle
        document.getElementById('loopBtn').addEventListener('click', (e) => {
            this.isLooping = !this.isLooping;
            e.target.classList.toggle('active');
            e.target.textContent = `🔁 Loop: ${this.isLooping ? 'ON' : 'OFF'}`;
            if (!this.isLooping) {
                this.animationFrame = 0;
            }
        });
        
        // Scale slider
        document.getElementById('scaleSlider').addEventListener('input', (e) => {
            this.scale = parseInt(e.target.value);
            document.getElementById('scaleValue').textContent = `${this.scale}x`;
        });
        
        // Speed slider
        document.getElementById('speedSlider').addEventListener('input', (e) => {
            this.speedMultiplier = parseFloat(e.target.value);
            document.getElementById('speedValue').textContent = `${this.speedMultiplier}x`;
        });
        
        // Search box
        document.getElementById('searchBox').addEventListener('input', (e) => {
            const searchTerm = e.target.value.toLowerCase();
            document.querySelectorAll('.mob-list li').forEach(li => {
                const mobName = li.textContent.toLowerCase();
                li.style.display = mobName.includes(searchTerm) ? 'block' : 'none';
            });
        });
    }
    
    switchToMobMode() {
        this.isHeroMode = false;
        document.getElementById('modeMobBtn').classList.add('active');
        document.getElementById('modeHeroBtn').classList.remove('active');
        document.getElementById('mobSection').style.display = 'block';
        document.getElementById('heroSection').style.display = 'none';
        document.getElementById('infoLayersRow').style.display = 'none';
    }
    
    switchToHeroMode() {
        this.isHeroMode = true;
        document.getElementById('modeMobBtn').classList.remove('active');
        document.getElementById('modeHeroBtn').classList.add('active');
        document.getElementById('mobSection').style.display = 'none';
        document.getElementById('heroSection').style.display = 'block';
        document.getElementById('infoLayersRow').style.display = 'flex';
    }
    
    setupHeroSelectors() {
        // Populate armor dropdown
        const armorSelect = document.getElementById('heroArmorSelect');
        for (const armor of this.armorList) {
            const option = document.createElement('option');
            option.value = armor;
            option.textContent = armor.charAt(0).toUpperCase() + armor.slice(1).replace(/([A-Z])/g, ' $1');
            armorSelect.appendChild(option);
        }
        
        // Populate weapon dropdown
        const weaponSelect = document.getElementById('heroWeaponSelect');
        for (const weapon of this.weaponList) {
            const option = document.createElement('option');
            option.value = weapon;
            option.textContent = weapon.charAt(0).toUpperCase() + weapon.slice(1).replace(/([A-Z])/g, ' $1');
            weaponSelect.appendChild(option);
        }
        
        // Populate accessory dropdown
        const accessorySelect = document.getElementById('heroAccessorySelect');
        for (const accessory of this.accessoryList) {
            const option = document.createElement('option');
            option.value = accessory;
            option.textContent = accessory.charAt(0).toUpperCase() + accessory.slice(1).replace(/([A-Z])/g, ' $1');
            accessorySelect.appendChild(option);
        }
        
        // Initialize subclass options for first class
        this.updateSubclassOptions('WARRIOR');
    }
    
    updateSubclassOptions(heroClass) {
        const subclassSelect = document.getElementById('heroSubclassSelect');
        subclassSelect.innerHTML = '';
        
        const subclasses = this.subclasses[heroClass] || ['NONE'];
        for (const subclass of subclasses) {
            const option = document.createElement('option');
            option.value = subclass;
            option.textContent = subclass === 'NONE' ? 'None' : subclass.charAt(0).toUpperCase() + subclass.slice(1).replace(/([A-Z])/g, ' $1');
            subclassSelect.appendChild(option);
        }
    }
    
    async applyHeroConfiguration() {
        const style = document.getElementById('heroStyleSelect').value;
        const heroClass = document.getElementById('heroClassSelect').value;
        const subClass = document.getElementById('heroSubclassSelect').value;
        const armor = document.getElementById('heroArmorSelect').value;
        const weapon = document.getElementById('heroWeaponSelect').value;
        const accessory = document.getElementById('heroAccessorySelect').value;
        
        await this.loadHero(style, heroClass, subClass, armor, weapon, accessory);
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
            
            // Define layer order (matching ModernHeroSpriteDef.java)
            const layersOrder = [
                'right_back_item', 'left_back_item',
                'body', 'collar', 'head', 'hair', 'armor', 'armor_boots',
                'facial_hair', 'helmet', 'left_hand', 'right_hand',
                'left_hand_armor', 'right_hand_armor', 'accessory',
                'left_hand_item', 'right_hand_item'
            ];
            
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
            this.currentHero = {
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
            
            this.updateAnimationSelect();
            this.updateHeroInfoPanel();
            this.resetAnimation();
            
        } catch (error) {
            console.error(`Failed to load hero:`, error);
            alert(`Failed to load hero configuration.`);
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
        // Based on ModernHeroSpriteDef.bodyType mapping
        const bodyTypeMap = {
            'WARLOCK': 'warlock',
            'LICH': 'lich',
            'GNOLL': 'gnoll',
            'HUNTRESS': 'woman'
        };
        
        // Check subclass first, then class
        if (bodyTypeMap[subClass]) return bodyTypeMap[subClass];
        if (bodyTypeMap[heroClass]) return bodyTypeMap[heroClass];
        
        // Default based on class
        if (heroClass === 'HUNTRESS') return 'woman';
        if (heroClass === 'NECROMANCER' && subClass === 'LICH') return 'lich';
        if (heroClass === 'GNOLL') return 'gnoll';
        
        return style === 'modern' ? 'man' : 'man';
    }
    
    updateHeroInfoPanel() {
        const infoPanel = document.getElementById('animInfo');
        infoPanel.style.display = 'block';
        
        document.getElementById('infoName').textContent = this.currentHero.name;
        document.getElementById('infoSize').textContent = `${this.currentHero.width}x${this.currentHero.height}`;
        document.getElementById('infoTexture').textContent = this.currentHero.style + ' hero';
        
        const animations = Object.keys(this.currentHero.data).filter(key => 
            typeof this.currentHero.data[key] === 'object' && 
            this.currentHero.data[key].frames
        );
        document.getElementById('infoAnims').textContent = animations.join(', ');
        
        document.getElementById('infoLayers').textContent = this.heroLayers.join(', ');
    }
    
    resetAnimation() {
        this.animationFrame = 0;
        this.lastFrameTime = performance.now();
    }
    
    getCurrentAnimation() {
        if (this.isHeroMode) {
            if (!this.currentHero || !this.currentHero.data[this.currentAnim]) {
                return null;
            }
            return this.currentHero.data[this.currentAnim];
        } else {
            if (!this.currentMob || !this.currentMob.data[this.currentAnim]) {
                return null;
            }
            return this.currentMob.data[this.currentAnim];
        }
    }
    
    render() {
        // Clear canvas
        this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
        
        if (this.isHeroMode) {
            this.renderHero();
        } else {
            this.renderMob();
        }
    }
    
    renderMob() {
        if (!this.currentMob || !this.textureImage) {
            return;
        }
        
        const anim = this.getCurrentAnimation();
        if (!anim) {
            return;
        }
        
        const frameIndex = anim.frames[this.animationFrame];
        const framesInRow = Math.floor(this.textureImage.width / this.currentMob.width);
        
        const frameX = (frameIndex % framesInRow) * this.currentMob.width;
        const frameY = Math.floor(frameIndex / framesInRow) * this.currentMob.height;
        
        const scaledWidth = this.currentMob.width * this.scale;
        const scaledHeight = this.currentMob.height * this.scale;
        
        const x = (this.canvas.width - scaledWidth) / 2;
        const y = (this.canvas.height - scaledHeight) / 2;
        
        // Draw the frame
        this.ctx.imageSmoothingEnabled = false;
        this.ctx.drawImage(
            this.textureImage,
            frameX, frameY,
            this.currentMob.width, this.currentMob.height,
            x, y,
            scaledWidth, scaledHeight
        );
    }
    
    renderHero() {
        if (!this.currentHero || Object.keys(this.heroTextures).length === 0) {
            return;
        }
        
        const anim = this.getCurrentAnimation();
        if (!anim) {
            return;
        }
        
        const frameIndex = anim.frames[this.animationFrame];
        const framesInRow = 8; // Standard frames per row for hero sprites
        
        const frameX = (frameIndex % framesInRow) * this.currentHero.width;
        const frameY = Math.floor(frameIndex / framesInRow) * this.currentHero.height;
        
        // Use visual dimensions if available (for proper centering)
        const visualWidth = this.currentHero.visualWidth || this.currentHero.width;
        const visualHeight = this.currentHero.visualHeight || this.currentHero.height;
        const offsetX = this.currentHero.visualOffsetX || 0;
        const offsetY = this.currentHero.visualOffsetY || 0;
        
        const scaledWidth = visualWidth * this.scale;
        const scaledHeight = visualHeight * this.scale;
        
        // Center the sprite with offset
        const baseX = (this.canvas.width - scaledWidth) / 2 + offsetX * this.scale;
        const baseY = (this.canvas.height - scaledHeight) / 2 + offsetY * this.scale;
        
        this.ctx.imageSmoothingEnabled = false;
        
        // Render each layer in order
        for (const layerName of this.heroLayers) {
            const layerImg = this.heroTextures[layerName];
            if (layerImg) {
                this.ctx.drawImage(
                    layerImg,
                    frameX, frameY,
                    this.currentHero.width, this.currentHero.height,
                    baseX, baseY,
                    scaledWidth, scaledHeight
                );
            }
        }
    }
    
    drawGrid(x, y, w, h) {
        this.ctx.strokeStyle = 'rgba(255, 0, 0, 0.3)';
        this.ctx.lineWidth = 1;
        this.ctx.strokeRect(x, y, w, h);
    }
    
    updateAnimation(currentTime) {
        if (!this.isPlaying) {
            return;
        }
        
        // Check for appropriate current object based on mode
        if (this.isHeroMode && !this.currentHero) {
            return;
        }
        if (!this.isHeroMode && !this.currentMob) {
            return;
        }
        
        const anim = this.getCurrentAnimation();
        if (!anim) {
            return;
        }
        
        const frameDelay = (1000 / anim.fps) / this.speedMultiplier;
        
        if (currentTime - this.lastFrameTime >= frameDelay) {
            this.animationFrame++;
            
            if (this.animationFrame >= anim.frames.length) {
                if (this.isLooping && anim.looped) {
                    this.animationFrame = 0;
                } else {
                    this.animationFrame = anim.frames.length - 1;
                    this.isPlaying = false;
                }
            }
            
            this.lastFrameTime = currentTime;
        }
    }
    
    startRenderLoop() {
        const loop = (currentTime) => {
            this.updateAnimation(currentTime);
            this.render();
            requestAnimationFrame(loop);
        };
        requestAnimationFrame(loop);
    }
}

// Initialize the application when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    window.spriteViewer = new SpriteViewer();
});
