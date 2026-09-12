// fps + audio diag: boot ?ep=newgame&level=1, measure frame rate over a
// window, count audio elements, log >=400 responses and audio element errors.
const puppeteer = require('puppeteer');

const sleep = ms => new Promise(r => setTimeout(r, ms));
const url = process.argv[2] || 'http://127.0.0.1:8081/?ep=newgame&level=1';

(async () => {
    const browser = await puppeteer.launch({ headless: true, args: ['--no-sandbox', '--enable-unsafe-swiftshader', '--use-gl=swiftshader'] });
    const page = await browser.newPage();
    await page.setViewport({ width: 1024, height: 600 });

    const bad = [];
    page.on('response', r => { if (r.status() >= 400) bad.push(r.status() + ' ' + r.url()); });

    await page.evaluateOnNewDocument(() => {
        window.__audioCreated = 0;
        window.__audioErrors = [];
        const orig = document.createElement.bind(document);
        document.createElement = function(tag) {
            const el = orig(String(tag).toLowerCase());
            if (String(tag).toLowerCase() === 'audio') {
                window.__audioCreated++;
                el.addEventListener('error', () => window.__audioErrors.push(el.src + ' code=' + (el.error ? el.error.code : '?')));
            }
            return el;
        };
    });

    console.log('booting', url);
    await page.goto(url, { waitUntil: 'domcontentloaded', timeout: 60000 });

    let scene = '';
    for (let i = 0; i < 60; i++) {
        await sleep(2000);
        scene = await page.evaluate(() => (window.__gameState || {}).scene || '?');
        if (scene === 'GameScene') { console.log(`GameScene at t=${i * 2}s`); break; }
        if (i % 5 === 0) console.log(`t=${i * 2}s scene=${scene}`);
    }

    const count = () => page.evaluate(() => window.__rafTicks);
    const t0 = await count();
    await sleep(10000);
    const t1 = await count();

    const diag = await page.evaluate(() => ({
        scene: (window.__gameState || {}).scene,
        raf: window.__rafTicks,
        rafShim: window.__rafShimStats,
        audioCreated: window.__audioCreated,
        audioErrors: (window.__audioErrors || []).slice(0, 10),
        blockedAudio: (window.__blockedAudio || []).length,
        jsHeapMB: performance.memory ? Math.round(performance.memory.usedJSHeapSize / 1048576) : -1,
        errors: (window.__errors || []).length,
        lastErrors: (window.__errors || []).slice(-8).map(String),
    }));

    console.log('frames in 10s window:', t1 - t0, `(~${(t1 - t0) / 10} fps)`);
    console.log(JSON.stringify(diag, null, 1));
    if (bad.length) {
        console.log('HTTP >=400 (first 15):');
        bad.slice(0, 15).forEach(b => console.log(' ', b));
        console.log('total bad responses:', bad.length);
    } else {
        console.log('no HTTP >=400');
    }

    await browser.close();
})();
