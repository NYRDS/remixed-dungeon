// Headed real-Chrome check: boots the game on a real display/GPU/audio stack
// (headless lacks performance.measureMemory and real codecs, so the gcHint
// path never ran in headless testing). Measures fps in GameScene, the rAF
// shim split, heap, and measureMemory cost. Closes the browser when done.
const puppeteer = require('puppeteer');

const sleep = ms => new Promise(r => setTimeout(r, ms));
const url = process.argv[2] || 'http://127.0.0.1:8081/?ep=newgame&level=1';
const display = process.argv[3] || ':1';

(async () => {
    const browser = await puppeteer.launch({
        headless: false,
        executablePath: '/usr/bin/chromium-browser',
        args: [
            '--no-sandbox',
            '--window-position=60,60',
            '--window-size=1100,700',
            '--autoplay-policy=no-user-gesture-required',
            '--use-gl=angle',
        ],
        env: { ...process.env, DISPLAY: display },
    });
    const page = await browser.newPage();
    await page.setViewport({ width: 1024, height: 600 });

    await page.evaluateOnNewDocument(() => {
        window.__mmCalls = [];
        if (window.performance && performance.measureMemory) {
            const omm = performance.measureMemory.bind(performance);
            performance.measureMemory = function(opts) {
                const t0 = Date.now();
                const p = omm(opts);
                p['finally'](() => window.__mmCalls.push(Date.now() - t0));
                return p;
            };
        }
    });

    console.log('booting (headed):', url);
    await page.goto(url, { waitUntil: 'domcontentloaded', timeout: 60000 });

    let scene = '';
    for (let i = 0; i < 60; i++) {
        await sleep(2000);
        scene = await page.evaluate(() => (window.__gameState || {}).scene || '?');
        if (scene === 'GameScene') { console.log(`GameScene at t=${i * 2}s`); break; }
        if (i % 5 === 0) console.log(`t=${i * 2}s scene=${scene}`);
    }

    // settle 5s, then measure fps over 15s
    await sleep(5000);
    const f0 = await page.evaluate(() => (window.__gameState || {}).frames || 0);
    await sleep(15000);
    const f1 = await page.evaluate(() => (window.__gameState || {}).frames || 0);

    const diag = await page.evaluate(() => ({
        scene: (window.__gameState || {}).scene,
        vis: document.visibilityState,
        fps: window.__fps,
        raf: window.__rafShimStats,
        mmCalls: window.__mmCalls || 'no measureMemory',
        heapMB: performance.memory ? Math.round(performance.memory.usedJSHeapSize / 1048576) : -1,
        audioCreated: (document.querySelectorAll('audio') || []).length,
        perfErrors: (window.__errors || []).filter(e => String(e).includes('[perf]')).slice(-10),
        errCount: (window.__errors || []).length,
    }));

    console.log(`frames in 15s: ${f1 - f0} (~${((f1 - f0) / 15).toFixed(1)} fps)`);
    console.log(JSON.stringify(diag, null, 1));

    await page.screenshot({ path: '/tmp/headed_check.png' });
    await browser.close();
    console.log('done, screenshot /tmp/headed_check.png');
})().catch(e => { console.error('FAILED:', e.message); process.exit(1); });
