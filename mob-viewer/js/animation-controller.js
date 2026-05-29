// Animation Controller - Manages animation state and timing

export class AnimationController {
    constructor() {
        this.animationFrame = 0;
        this.lastFrameTime = 0;
        this.isPlaying = true;
        this.isLooping = true;
        this.speedMultiplier = 1;
    }

    reset() {
        this.animationFrame = 0;
        this.lastFrameTime = performance.now();
    }

    update(currentTime, currentData, currentAnim) {
        if (!this.isPlaying) {
            return false;
        }

        const anim = currentData[currentAnim];
        if (!anim) {
            return false;
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
                    return true; // Animation completed
                }
            }

            this.lastFrameTime = currentTime;
            return true; // Frame updated
        }

        return false; // No update
    }

    getCurrentFrame() {
        return this.animationFrame;
    }

    setFrame(frame) {
        this.animationFrame = frame;
    }

    togglePlay() {
        this.isPlaying = !this.isPlaying;
        return this.isPlaying;
    }

    toggleLoop() {
        this.isLooping = !this.isLooping;
        if (!this.isLooping) {
            this.animationFrame = 0;
        }
        return this.isLooping;
    }

    setSpeed(multiplier) {
        this.speedMultiplier = multiplier;
    }
}
