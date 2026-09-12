const puppeteer = require('puppeteer');

(async () => {
    const browser = await puppeteer.launch({
        headless: true,
        args: ['--no-sandbox', '--enable-unsafe-swiftshader', '--use-gl=swiftshader'],
    });
    const page = await browser.newPage();
    await page.setViewport({ width: 1024, height: 600 });

    const errors = [];
    page.on('console', msg => {
        const t = msg.text();
        if (msg.type() === 'error' || t.includes('GL_INVALID') || t.includes('Exception')) {
            errors.push(t.slice(0, 500));
        }
    });
    page.on('pageerror', err => errors.push('pageerror: ' + err.message.slice(0, 500)));

    await page.goto('http://127.0.0.1:8081/?ep=newgame', { waitUntil: 'domcontentloaded', timeout: 60000 });

    // wait for boot: rAF ticks flowing and a game scene
    for (let i = 0; i < 60; i++) {
        await new Promise(r => setTimeout(r, 2000));
        const st = await page.evaluate(() => ({
            raf: window.__rafTicks, state: window.__gameState,
        }));
        console.log(`[boot] t=${(i + 1) * 2}s raf=${st.raf} state=${JSON.stringify(st.state)}`);
        if (st.raf > 50 && st.state && st.state.scene && st.state.scene.includes('GameScene')) break;
    }

    await new Promise(r => setTimeout(r, 4000));

    // capture a few frames from the shim
    const shot1 = await page.evaluate(() => window.__frameData);
    if (shot1) {
        require('fs').writeFileSync('/tmp/town_web.png', Buffer.from(shot1.split(',')[1], 'base64'));
        console.log('saved /tmp/town_web.png');
    } else {
        console.log('no __frameData!');
        const buf = await page.screenshot();
        require('fs').writeFileSync('/tmp/town_web.png', buf);
        console.log('saved page screenshot instead');
    }

    console.log('=== interesting console lines ===');
    errors.slice(-40).forEach(l => console.log(l));

    await browser.close();
})();
