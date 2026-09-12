// CJK fallback font on-demand check + heavy-load splash.
// Phase A: plain boot (en) - assert NO LXGW fetch fires.
// Phase B: zh_CN boot, download throttled to ~1MB/s - the heavy-load splash
//          must appear with progress, then the font lands, scene rebuilds,
//          CJK title renders. Un-throttles to finish quickly.
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

(async () => {
    const browser = await puppeteer.launch({ headless: true, args: ['--no-sandbox', '--enable-unsafe-swiftshader', '--use-gl=swiftshader'] });

    console.log('=== Phase A: plain en boot ===');
    const pageA = await browser.newPage();
    await pageA.setViewport({ width: 1024, height: 600 });
    await trackFonts(pageA);
    await pageA.goto('http://127.0.0.1:8081/', { waitUntil: 'domcontentloaded', timeout: 60000 });
    let a = {};
    for (let i = 0; i < 40; i++) {
        await sleep(2000);
        a = await pageA.evaluate(() => ({ scene: (window.__gameState || {}).scene, fonts: window.__fontRequests || [], raf: window.__rafTicks }));
        if (a.scene === 'TitleScene' && a.raf > 50) break;
    }
    console.log('scene:', a.scene, 'fontRequests:', JSON.stringify(a.fonts));
    console.log((a.fonts || []).length === 0 ? 'PASS: no font fetch on plain boot' : 'FAIL: font fetched without CJK need');
    await pageA.close();

    console.log('=== Phase B: zh_CN boot, throttled ~1MB/s ===');
    const pageB = await browser.newPage();
    await pageB.setViewport({ width: 1024, height: 600 });
    await trackFonts(pageB);
    await pageB.evaluateOnNewDocument(() => {
        localStorage.setItem('pref:app:RemixedDungeon:datas', '{"locale":"zh_CN"}');
    });
    const client = await pageB.createCDPSession();
    await client.send('Network.enable');
    await client.send('Network.emulateNetworkConditions', {
        offline: false, latency: 20,
        downloadThroughput: 1024 * 1024,
        uploadThroughput: 1024 * 1024,
    });
    await pageB.goto('http://127.0.0.1:8081/', { waitUntil: 'domcontentloaded', timeout: 60000 });

    let splashSeen = false;
    let ready = false;
    for (let i = 0; i < 60; i++) {
        await sleep(1000);
        const st = await pageB.evaluate(() => ({
            scene: (window.__gameState || {}).scene,
            fonts: window.__fontRequests || [],
            splashUp: (() => { const e = document.getElementById('heavyload'); return !!e && e.style.display === 'flex'; })(),
            splashText: (() => { const e = document.getElementById('heavyload'); return e ? e.querySelector('.label').textContent : ''; })(),
            splashWidth: (() => { const e = document.getElementById('heavyload'); return e ? e.querySelector('.bar > div').style.width : ''; })(),
            cjk: (window.__errors || []).filter(x => String(x).includes('cjk_font')).map(String),
        }));
        if (st.splashUp && !splashSeen) {
            splashSeen = true;
            console.log(`splash visible at t=${i}s label="${st.splashText}" bar=${st.splashWidth}`);
            await pageB.screenshot({ path: '/tmp/cjk_splash.png' });
            // let it show a bit more progress, then unthrottle to finish fast
            await sleep(3000);
            console.log('bar after 3s:', await pageB.evaluate(() => document.querySelector('#heavyload .bar > div').style.width));
            await client.send('Network.emulateNetworkConditions', {
                offline: false, latency: 5, downloadThroughput: 10 * 1024 * 1024, uploadThroughput: 10 * 1024 * 1024,
            });
        }
        if (st.cjk.some(e => e.includes('cjk_font_ready'))) { ready = true; console.log(`cjk_font_ready at t=${i}s scene=${st.scene}`); break; }
        if (i % 10 === 0) console.log(`t=${i}s scene=${st.scene} splash=${st.splashUp} fontReq=${st.fonts.length}`);
    }

    console.log('fontRequests:', await pageB.evaluate(() => JSON.stringify(window.__fontRequests)));
    console.log(splashSeen ? 'PASS: splash shown during heavy load' : 'FAIL: splash never appeared');
    console.log(ready ? 'PASS: font fetched and installed' : 'FAIL: font not ready');
    await sleep(3000);
    await pageB.screenshot({ path: '/tmp/cjk_title.png' });
    console.log('screenshots: /tmp/cjk_splash.png /tmp/cjk_title.png');

    await browser.close();
})().catch(e => { console.error('FAILED:', e.message); process.exit(1); });
