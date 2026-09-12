const puppeteer = require('puppeteer');
const fs = require('fs');

// usage: node ep_shot.js "query" out.png [waitMs]
(async () => {
    const query = process.argv[2] || '?ep=newgame';
    const out = process.argv[3] || '/tmp/ep_shot.png';
    const extraWait = parseInt(process.argv[4] || '6000', 10);

    const browser = await puppeteer.launch({
        headless: true,
        args: ['--no-sandbox', '--enable-unsafe-swiftshader', '--use-gl=swiftshader'],
    });
    const page = await browser.newPage();
    await page.setViewport({ width: 1024, height: 600 });

    const errors = [];
    page.on('pageerror', err => errors.push('pageerror: ' + err.message.slice(0, 200)));

    await page.goto('http://127.0.0.1:8081/' + query, { waitUntil: 'domcontentloaded', timeout: 60000 });

    for (let i = 0; i < 45; i++) {
        await new Promise(r => setTimeout(r, 2000));
        const st = await page.evaluate(() => (window.__gameState || {}).scene);
        if (st === 'GameScene') break;
    }
    await new Promise(r => setTimeout(r, extraWait));

    const d = await page.evaluate(() => window.__frameData);
    if (d) { fs.writeFileSync(out, Buffer.from(d.split(',')[1], 'base64')); console.log('saved', out); }
    else { console.log('no frameData'); }

    // EP2 slog lines via console capture: pull from __errors ring
    const epLogs = await page.evaluate(() => (window.__errors || []).filter(l => l.includes('EP2') || l.includes('entrypoint')).slice(-8));
    epLogs.forEach(l => console.log('LOG:', l.slice(0, 160)));
    errors.slice(-5).forEach(l => console.log(l));

    await browser.close();
})();
