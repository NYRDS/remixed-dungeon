const puppeteer = require('puppeteer');
(async () => {
    const browser = await puppeteer.launch({ headless: true, args: ['--no-sandbox', '--enable-unsafe-swiftshader', '--use-gl=swiftshader'] });
    const page = await browser.newPage();
    await page.setViewport({ width: 1024, height: 600 });
    await page.goto('http://127.0.0.1:8081/?ep=newgame', { waitUntil: 'domcontentloaded', timeout: 60000 });
    await new Promise(r => setTimeout(r, 30000));
    const errs = await page.evaluate(() => (window.__errors||[]));
    const hits = errs.filter(l => l.includes('tilemapDesc') || l.includes('tiles_x') || l.includes('tiles0') || l.includes('bad json') || l.includes('gson failed') || l.includes('sanitize') || l.includes('read json'));
    hits.slice(0, 20).forEach(l => console.log('HIT:', l.slice(0, 200)));
    console.log('total ring:', errs.length);
    await browser.close();
})();
