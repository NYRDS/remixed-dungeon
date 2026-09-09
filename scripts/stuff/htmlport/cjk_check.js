// CJK fallback font on-demand check.
// Phase A: plain boot (en) - assert NO /fonts/ request fires.
// Phase B: boot with locale=zh_CN seeded in prefs - assert the font is
//          fetched, 'cjk_font_ready' lands, scene rebuilds, and capture
//          the title screen (visual: CJK glyphs rendered by LXGW font).
const puppeteer = require('puppeteer');

const sleep = ms => new Promise(r => setTimeout(r, ms));

const trackFonts = async (page) => {
    await page.evaluateOnNewDocument(() => {
        window.__fontRequests = [];
        const orig = window.fetch;
        window.fetch = function(url) {
            const u = String(url);
            if (u.includes('LXGWWenKaiScreen.ttf')) { window.__fontRequests.push(u); }
            return orig.apply(this, arguments);
        };
        const origOpen = XMLHttpRequest.prototype.open;
        XMLHttpRequest.prototype.open = function(m, u) {
            if (String(u).includes('LXGWWenKaiScreen.ttf')) { window.__fontRequests.push(String(u)); }
            return origOpen.apply(this, arguments);
        };
    });
};

const waitTitle = async (page, timeoutS) => {
    for (let i = 0; i < timeoutS; i++) {
        await sleep(2000);
        const st = await page.evaluate(() => ({
            scene: (window.__gameState || {}).scene,
            raf: window.__rafTicks,
            fonts: window.__fontRequests || [],
            cjk: (window.__errors || []).filter(e => String(e).includes('cjk_font')).map(String),
        }));
        if (st.scene === 'TitleScene' && st.raf > 50) { return st; }
    }
    return await page.evaluate(() => ({ scene: (window.__gameState || {}).scene, fonts: window.__fontRequests || [] }));
};

(async () => {
    const browser = await puppeteer.launch({ headless: true, args: ['--no-sandbox', '--enable-unsafe-swiftshader', '--use-gl=swiftshader'] });

    console.log('=== Phase A: plain en boot ===');
    const pageA = await browser.newPage();
    await pageA.setViewport({ width: 1024, height: 600 });
    await trackFonts(pageA);
    await pageA.goto('http://127.0.0.1:8081/', { waitUntil: 'domcontentloaded', timeout: 60000 });
    const a = await waitTitle(pageA, 40);
    console.log('scene:', a.scene, 'fontRequests:', JSON.stringify(a.fonts || []));
    console.log(a.fonts && a.fonts.length === 0 ? 'PASS: no font fetch on plain boot' : 'FAIL: font fetched without CJK need');

    console.log('=== Phase B: zh_CN boot ===');
    const pageB = await browser.newPage();
    await pageB.setViewport({ width: 1024, height: 600 });
    await trackFonts(pageB);
    await pageB.evaluateOnNewDocument(() => {
        localStorage.setItem('pref:app:RemixedDungeon:datas', '{"locale":"zh_CN"}');
    });
    await pageB.goto('http://127.0.0.1:8081/', { waitUntil: 'domcontentloaded', timeout: 60000 });

    let ready = false;
    for (let i = 0; i < 45; i++) {
        await sleep(2000);
        const st = await pageB.evaluate(() => ({
            scene: (window.__gameState || {}).scene,
            fonts: window.__fontRequests || [],
            cjk: (window.__errors || []).filter(e => String(e).includes('cjk_font')).map(String),
        }));
        if (i % 5 === 0) console.log(`t=${i * 2}s scene=${st.scene} fontReq=${st.fonts.length} cjkEvents=${JSON.stringify(st.cjk)}`);
        if (st.cjk.some(e => e.includes('cjk_font_ready'))) { ready = true; console.log(`cjk_font_ready at t=${i * 2}s scene=${st.scene}`); break; }
    }
    const b = await pageB.evaluate(() => ({
        fonts: window.__fontRequests || [],
        cjk: (window.__errors || []).filter(e => String(e).includes('cjk_font')).map(String),
        scene: (window.__gameState || {}).scene,
    }));
    console.log('fontRequests:', JSON.stringify(b.fonts));
    console.log('cjk events:', JSON.stringify(b.cjk));
    console.log(ready && b.fonts.length > 0 ? 'PASS: font fetched on demand for zh_CN' : 'FAIL: font not fetched');
    await sleep(3000);
    await pageB.screenshot({ path: '/tmp/cjk_title.png' });
    console.log('screenshot /tmp/cjk_title.png');

    await browser.close();
})();
