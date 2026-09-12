// first-attack lag attribution: boots ?ep=newgame&level=0&fight=1 (a mob is
// teleported adjacent to the hero and set hunting), freeze-detects via the
// render heartbeat, and runs the CDP CPU profiler across the fight so the
// hot functions behind any stall show up by name.
const puppeteer = require('puppeteer');

const sleep = ms => new Promise(r => setTimeout(r, ms));

(async () => {
    const browser = await puppeteer.launch({ headless: true, args: ['--no-sandbox', '--enable-unsafe-swiftshader', '--use-gl=swiftshader'] });
    const page = await browser.newPage();
    await page.setViewport({ width: 1024, height: 600 });
    await page.goto('http://127.0.0.1:8081/?ep=newgame&level=0&fight=1', { waitUntil: 'domcontentloaded', timeout: 60000 });
    for (let i = 0; i < 45; i++) {
        await sleep(2000);
        const st = await page.evaluate(() => ({ s: (window.__gameState || {}).scene, r: window.__rafTicks }));
        if (st.s === 'GameScene' && st.r > 50) break;
    }
    // wait for the fight marker
    for (let i = 0; i < 20; i++) {
        await sleep(1000);
        const ok = await page.evaluate(() => (window.__errors || []).map(String).some(e => e.includes('fight:')));
        if (ok) break;
    }
    await sleep(2000);
    console.log('fight staged, profiling...');

    // the debug capture shim toDataURLs the canvas every frame - stub it so
    // it doesn't dominate the profile
    await page.evaluate(() => {
        HTMLCanvasElement.prototype.toDataURL = function () { return 'data:,stub'; };
    });

    const client = await page.createCDPSession();
    await client.send('Profiler.enable');
    await client.send('Profiler.start');

    let lastFrame = await page.evaluate(() => (window.__gameState || {}).frame || 0);
    let lastNewFrameWall = Date.now();
    let warmed = false;
    const freezes = [];
    const t0 = Date.now();
    for (let i = 0; i < 200; i++) { // ~35s
        await sleep(100);
        const st = await page.evaluate(() => ({
            frame: (window.__gameState || {}).frame || 0,
            atk: (window.__errors || []).map(String).filter(e => /t=\d+ (enter|hit-rolled|done \(|miss-done|post-sound)|snd load/.test(e)),
            snd: window.__sndEvents || [],
        }));
        const now = Date.now();
        if (st.frame !== lastFrame) {
            if (warmed) {
                const gap = now - lastNewFrameWall;
                if (gap > 150) {
                    freezes.push({ t: now - t0, gap });
                    const tNow = performance ? '' : '';
                    console.log(`FREEZE t=${now - t0} gap=${gap}ms  atkLogsNear: ${JSON.stringify(st.atk.slice(-3))}  sndTail: ${JSON.stringify(st.snd.slice(-3))}`);
                }
            }
            warmed = true;
            lastFrame = st.frame;
            lastNewFrameWall = now;
        }
    }
    console.log('freezes:', JSON.stringify(freezes));
    const final = await page.evaluate(() => ({
        snd: window.__sndEvents || [],
        atk: (window.__errors || []).map(String).filter(e => /t=\d+ (enter|hit-rolled|done \(|miss-done|post-sound)|snd load/.test(e)),
    }));
    console.log('sound timeline:', JSON.stringify(final.snd, null, 0));
    final.atk.forEach(a => console.log('ATK:', a.slice(0, 130)));

    const { profile } = await client.send('Profiler.stop');
    console.log('freezes:', JSON.stringify(freezes));

    // aggregate self time per function
    const nodes = new Map(profile.nodes.map(n => [n.id, n]));
    const self = new Map();
    for (const id of Object.keys(profile.samples || {})) {
        const nid = profile.samples[id];
        const node = nodes.get(nid);
        if (!node) continue;
        const name = (node.callFrame.functionName || '(anon)') + ' @' + (node.callFrame.url || '').split('/').pop() + ':' + node.callFrame.lineNumber;
        self.set(name, (self.get(name) || 0) + 1);
    }
    const top = [...self.entries()].sort((a, b) => b[1] - a[1]).slice(0, 25);
    top.forEach(([name, samples]) => console.log(String(samples).padStart(6), name));

    await browser.close();
})();
