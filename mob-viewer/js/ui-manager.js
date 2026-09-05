// UI Manager - Handles all UI interactions and updates

export class UIManager {
    constructor(spriteViewer) {
        this.viewer = spriteViewer;
    }

    hideLoading() {
        document.getElementById('loading').style.display = 'none';
    }

    setupEventListeners() {
        // Mode switching
        document.getElementById('modeMobBtn').addEventListener('click', () => {
            this.viewer.switchToMobMode();
        });
        document.getElementById('modeHeroBtn').addEventListener('click', () => {
            this.viewer.switchToHeroMode();
        });

        // Apply hero button
        document.getElementById('applyHeroBtn').addEventListener('click', () => {
            this.viewer.applyHeroConfiguration();
        });

        // Hero class change - update subclasses
        document.getElementById('heroClassSelect').addEventListener('change', (e) => {
            this.viewer.updateSubclassOptions(e.target.value);
        });

        // Animation selection
        document.getElementById('animationSelect').addEventListener('change', (e) => {
            this.viewer.currentAnim = e.target.value;
            this.viewer.resetAnimation();
        });

        // Play/Pause buttons drive the animation controller
        document.getElementById('playBtn').addEventListener('click', () => {
            this.viewer.animationController.isPlaying = true;
        });

        document.getElementById('pauseBtn').addEventListener('click', () => {
            this.viewer.animationController.isPlaying = false;
        });

        // Loop toggle
        document.getElementById('loopBtn').addEventListener('click', (e) => {
            const looping = this.viewer.animationController.toggleLoop();
            e.target.classList.toggle('active', looping);
            e.target.textContent = `🔁 Loop: ${looping ? 'ON' : 'OFF'}`;
        });

        // Scale slider
        document.getElementById('scaleSlider').addEventListener('input', (e) => {
            this.viewer.scale = parseInt(e.target.value);
            document.getElementById('scaleValue').textContent = `${this.viewer.scale}x`;
        });

        // Speed slider
        document.getElementById('speedSlider').addEventListener('input', (e) => {
            this.viewer.animationController.setSpeed(parseFloat(e.target.value));
            document.getElementById('speedValue').textContent = `${e.target.value}x`;
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

    populateMobList(mobNames, onSelectCallback) {
        const mobListEl = document.getElementById('mobList');
        mobListEl.innerHTML = '';

        for (const mobName of mobNames) {
            const li = document.createElement('li');
            li.textContent = mobName;
            li.onclick = () => onSelectCallback(mobName);
            mobListEl.appendChild(li);
        }
    }

    updateMobSelectionUI(mobName) {
        document.querySelectorAll('.mob-list li').forEach(li => {
            li.classList.remove('selected');
            if (li.textContent === mobName) {
                li.classList.add('selected');
            }
        });
    }

    updateAnimationSelect(animations, currentAnim, data) {
        const select = document.getElementById('animationSelect');
        select.innerHTML = '';

        for (const anim of animations) {
            const option = document.createElement('option');
            option.value = anim;
            option.textContent = anim.charAt(0).toUpperCase() + anim.slice(1);
            select.appendChild(option);
        }

        // Prefer idle, then any looped animation - a non-looped default (die,
        // zap) would play once and pause the controller
        if (animations.includes('idle')) {
            select.value = 'idle';
        } else {
            const firstLooped = animations.find(
                a => data && data[a] && data[a].looped);
            select.value = firstLooped || (animations.length > 0 ? animations[0] : '');
        }

        return select.value;
    }

    updateInfoPanel(name, size, texture, animations, layers = null) {
        const infoPanel = document.getElementById('animInfo');
        infoPanel.style.display = 'block';

        document.getElementById('infoName').textContent = name;
        document.getElementById('infoSize').textContent = size;
        document.getElementById('infoTexture').textContent = texture;
        document.getElementById('infoAnims').textContent = animations.join(', ');

        if (layers !== null) {
            document.getElementById('infoLayers').textContent = layers.join(', ');
        }
    }

    setupHeroSelectors(armorList, weaponList, accessoryList, subclasses) {
        // Populate armor dropdown
        const armorSelect = document.getElementById('heroArmorSelect');
        for (const armor of armorList) {
            const option = document.createElement('option');
            option.value = armor;
            option.textContent = armor.charAt(0).toUpperCase() + armor.slice(1).replace(/([A-Z])/g, ' $1');
            armorSelect.appendChild(option);
        }

        // Populate weapon dropdown
        const weaponSelect = document.getElementById('heroWeaponSelect');
        for (const weapon of weaponList) {
            const option = document.createElement('option');
            option.value = weapon;
            option.textContent = weapon.charAt(0).toUpperCase() + weapon.slice(1).replace(/([A-Z])/g, ' $1');
            weaponSelect.appendChild(option);
        }

        // Populate accessory dropdown
        const accessorySelect = document.getElementById('heroAccessorySelect');
        for (const accessory of accessoryList) {
            const option = document.createElement('option');
            option.value = accessory;
            option.textContent = accessory.charAt(0).toUpperCase() + accessory.slice(1).replace(/([A-Z])/g, ' $1');
            accessorySelect.appendChild(option);
        }
    }

    updateSubclassOptions(heroClass, subclasses) {
        const subclassSelect = document.getElementById('heroSubclassSelect');
        subclassSelect.innerHTML = '';

        const subclassList = subclasses[heroClass] || ['NONE'];
        for (const subclass of subclassList) {
            const option = document.createElement('option');
            option.value = subclass;
            option.textContent = subclass === 'NONE' ? 'None' : subclass.charAt(0).toUpperCase() + subclass.slice(1).replace(/([A-Z])/g, ' $1');
            subclassSelect.appendChild(option);
        }
    }

    getHeroConfiguration() {
        return {
            style: document.getElementById('heroStyleSelect').value,
            heroClass: document.getElementById('heroClassSelect').value,
            subClass: document.getElementById('heroSubclassSelect').value,
            armor: document.getElementById('heroArmorSelect').value,
            weapon: document.getElementById('heroWeaponSelect').value,
            accessory: document.getElementById('heroAccessorySelect').value
        };
    }
}
