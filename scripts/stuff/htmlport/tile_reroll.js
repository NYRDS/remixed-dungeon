// isometric tile-variant stability check: boots ?ep=newgame&level=1
// (level 1, _xyz tileset), captures frames around hero actions and diffs
// them per 16px block. Before the TRandom fix every action re-rolled every
// visible tile variant (map-wide diff); after it only fog-ring/actor
// blocks change.
const puppeteer = require('puppeteer');

const sleep = ms => new Promise(r => setTimeout(r, ms));

// diff the last two frames pushed into window.__diffStore
const diffLastPair = page => page.evaluate(() => {
    const store = window.__diffStore || [];
    if (store.length < 2) return Promise.resolve({ error: 'need 2 frames' });
    const [sa, sb] = store.slice(-2);
    const load = src => new Promise(res => {
        const i = new Image();
        i.onload = () => res(i);
        i.src = src; // onload attached BEFORE src
    });
    return Promise.all([load(sa), load(sb)]).then(([ia, ib]) => {
        const w = ia.width, h = ia.height;
        const mk = i => {
            const c = document.createElement('canvas');
            c.width = w; c.height = h;
            c.getContext('2d').drawImage(i, 0, 0);
            return c.getContext('2d').getImageData(0, 0, w, h).data;
        };
        const da = mk(ia), db = mk(ib);
        const B = 16;
        let blocks = 0, changed = 0, maxDelta = 0;
        for (let by = 0; by + B <= h; by += B) {
            for (let bx = 0; bx + B <= w; bx += B) {
                blocks++;
                let mx = 0;
                for (let y = 0; y < B; y++) {
                    for (let x = 0; x < B; x++) {
                        const i = ((by + y) * w + bx + x) * 4;
                        const d = Math.max(Math.abs(da[i] - db[i]),
                                Math.abs(da[i + 1] - db[i + 1]),
                                Math.abs(da[i + 2] - db[i + 2]));
                        if (d > mx) mx = d;
                    }
                }
                if (mx > 16) changed++;
                if (mx > maxDelta) maxDelta = mx;
            }
        }
        return { blocks, changed, frac: +(changed / blocks).toFixed(3), maxDelta };
    });
});

const pushFrame = page => page.evaluate(() => {
    window.__diffStore = window.__diffStore || [];
    window.__diffStore.push(window.__frameData);
    if (window.__diffStore.length > 2) window.__diffStore.shift();
});

(async () => {
    const browser = await puppeteer.launch({ headless: true, args: ['--no-sandbox', '--enable-unsafe-swiftshader', '--use-gl=swiftshader'] });
    const page = await browser.newPage();
    await page.setViewport({ width: 1024, height: 600 });
    await page.goto('http://127.0.0.1:8081/?ep=newgame&level=1', { waitUntil: 'domcontentloaded', timeout: 60000 });
    for (let i = 0; i < 45; i++) {
        await sleep(2000);
        const st = await page.evaluate(() => ({ s: (window.__gameState || {}).scene, r: window.__rafTicks }));
        if (st.s === 'GameScene' && st.r > 50) break;
    }
    await sleep(4000);

    const canvas = await page.$('canvas');
    const box = await canvas.boundingBox();
    const click = (x, y) => page.mouse.click(box.x + x, box.y + y);

    // idle: two captures, no action
    await pushFrame(page);
    await sleep(2000);
    await pushFrame(page);
    console.log('idle:', JSON.stringify(await diffLastPair(page)));

    // hero action 1: walk down
    await pushFrame(page);
    await click(397, 380);
    await sleep(3000);
    await pushFrame(page);
    console.log('walk1:', JSON.stringify(await diffLastPair(page)));

    // hero action 2: walk right
    await pushFrame(page);
    await click(500, 380);
    await sleep(3000);
    await pushFrame(page);
    console.log('walk2:', JSON.stringify(await diffLastPair(page)));

    await page.screenshot({ path: '/tmp/tile_reroll_final.png' });
    await browser.close();
})();
