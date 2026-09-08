// detect first-attack lag: boots ?ep=newgame&level=0 (random sewer, rats
// roam), samples the render heartbeat for freeze gaps >250ms and correlates
// them with GLog lines (attacks, sounds, effects) from the error ring.
const puppeteer = require('puppeteer');

const sleep = ms => new Promise(r => setTimeout(r, ms));

(async () => {
    const browser = await puppeteer.launch({ headless: true, args: ['--no-sandbox', '--enable-unsafe-swiftshader', '--use-gl=swiftshader'] });
    const page = await browser.newPage();
    await page.setViewport({ width: 1024, height: 600 });
    await page.goto('http://127.0.0.1:8081/?ep=newgame&level=0', { waitUntil: 'domcontentloaded', timeout: 60000 });
    for (let i = 0; i < 45; i++) {
        await sleep(2000);
        const st = await page.evaluate(() => ({ s: (window.__gameState || {}).scene, r: window.__rafTicks }));
        if (st.s === 'GameScene' && st.r > 50) break;
    }
    await sleep(5000);
    console.log('in GameScene, watching for freezes (wandering + occasional hero moves)...');

    const canvas = await page.$('canvas');
    const box = await canvas.boundingBox();
    const click = (x, y) => page.mouse.click(box.x + x, box.y + y);
    const moves = [[397, 300], [500, 380], [300, 380], [450, 300], [350, 320], [550, 330], [400, 350], [480, 300]];

    let lastFrame = await page.evaluate(() => (window.__gameState || {}).frame || 0);
    let lastNewFrameWall = Date.now();
    let lastLogCount = 0;
    const t0 = Date.now();
    let warmed = false;
    for (let i = 0; i < 400; i++) { // ~70s of watching
        await sleep(175);
        if (i % 32 === 0) { // ~5.6s: nudge the hero somewhere to meet mobs
            const m = moves[(i / 32) % moves.length];
            click(m[0], m[1]);
        }
        const st = await page.evaluate(() => ({
            frame: (window.__gameState || {}).frame || 0,
            errs: (window.__errors || []).map(String).filter(e => /attack|hit|hits|miss|defend|dodge|snd_|Sample|Sound/i.test(e)),
        }));
        const now = Date.now();
        if (st.frame !== lastFrame) {
            if (warmed) {
                const gap = now - lastNewFrameWall;
                if (gap > 300) {
                    console.log(`FREEZE t=${now - t0} gap=${gap}ms  logs: ${JSON.stringify(st.errs.slice(lastLogCount).slice(-4))}`);
                }
            }
            warmed = true;
            lastFrame = st.frame;
            lastNewFrameWall = now;
            lastLogCount = st.errs.length;
        } else if (st.errs.length > lastLogCount) {
            console.log(`t=${now - t0} logs(no new frame): ${JSON.stringify(st.errs.slice(lastLogCount).slice(-3))}`);
            lastLogCount = st.errs.length;
        }
    }
    console.log('done');
    await browser.close();
})();
