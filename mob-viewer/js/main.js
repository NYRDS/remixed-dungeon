// Main Application - SpriteViewer

import { MobLoader } from './mob-loader.js';
import { HeroLoader } from './hero-loader.js';
import { Renderer } from './renderer.js';
import { UIManager } from './ui-manager.js';
import { AnimationController } from './animation-controller.js';
import { HERO_CLASSES, SUBCLASSES, ARMOR_LIST, WEAPON_LIST, ACCESSORY_LIST, LAYERS_ORDER } from './config.js';

class SpriteViewer {
    constructor() {
        this.canvas = document.getElementById('spriteCanvas');
        
        // State
        this.currentMob = null;
        this.currentHero = null;
        this.currentAnim = 'idle';
        this.scale = 4;
        this.isHeroMode = false;
        
        // Component instances
        this.mobLoader = new MobLoader();
        this.heroLoader = new HeroLoader();
        this.renderer = new Renderer(this.canvas);
        this.uiManager = new UIManager(this);
        this.animationController = new AnimationController();
        
        // Hero textures and layers (managed by heroLoader but cached here for rendering)
        this.heroTextures = {};
        this.heroLayers = [];
        
        this.init();
    }
    
    async init() {
        const mobNames = await this.mobLoader.loadMobList();
        this.uiManager.setupEventListeners();
        this.uiManager.setupHeroSelectors(ARMOR_LIST, WEAPON_LIST, ACCESSORY_LIST, SUBCLASSES);
        this.uiManager.populateMobList(mobNames, (mobName) => this.selectMob(mobName));
        
        // Load first mob by default
        if (mobNames.length > 0) {
            await this.selectMob(mobNames[0]);
        }
        
        this.uiManager.hideLoading();
        this.startRenderLoop();
    }
    
    async selectMob(mobName) {
        try {
            this.uiManager.updateMobSelectionUI(mobName);
            
            const result = await this.mobLoader.selectMob(mobName);
            
            this.currentMob = result.mob;
            this.currentHero = null;
            
            const animations = this.mobLoader.getAvailableAnimations(this.currentMob.data);
            this.currentAnim = this.uiManager.updateAnimationSelect(animations, this.currentAnim);
            
            this.uiManager.updateInfoPanel(
                this.currentMob.name,
                `${this.currentMob.width}x${this.currentMob.height}`,
                this.currentMob.data.texture,
                animations
            );
            
            this.animationController.reset();
            
        } catch (error) {
            console.error(`Failed to load ${mobName}:`, error);
            alert(error.message);
        }
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
    
    updateSubclassOptions(heroClass) {
        this.uiManager.updateSubclassOptions(heroClass, SUBCLASSES);
    }
    
    async applyHeroConfiguration() {
        const config = this.uiManager.getHeroConfiguration();
        await this.loadHero(config.style, config.heroClass, config.subClass, config.armor, config.weapon, config.accessory);
    }
    
    async loadHero(style, heroClass, subClass, armor, weapon, accessory) {
        try {
            const result = await this.heroLoader.loadHero(style, heroClass, subClass, armor, weapon, accessory);
            
            this.currentHero = result.hero;
            this.heroTextures = result.textures;
            this.heroLayers = result.layers;
            this.currentMob = null;
            
            const animations = Object.keys(this.currentHero.data).filter(key =>
                typeof this.currentHero.data[key] === 'object' &&
                this.currentHero.data[key].frames
            );
            
            this.currentAnim = this.uiManager.updateAnimationSelect(animations, this.currentAnim);
            
            this.uiManager.updateInfoPanel(
                this.currentHero.name,
                `${this.currentHero.width}x${this.currentHero.height}`,
                this.currentHero.style + ' hero',
                animations,
                this.heroLayers
            );
            
            this.animationController.reset();
            
        } catch (error) {
            console.error(`Failed to load hero:`, error);
            alert(`Failed to load hero configuration.`);
        }
    }
    
    resetAnimation() {
        this.animationController.reset();
    }
    
    getCurrentData() {
        if (this.isHeroMode) {
            return this.currentHero ? this.currentHero.data : null;
        } else {
            return this.currentMob ? this.currentMob.data : null;
        }
    }
    
    render() {
        this.renderer.clear();
        
        if (this.isHeroMode) {
            if (this.currentHero) {
                this.renderer.renderHero(
                    this.currentHero,
                    this.heroTextures,
                    this.heroLayers,
                    this.currentAnim,
                    this.animationController.getCurrentFrame(),
                    this.scale
                );
            }
        } else {
            if (this.currentMob) {
                this.renderer.renderMob(
                    this.currentMob,
                    this.mobLoader.textureImage,
                    this.currentAnim,
                    this.animationController.getCurrentFrame(),
                    this.scale
                );
            }
        }
    }
    
    updateAnimation(currentTime) {
        const data = this.getCurrentData();
        if (!data) {
            return;
        }
        
        this.animationController.update(currentTime, data, this.currentAnim);
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
