// Renderer - Handles rendering mobs and heroes to canvas

export class Renderer {
    constructor(canvas) {
        this.canvas = canvas;
        this.ctx = canvas.getContext('2d');
    }

    clear() {
        this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
    }

    renderMob(currentMob, textureImage, currentAnim, animationFrame, scale) {
        if (!currentMob || !textureImage) {
            return;
        }

        const anim = currentMob.data[currentAnim];
        if (!anim) {
            return;
        }

        const frameIndex = anim.frames[animationFrame];
        const framesInRow = Math.floor(textureImage.width / currentMob.width);

        const frameX = (frameIndex % framesInRow) * currentMob.width;
        const frameY = Math.floor(frameIndex / framesInRow) * currentMob.height;

        const scaledWidth = currentMob.width * scale;
        const scaledHeight = currentMob.height * scale;

        const x = (this.canvas.width - scaledWidth) / 2;
        const y = (this.canvas.height - scaledHeight) / 2;

        // Draw the frame
        this.ctx.imageSmoothingEnabled = false;
        this.ctx.drawImage(
            textureImage,
            frameX, frameY,
            currentMob.width, currentMob.height,
            x, y,
            scaledWidth, scaledHeight
        );
    }

    renderHero(currentHero, heroTextures, heroLayers, currentAnim, animationFrame, scale) {
        if (!currentHero || Object.keys(heroTextures).length === 0) {
            return;
        }

        const anim = currentHero.data[currentAnim];
        if (!anim) {
            return;
        }

        const frameIndex = anim.frames[animationFrame];
        const framesInRow = 8; // Standard frames per row for hero sprites

        const frameX = (frameIndex % framesInRow) * currentHero.width;
        const frameY = Math.floor(frameIndex / framesInRow) * currentHero.height;

        // Use visual dimensions if available (for proper centering)
        const visualWidth = currentHero.visualWidth || currentHero.width;
        const visualHeight = currentHero.visualHeight || currentHero.height;
        const offsetX = currentHero.visualOffsetX || 0;
        const offsetY = currentHero.visualOffsetY || 0;

        const scaledWidth = visualWidth * scale;
        const scaledHeight = visualHeight * scale;

        // Center the sprite with offset
        const baseX = (this.canvas.width - scaledWidth) / 2 + offsetX * scale;
        const baseY = (this.canvas.height - scaledHeight) / 2 + offsetY * scale;

        this.ctx.imageSmoothingEnabled = false;

        // Render each layer in order
        for (const layerName of heroLayers) {
            const layerImg = heroTextures[layerName];
            if (layerImg) {
                this.ctx.drawImage(
                    layerImg,
                    frameX, frameY,
                    currentHero.width, currentHero.height,
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
}
