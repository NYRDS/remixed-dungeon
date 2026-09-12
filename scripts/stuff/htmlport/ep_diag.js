const puppeteer = require('puppeteer');
(async () => {
    const browser = await puppeteer.launch({ headless: true, args: ['--no-sandbox', '--enable-unsafe-swiftshader', '--use-gl=swiftshader'] });
    const page = await browser.newPage();
    await page.setViewport({ width: 1024, height: 600 });
    await page.goto('http://127.0.0.1:8081/?ep=newgame&level=1', { waitUntil: 'domcontentloaded', timeout: 60000 });
    for (let i = 0; i < 45; i++) {
        await new Promise(r => setTimeout(r, 2000));
        const st = await page.evaluate(() => ({ scene: (window.__gameState||{}).scene, raf: window.__rafTicks }));
        if (st.scene === 'GameScene' && st.raf > 50) { console.log('GameScene at t=' + (i*2) + 's'); break; }
    }
    await new Promise(r => setTimeout(r, 8000));
    const diag = await page.evaluate(() => ({
        scene: (window.__gameState||{}).scene,
        state: window.__gameState,
        raf: window.__rafTicks,
        rafStats: window.__rafShimStats,
        errTail: (window.__errors||[]).slice(-12),
        jsErr: (window.__jsErrLog||[]).slice(-5),
    }));
    console.log(JSON.stringify(diag, null, 1).slice(0, 3000));
    await browser.close();
})();
