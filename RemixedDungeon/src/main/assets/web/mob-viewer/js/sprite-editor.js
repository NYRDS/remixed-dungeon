// Sprite Editor - pixel-level editor for one sprite sheet, opened from the
// animation viewer. Edits a frame of the sheet (grid geometry identical to
// the game's sheet layout) and saves the WHOLE sheet back as PNG.

import { isGameMode, saveSprite, reloadGameTextures } from './data-source.js';

const MAX_UNDO = 50;

// hex + alpha -> [r,g,b,a]; '' (empty swatch) = fully transparent
export function colorToRgba(color, alpha) {
    if (!color) {
        return [0, 0, 0, 0];
    }
    const v = parseInt(color.slice(1), 16);
    return [(v >> 16) & 255, (v >> 8) & 255, v & 255, alpha === undefined ? 255 : alpha];
}

function rgbaToHex(r, g, b) {
    return '#' + [r, g, b].map(c => c.toString(16).padStart(2, '0')).join('');
}

export class SpriteEditor {
    constructor(onSaved) {
        this.onSaved = onSaved; // callback(modPath) after a successful save
        this.overlay = document.getElementById('editorOverlay');
        this.sheetNav = document.getElementById('editorSheetNav');
        this.frameView = document.getElementById('editorFrame');
        this.frameBox = document.getElementById('editorFrameBox'); // scrollable area

        this.sheet = null;      // offscreen canvas with the full sheet
        this.sheetCtx = this.sheet ? null : null;
        this.frameW = 0;
        this.frameH = 0;
        this.cols = 0;
        this.rows = 0;
        this.modPath = null;    // save target, mod-relative ('bat.png')
        this.animFrames = [];   // frame indices of the current animation

        this.selectedFrame = 0;
        this.zoom = 16;
        this.tool = 'pencil';
        this.color = '#e94560';
        this.alpha = 255;
        this.palette = [];
        this.undoStack = [];
        this.redoStack = [];
        this.dirty = false;
        this.painting = false;

        this.bindEvents();
    }

    // ---- open / close -------------------------------------------------

    open(sheetImage, frameW, frameH, modPath, animFrames, selectedFrame) {
        this.frameW = frameW;
        this.frameH = frameH;
        this.modPath = modPath;
        this.animFrames = animFrames || [];
        this.selectedFrame = selectedFrame || 0;

        this.sheet = document.createElement('canvas');
        this.sheet.width = sheetImage.width;
        this.sheet.height = sheetImage.height;
        this.sheetCtx = this.sheet.getContext('2d', { willReadFrequently: true });
        this.sheetCtx.imageSmoothingEnabled = false;
        this.sheetCtx.drawImage(sheetImage, 0, 0);

        this.cols = Math.floor(this.sheet.width / frameW);
        this.rows = Math.floor(this.sheet.height / frameH);
        if (this.cols < 1 || this.rows < 1) {
            alert('Sheet geometry does not fit a single ' + frameW + 'x' + frameH + ' frame');
            return;
        }

        this.undoStack = [];
        this.redoStack = [];
        this.dirty = false;

        this.extractPalette();
        this.renderPalette();
        this.updateZoom(0); // clamps + sizes the frame canvas
        this.drawSheetNav();
        this.drawFrame();

        const saveBtn = document.getElementById('editorSaveBtn');
        if (isGameMode() && this.modPath) {
            saveBtn.textContent = '💾 Save to game';
            saveBtn.title = 'Overwrites ' + this.modPath + ' in the active mod';
        } else {
            saveBtn.textContent = '⬇ Download PNG';
            saveBtn.title = this.modPath
                ? 'No game server - downloads ' + this.modPath
                : 'Downloads the edited sheet';
        }

        this.overlay.style.display = 'flex';
    }

    close() {
        this.overlay.style.display = 'none';
        this.painting = false;
    }

    isOpen() {
        return this.overlay.style.display === 'flex';
    }

    // ---- geometry -----------------------------------------------------

    frameRect(frameIndex) {
        return {
            x: (frameIndex % this.cols) * this.frameW,
            y: Math.floor(frameIndex / this.cols) * this.frameH
        };
    }

    getFrameImageData() {
        const r = this.frameRect(this.selectedFrame);
        return this.sheetCtx.getImageData(r.x, r.y, this.frameW, this.frameH);
    }

    putFrameImageData(imageData) {
        const r = this.frameRect(this.selectedFrame);
        this.sheetCtx.putImageData(imageData, r.x, r.y);
    }

    // ---- undo ---------------------------------------------------------

    pushUndo() {
        this.undoStack.push(this.getFrameImageData());
        if (this.undoStack.length > MAX_UNDO) {
            this.undoStack.shift();
        }
        this.redoStack = [];
    }

    undo() {
        if (!this.undoStack.length) {
            return;
        }
        this.redoStack.push(this.getFrameImageData());
        this.putFrameImageData(this.undoStack.pop());
        this.dirty = true;
        this.refresh();
    }

    redo() {
        if (!this.redoStack.length) {
            return;
        }
        this.undoStack.push(this.getFrameImageData());
        this.putFrameImageData(this.redoStack.pop());
        this.dirty = true;
        this.refresh();
    }

    // ---- painting -----------------------------------------------------

    bindEvents() {
        document.getElementById('editorCloseBtn').addEventListener('click', () => this.close());
        document.getElementById('editorUndoBtn').addEventListener('click', () => this.undo());
        document.getElementById('editorRedoBtn').addEventListener('click', () => this.redo());
        document.getElementById('editorZoomIn').addEventListener('click', () => this.updateZoom(4));
        document.getElementById('editorZoomOut').addEventListener('click', () => this.updateZoom(-4));
        document.getElementById('editorSaveBtn').addEventListener('click', () => this.save());
        document.getElementById('editorPickColor').addEventListener('input', (e) => {
            this.color = e.target.value;
            this.markActiveSwatch();
        });
        document.getElementById('editorAlpha').addEventListener('input', (e) => {
            this.alpha = parseInt(e.target.value, 10);
        });

        for (const [tool, btnId] of [
            ['pencil', 'toolPencil'], ['eraser', 'toolEraser'],
            ['picker', 'toolPicker'], ['fill', 'toolFill']
        ]) {
            document.getElementById(btnId).addEventListener('click', () => {
                this.tool = tool;
                this.updateToolButtons();
            });
        }
        this.updateToolButtons();

        // sheet navigation: click a cell to select the frame
        this.sheetNav.addEventListener('click', (e) => {
            const rect = this.sheetNav.getBoundingClientRect();
            const scale = this.sheet.width / rect.width;
            const px = (e.clientX - rect.left) * scale;
            const py = (e.clientY - rect.top) * (this.sheet.height / rect.height);
            const col = Math.floor(px / this.frameW);
            const row = Math.floor(py / this.frameH);
            if (col >= 0 && col < this.cols && row >= 0 && row < this.rows) {
                this.selectedFrame = row * this.cols + col;
                this.drawSheetNav();
                this.drawFrame();
            }
        });

        // frame painting: pointer events cover mouse + touch
        this.frameView.addEventListener('pointerdown', (e) => {
            e.preventDefault();
            const px = this.framePixelFromEvent(e);
            if (!px) {
                return;
            }
            if (this.tool === 'picker') {
                this.pickColorAt(px.x, px.y);
                return;
            }
            this.pushUndo();
            this.painting = true;
            this.paintAt(px.x, px.y);
            this.refresh();
        });
        this.frameView.addEventListener('pointermove', (e) => {
            if (!this.painting) {
                return;
            }
            const px = this.framePixelFromEvent(e);
            if (px) {
                this.paintAt(px.x, px.y);
                this.refresh();
            }
        });
        window.addEventListener('pointerup', () => {
            this.painting = false;
        });

        window.addEventListener('keydown', (e) => {
            if (!this.isOpen()) {
                return;
            }
            if (e.ctrlKey && e.key === 'z') { this.undo(); e.preventDefault(); }
            else if (e.ctrlKey && e.key === 'y') { this.redo(); e.preventDefault(); }
            else if (e.key === 'b') { this.tool = 'pencil'; this.updateToolButtons(); }
            else if (e.key === 'e') { this.tool = 'eraser'; this.updateToolButtons(); }
            else if (e.key === 'i') { this.tool = 'picker'; this.updateToolButtons(); }
            else if (e.key === 'f') { this.tool = 'fill'; this.updateToolButtons(); }
            else if (e.key === 'Escape') { this.close(); }
        });
    }

    framePixelFromEvent(e) {
        const rect = this.frameView.getBoundingClientRect();
        const x = Math.floor((e.clientX - rect.left) / this.zoom);
        const y = Math.floor((e.clientY - rect.top) / this.zoom);
        if (x < 0 || y < 0 || x >= this.frameW || y >= this.frameH) {
            return null;
        }
        return { x, y };
    }

    paintAt(x, y) {
        this.dirty = true;
        if (this.tool === 'fill') {
            this.floodFill(x, y);
            return;
        }
        const erase = this.tool === 'eraser';
        const [r, g, b, a] = erase ? [0, 0, 0, 0] : colorToRgba(this.color, this.alpha);
        const img = this.getFrameImageData();
        const i = (y * this.frameW + x) * 4;
        img.data[i] = r;
        img.data[i + 1] = g;
        img.data[i + 2] = b;
        img.data[i + 3] = a;
        this.putFrameImageData(img);
    }

    floodFill(x, y) {
        const img = this.getFrameImageData();
        const d = img.data;
        const idx = (y * this.frameW + x) * 4;
        const target = [d[idx], d[idx + 1], d[idx + 2], d[idx + 3]];
        const [r, g, b, a] = colorToRgba(this.color, this.alpha);
        const same = (i) => d[i] === target[0] && d[i + 1] === target[1]
            && d[i + 2] === target[2] && d[i + 3] === target[3];
        const stack = [[x, y]];
        while (stack.length) {
            const [cx, cy] = stack.pop();
            if (cx < 0 || cy < 0 || cx >= this.frameW || cy >= this.frameH) {
                continue;
            }
            const i = (cy * this.frameW + cx) * 4;
            if (!same(i)) {
                continue;
            }
            d[i] = r; d[i + 1] = g; d[i + 2] = b; d[i + 3] = a;
            stack.push([cx + 1, cy], [cx - 1, cy], [cx, cy + 1], [cx, cy - 1]);
        }
        this.putFrameImageData(img);
    }

    pickColorAt(x, y) {
        const img = this.getFrameImageData();
        const i = (y * this.frameW + x) * 4;
        if (img.data[i + 3] === 0) {
            this.color = '';
            this.markActiveSwatch();
            return;
        }
        this.color = rgbaToHex(img.data[i], img.data[i + 1], img.data[i + 2]);
        document.getElementById('editorPickColor').value = this.color;
        this.markActiveSwatch();
    }

    // ---- palette ------------------------------------------------------

    extractPalette() {
        const img = this.getFrameImageData();
        const seen = new Map(); // hex -> count
        for (let i = 0; i < img.data.length; i += 4) {
            if (img.data[i + 3] < 128) {
                continue; // treat semi-transparent as transparent
            }
            const hex = rgbaToHex(img.data[i], img.data[i + 1], img.data[i + 2]);
            seen.set(hex, (seen.get(hex) || 0) + 1);
        }
        this.palette = [...seen.entries()]
            .sort((a, b) => b[1] - a[1])
            .slice(0, 32)
            .map(e => e[0]);
    }

    renderPalette() {
        const holder = document.getElementById('editorPalette');
        holder.innerHTML = '';
        this.markActiveSwatch();
        for (const hex of this.palette) {
            const sw = document.createElement('button');
            sw.className = 'swatch';
            sw.style.background = hex;
            sw.title = hex;
            sw.dataset.color = hex;
            sw.addEventListener('click', () => {
                this.color = hex;
                document.getElementById('editorPickColor').value = hex;
                this.markActiveSwatch();
            });
            holder.appendChild(sw);
        }
    }

    markActiveSwatch() {
        document.querySelectorAll('#editorPalette .swatch').forEach(sw => {
            sw.classList.toggle('active', sw.dataset.color === this.color);
        });
    }

    updateToolButtons() {
        for (const [tool, btnId] of [
            ['pencil', 'toolPencil'], ['eraser', 'toolEraser'],
            ['picker', 'toolPicker'], ['fill', 'toolFill']
        ]) {
            document.getElementById(btnId).classList.toggle('active', this.tool === tool);
        }
        this.frameView.style.cursor = this.tool === 'picker' ? 'crosshair' : 'cell';
    }

    // ---- rendering ----------------------------------------------------

    updateZoom(delta) {
        this.zoom = Math.min(32, Math.max(4, this.zoom + delta));
        this.frameView.width = this.frameW * this.zoom;
        this.frameView.height = this.frameH * this.zoom;
        document.getElementById('editorZoomLabel').textContent = this.zoom + 'x';
        this.drawFrame();
    }

    // sheet thumbnail with frame grid + selection + animation highlight
    drawSheetNav() {
        const ctx = this.sheetNav.getContext('2d');
        const w = 260;
        const h = Math.max(1, Math.round(this.sheet.height * (w / this.sheet.width)));
        this.sheetNav.width = w;
        this.sheetNav.height = h;
        ctx.imageSmoothingEnabled = false;
        ctx.clearRect(0, 0, w, h);
        ctx.drawImage(this.sheet, 0, 0, w, h);

        const cw = w / this.cols;
        const ch = h / this.rows;
        for (let i = 0; i < this.cols * this.rows; i++) {
            const col = i % this.cols;
            const row = Math.floor(i / this.cols);
            if (this.animFrames.includes(i)) {
                ctx.fillStyle = 'rgba(78, 204, 163, 0.18)';
                ctx.fillRect(col * cw, row * ch, cw, ch);
            }
            ctx.strokeStyle = 'rgba(255,255,255,0.15)';
            ctx.strokeRect(col * cw, row * ch, cw, ch);
        }
        const sel = this.frameRect(this.selectedFrame);
        ctx.strokeStyle = '#e94560';
        ctx.lineWidth = 2;
        ctx.strokeRect((sel.x / this.sheet.width) * w, (sel.y / this.sheet.height) * h,
            cw, ch);
    }

    drawFrame() {
        const ctx = this.frameView.getContext('2d');
        const z = this.zoom;
        ctx.imageSmoothingEnabled = false;
        ctx.clearRect(0, 0, this.frameView.width, this.frameView.height);

        const r = this.frameRect(this.selectedFrame);
        ctx.drawImage(this.sheet, r.x, r.y, this.frameW, this.frameH, 0, 0,
            this.frameW * z, this.frameH * z);

        // pixel grid from 8x zoom up
        if (z >= 8) {
            ctx.strokeStyle = 'rgba(255,255,255,0.08)';
            ctx.lineWidth = 1;
            for (let x = 1; x < this.frameW; x++) {
                ctx.beginPath(); ctx.moveTo(x * z, 0); ctx.lineTo(x * z, this.frameH * z); ctx.stroke();
            }
            for (let y = 1; y < this.frameH; y++) {
                ctx.beginPath(); ctx.moveTo(0, y * z); ctx.lineTo(this.frameW * z, y * z); ctx.stroke();
            }
        }
    }

    refresh() {
        this.drawFrame();
        this.drawSheetNav();
    }

    // ---- save ---------------------------------------------------------

    async save() {
        const dataUrl = this.sheet.toDataURL('image/png');
        if (!(isGameMode() && this.modPath)) {
            // no game server: hand out the file
            const a = document.createElement('a');
            a.href = dataUrl;
            a.download = this.modPath ? this.modPath.split('/').pop() : 'sprite.png';
            a.click();
            return;
        }
        const btn = document.getElementById('editorSaveBtn');
        btn.disabled = true;
        try {
            await saveSprite(this.modPath, dataUrl.substring('data:image/png;base64,'.length));
            this.dirty = false;
            await reloadGameTextures();
            if (this.onSaved) {
                this.onSaved(this.modPath);
            }
        } catch (e) {
            alert('Save failed: ' + e.message);
        } finally {
            btn.disabled = false;
        }
    }
}
