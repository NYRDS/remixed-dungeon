const puppeteer = require('puppeteer');
const fs = require('fs');

(async () => {
    const browser = await puppeteer.launch({
        headless: true,
        args: ['--no-sandbox', '--enable-unsafe-swiftshader', '--use-gl=swiftshader'],
    });
    const page = await browser.newPage();
    await page.setViewport({ width: 1024, height: 600 });

    // install GL probes before any page script runs
    await page.evaluateOnNewDocument(() => {
        window.__bdLog = [];
        window.__texLog = [];
        const proto = WebGLRenderingContext.prototype;
        const bd = proto.bufferData;
        proto.bufferData = function (target, data, usage) {
            try {
                let len = -1, src = '?';
                if (data && data.buffer) { len = data.length; src = 'view'; }
                else if (typeof data === 'number') { src = 'sizeOnly'; len = data; }
                window.__bdLog.push([target, len, src, usage]);
                if (window.__bdLog.length > 4000) window.__bdLog.shift();
            } catch (e) { }
            return bd.call(this, target, data, usage);
        };
        const ti = proto.texImage2D;
        proto.texImage2D = function (target, level, ifmt, w, h, x, y, fmt, type, pixels) {
            try {
                // overload forms: (target, level, ifmt, w, h, border, fmt, type, pixels)
                let width = 0, height = 0;
                if (typeof w === 'number' && typeof h === 'number' && typeof x === 'number') {
                    width = w; height = h;
                } else if (w && w.width) { width = w.width; height = w.height; }
                const binding = this.getParameter(this.TEXTURE_BINDING_2D);
                if (width === 128 && height === 128) {
                    window.__texLog.push({ tex: binding, width, height });
                }
            } catch (e) { }
            return ti.apply(this, arguments);
        };
    });

    await page.goto('http://127.0.0.1:8081/?ep=newgame', { waitUntil: 'domcontentloaded', timeout: 60000 });

    for (let i = 0; i < 45; i++) {
        await new Promise(r => setTimeout(r, 2000));
        const st = await page.evaluate(() => (window.__gameState || {}).scene);
        if (st === 'GameScene') break;
    }
    await new Promise(r => setTimeout(r, 3000));

    // walk to the buildings
    const canvas = await page.$('canvas');
    const box = await canvas.boundingBox();
    await page.mouse.click(box.x + 300, box.y + 230); await new Promise(r => setTimeout(r, 3000));
    await page.mouse.click(box.x + 280, box.y + 200); await new Promise(r => setTimeout(r, 3000));
    await page.mouse.click(box.x + 260, box.y + 190); await new Promise(r => setTimeout(r, 4000));

    // 1. bufferData stats
    const bdStats = await page.evaluate(() => {
        const counts = {};
        for (const [target, len, src, usage] of window.__bdLog) {
            const k = `target=${target} len=${len} src=${src}`;
            counts[k] = (counts[k] || 0) + 1;
        }
        return counts;
    });
    console.log('=== bufferData call stats ===');
    Object.entries(bdStats).sort((a, b) => b[1] - a[1]).slice(0, 20).forEach(([k, v]) => console.log(v, k));

    // 2. dump every 128x128 texture uploaded (the CircleMask), read alpha channel
    const texInfo = await page.evaluate(async () => {
        const gl = document.querySelector('canvas').getContext('webgl') ||
                   document.querySelector('canvas').getContext('experimental-webgl');
        const seen = new Map();
        for (const rec of window.__texLog) {
            seen.set(rec.tex, rec);
        }
        const out = [];
        for (const [tex, rec] of seen) {
            const fbo = gl.createFramebuffer();
            gl.bindFramebuffer(gl.FRAMEBUFFER, fbo);
            gl.framebufferTexture2D(gl.FRAMEBUFFER, gl.COLOR_ATTACHMENT0, gl.TEXTURE_2D, tex, 0);
            const ok = gl.checkFramebufferStatus(gl.FRAMEBUFFER) === gl.FRAMEBUFFER_COMPLETE;
            let alphaHist = null;
            if (ok) {
                const px = new Uint8Array(128 * 128 * 4);
                gl.readPixels(0, 0, 128, 128, gl.RGBA, gl.UNSIGNED_BYTE, px);
                const hist = {};
                for (let i = 3; i < px.length; i += 4) {
                    hist[px[i]] = (hist[px[i]] || 0) + 1;
                }
                alphaHist = hist;
            }
            gl.bindFramebuffer(gl.FRAMEBUFFER, null);
            gl.deleteFramebuffer(fbo);
            out.push({ width: rec.width, height: rec.height, ok, alphaHist });
        }
        return out;
    });
    console.log('=== 128x128 textures uploaded ===');
    texInfo.forEach((t, i) => console.log(`tex#${i}`, JSON.stringify(t)));

    await browser.close();
})();
