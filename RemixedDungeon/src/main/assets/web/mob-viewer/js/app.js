// App entry - the SpriteViewer state machine wiring loader, renderer,
// animation controller, UI manager and the sprite editor together.

import { AnimationController } from './animation-controller.js';
import { Renderer } from './renderer.js';
import { MobLoader } from './mob-loader.js';
import { HeroLoader } from './hero-loader.js';
import { UIManager } from './ui-manager.js';
import { SpriteEditor } from './sprite-editor.js';
import { detectGameServer, getMode, setMode, isGameMode,
         modRelativePath, bustTextures } from './data-source.js';
import { HERO_CLASSES, SUBCLASSES, ARMOR_LIST, WEAPON_LIST } from './config.js';

class SpriteViewer {
    constructor() {
        this.canvas = document.getElementById('viewerCanvas');
        this.renderer = new Renderer(this.canvas);
        this.animationController = new AnimationController();
        this.mobLoader = new MobLoader();
        this.heroLoader = new HeroLoader();
        this.spriteEditor = new SpriteEditor(() => this.onSpriteSaved());

        this.mode = 'mob';
        this.currentAnim = 'idle';
        this.scale = 4;

        this.currentMob = null;
        this.texture = null;
        this.currentHero = null;
        this.heroTextures = {};
        this.heroLayers = [];
        this.heroLayerPaths = {};
        this.currentMobName = null;

        this.uiManager = new UIManager(this);
        this.uiManager.setupEventListeners();
        this.uiManager.setupHeroSelectors(ARMOR_LIST, WEAPON_LIST, SUBCLASSES);

        // hero class dropdown + default subclass list
        const classSelect = document.getElementById('heroClassSelect');
        for (const cls of HERO_CLASSES) {
            const option = document.createElement('option');
            option.value = cls;
            option.textContent = cls.charAt(0) + cls.slice(1).toLowerCase();
            classSelect.appendChild(option);
        }
        this.updateSubclassOptions('WARRIOR');
    }

    // ---- boot ----------------------------------------------------------

    async init() {
        const gameUp = await detectGameServer();
        if (gameUp) {
            // game mode is only selectable when the page IS served by the game
            const sel = document.getElementById('dataSourceSelect');
            const gameOption = document.createElement('option');
            gameOption.value = 'game';
            gameOption.textContent = 'Game server';
            sel.insertBefore(gameOption, sel.firstChild);
        } else if (getMode() === 'game') {
            setMode('local'); // stale preference, game server is gone
        }
        document.getElementById('dataSourceSelect').value = getMode();

        try {
            const mobs = await this.mobLoader.loadMobList();
            if (mobs.length === 0) {
                throw new Error('no mobs listed');
            }
            this.uiManager.populateMobList(mobs, (name) => this.selectMob(name));
            await this.selectMob(mobs.includes('Rat') ? 'Rat' : mobs[0]);
        } catch (e) {
            console.error('boot failed:', e);
            document.getElementById('loading').textContent = 'Failed to load mobs: ' + e.message;
            return;
        }

        document.getElementById('loading').style.display = 'none';
        requestAnimationFrame((t) => this.loop(t));
    }

    async reloadData() {
        // data source switched - reload whatever is on screen
        if (this.mode === 'mob') {
            if (this.currentMobName) {
                await this.selectMob(this.currentMobName);
            }
        } else {
            await this.applyHeroConfiguration();
        }
    }

    // ---- mode switching -------------------------------------------------

    switchToMobMode() {
        this.mode = 'mob';
        document.getElementById('modeMobBtn').classList.add('active');
        document.getElementById('modeHeroBtn').classList.remove('active');
        document.getElementById('mobSection').style.display = 'block';
        document.getElementById('heroSection').style.display = 'none';
        document.getElementById('editSpriteBtn').style.display = '';
        document.getElementById('editLayerSelect').style.display = 'none';
        this.animationController.reset();
        this.renderFrame();
    }

    switchToHeroMode() {
        this.mode = 'hero';
        document.getElementById('modeHeroBtn').classList.add('active');
        document.getElementById('modeMobBtn').classList.remove('active');
        document.getElementById('mobSection').style.display = 'none';
        document.getElementById('heroSection').style.display = 'block';
        document.getElementById('editSpriteBtn').style.display = 'none';
        this.animationController.reset();
        this.renderFrame();
    }

    // ---- mob mode -------------------------------------------------------

    async selectMob(mobName) {
        try {
            const { mob, texture } = await this.mobLoader.selectMob(mobName);
            this.currentMob = mob;
            this.texture = texture;
            this.currentMobName = mobName;
            this.currentAnim = this.pickDefaultAnim(mob.data);
            this.animationController.reset();

            this.uiManager.updateMobSelectionUI(mobName);
            this.uiManager.updateAnimationSelect(
                this.mobLoader.getAvailableAnimations(mob.data),
                this.currentAnim, mob.data);
            this.refreshLoopIndicator();
            this.uiManager.updateInfoPanel(
                mob.name,
                mob.width + 'x' + mob.height,
                mob.texturePath.split('/').pop(),
                this.mobLoader.getAvailableAnimations(mob.data));
            this.renderFrame();
        } catch (e) {
            console.error('selectMob failed:', e);
            alert('Failed to load ' + mobName + ': ' + e.message);
        }
    }

    // ---- hero mode ------------------------------------------------------

    updateSubclassOptions(heroClass) {
        this.uiManager.updateSubclassOptions(heroClass, SUBCLASSES);
    }

    async applyHeroConfiguration() {
        const cfg = this.uiManager.getHeroConfiguration();
        try {
            const { hero, textures, layers, layerPaths } =
                await this.heroLoader.loadHero(cfg.style, cfg.heroClass, cfg.subClass,
                    cfg.armor, cfg.weapon, cfg.leftWeapon);
            this.currentHero = hero;
            this.heroTextures = textures;
            this.heroLayers = layers;
            this.heroLayerPaths = layerPaths || {};
            this.currentAnim = this.pickDefaultAnim(hero.data);
            this.animationController.reset();

            this.uiManager.updateAnimationSelect(
                Object.keys(hero.data).filter((k) => k !== 'width' && k !== 'height' && k !== 'layersOrder'),
                this.currentAnim, hero.data);
            this.refreshLoopIndicator();
            this.uiManager.updateInfoPanel(
                hero.name,
                hero.width + 'x' + hero.height,
                hero.style + ' layer stack',
                Object.keys(hero.data),
                layers);
            this.populateEditLayerSelect();
            this.renderFrame();
        } catch (e) {
            console.error('applyHero failed:', e);
            alert('Failed to build hero: ' + e.message);
        }
    }

    populateEditLayerSelect() {
        const sel = document.getElementById('editLayerSelect');
        sel.innerHTML = '';
        for (const layer of this.heroLayers) {
            const option = document.createElement('option');
            option.value = layer;
            option.textContent = layer;
            sel.appendChild(option);
        }
        sel.style.display = this.heroLayers.length ? '' : 'none';
        // hero mode hides the edit button until there is a layer to edit
        document.getElementById('editSpriteBtn').style.display =
            this.heroLayers.length ? '' : 'none';
    }

    // ---- animation ------------------------------------------------------

    pickDefaultAnim(data) {
        const keys = Object.keys(data).filter((k) =>
            data[k] && typeof data[k] === 'object' && Array.isArray(data[k].frames));
        if (keys.includes('idle')) {
            return 'idle';
        }
        return keys.find((k) => data[k].looped) || (keys[0] || '');
    }

    resetAnimation() {
        this.animationController.reset();
        this.renderFrame();
    }

    refreshLoopIndicator() {
        this.uiManager.refreshLoopIndicator();
    }

    getCurrentData() {
        if (this.mode === 'mob') {
            return this.currentMob ? this.currentMob.data : null;
        }
        return this.currentHero ? this.currentHero.data : null;
    }

    loop(now) {
        const data = this.getCurrentData();
        if (data && this.animationController.update(now, data, this.currentAnim)) {
            this.renderFrame();
        }
        requestAnimationFrame((t) => this.loop(t));
    }

    renderFrame() {
        this.renderer.clear();
        if (this.mode === 'mob' && this.currentMob) {
            this.renderer.renderMob(this.currentMob, this.texture, this.currentAnim,
                this.animationController.getCurrentFrame(), this.scale);
        } else if (this.mode === 'hero' && this.currentHero) {
            this.renderer.renderHero(this.currentHero, this.heroTextures, this.heroLayers,
                this.currentAnim, this.animationController.getCurrentFrame(), this.scale);
        }
    }

    // ---- sprite editor ---------------------------------------------------

    openEditor() {
        const data = this.getCurrentData();
        if (!data || !data[this.currentAnim]) {
            alert('No animation selected');
            return;
        }
        const anim = data[this.currentAnim];
        const frameIdx = Math.min(this.animationController.getCurrentFrame(),
            anim.frames.length - 1);
        const sheetFrame = anim.frames[frameIdx];

        if (this.mode === 'mob' && this.currentMob) {
            // data.texture is the mod-relative path ('bat.png'); texturePath is
            // already a resolved URL and must not leak into the save target
            this.spriteEditor.open(this.texture, this.currentMob.width, this.currentMob.height,
                modRelativePath('assets/' + this.currentMob.data.texture), anim.frames, sheetFrame);
        } else if (this.mode === 'hero' && this.currentHero) {
            const layer = document.getElementById('editLayerSelect').value || this.heroLayers[0];
            if (!layer || !this.heroTextures[layer]) {
                alert('No layer selected');
                return;
            }
            this.spriteEditor.open(this.heroTextures[layer], this.currentHero.width,
                this.currentHero.height, modRelativePath(this.heroLayerPaths[layer]),
                anim.frames, sheetFrame);
        }
    }

    async onSpriteSaved() {
        // the sheet changed on the game side - reload textures (cache-busted
        // via bustTextures in data-source) and rebuild the current view
        bustTextures();
        if (this.mode === 'mob' && this.currentMobName) {
            await this.selectMob(this.currentMobName);
        } else if (this.mode === 'hero') {
            await this.applyHeroConfiguration();
        }
    }
}

const viewer = new SpriteViewer();
document.getElementById('editSpriteBtn').addEventListener('click', () => viewer.openEditor());
document.getElementById('editLayerSelect').addEventListener('change', () => viewer.renderFrame());
viewer.init();
