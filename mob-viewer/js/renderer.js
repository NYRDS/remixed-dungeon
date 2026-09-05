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
        const frameWidth = currentHero.width;
        const frameHeight = currentHero.height;

        const scaledWidth = frameWidth * scale;
        const scaledHeight = frameHeight * scale;

        const baseX = (this.canvas.width - scaledWidth) / 2;
        const baseY = (this.canvas.height - scaledHeight) / 2;

        this.ctx.imageSmoothingEnabled = false;

        // layers are pre-sorted into z-order by the hero loader; each layer
        // sheet has its own geometry (modern sheets are 1024px/32 cols,
        // retro 512px/36 cols of 14x17 cells)
        for (const layerName of heroLayers) {
            const layerImg = heroTextures[layerName];
            if (!layerImg) {
                continue;
            }
            const framesInRow = Math.max(1, Math.floor(layerImg.width / frameWidth));
            const frameX = (frameIndex % framesInRow) * frameWidth;
            const frameY = Math.floor(frameIndex / framesInRow) * frameHeight;

            this.ctx.drawImage(
                layerImg,
                frameX, frameY,
                frameWidth, frameHeight,
                baseX, baseY,
                scaledWidth, scaledHeight
            );
        }
    }

    drawGrid(x, y, w, h) {
        this.ctx.strokeStyle = 'rgba(255, 0, 0, 0.3)';
        this.ctx.lineWidth = 1;
        this.ctx.strokeRect(x, y, w, h);
    }
}
