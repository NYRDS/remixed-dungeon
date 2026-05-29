// Mob Animation Viewer - Main Application
class SpriteViewer {
    constructor() {
        this.canvas = document.getElementById('spriteCanvas');
        this.ctx = this.canvas.getContext('2d');
        this.mobs = [];
        this.currentMob = null;
        this.currentAnim = 'idle';
        this.isPlaying = true;
        this.isLooping = true;
        this.scale = 4;
        this.speedMultiplier = 1;
        this.animationFrame = 0;
        this.lastFrameTime = 0;
        this.textureImage = null;
        
        this.init();
    }
    
    async init() {
        await this.loadMobList();
        this.setupEventListeners();
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
    
    resetAnimation() {
        this.animationFrame = 0;
        this.lastFrameTime = performance.now();
    }
    
    getCurrentAnimation() {
        if (!this.currentMob || !this.currentMob.data[this.currentAnim]) {
            return null;
        }
        return this.currentMob.data[this.currentAnim];
    }
    
    render() {
        // Clear canvas
        this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
        
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
        
        // Draw grid overlay (optional, for debugging)
        // this.drawGrid(x, y, scaledWidth, scaledHeight);
    }
    
    drawGrid(x, y, w, h) {
        this.ctx.strokeStyle = 'rgba(255, 0, 0, 0.3)';
        this.ctx.lineWidth = 1;
        this.ctx.strokeRect(x, y, w, h);
    }
    
    updateAnimation(currentTime) {
        if (!this.isPlaying || !this.currentMob) {
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
