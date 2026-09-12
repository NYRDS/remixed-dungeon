const puppeteer = require('puppeteer');
const fs = require('fs');

(async () => {
    const browser = await puppeteer.launch({
        headless: true,
        args: ['--no-sandbox', '--enable-unsafe-swiftshader', '--use-gl=swiftshader'],
    });
    const page = await browser.newPage();
    await page.setViewport({ width: 1024, height: 600 });

    page.on('pageerror', err => console.log('pageerror: ' + err.message.slice(0, 200)));

    await page.goto('http://127.0.0.1:8081/?ep=newgame', { waitUntil: 'domcontentloaded', timeout: 60000 });

    for (let i = 0; i < 45; i++) {
        await new Promise(r => setTimeout(r, 2000));
        const st = await page.evaluate(() => (window.__gameState || {}).scene);
        if (st === 'GameScene') break;
    }
    await new Promise(r => setTimeout(r, 3000));

    const canvas = await page.$('canvas');
    const box = await canvas.boundingBox();

    const grab = async (name) => {
        const d = await page.evaluate(() => window.__frameData);
        if (d) { fs.writeFileSync(`/tmp/${name}.png`, Buffer.from(d.split(',')[1], 'base64')); console.log('saved', name); }
    };
    const click = async (x, y) => { await page.mouse.click(box.x + x, box.y + y); };

    // walk up-left into the town center (buildings area), like the manual check
    await grab('roof_web_0');
    await click(300, 230); await new Promise(r => setTimeout(r, 3000));
    await click(280, 200); await new Promise(r => setTimeout(r, 3000));
    await click(260, 190); await new Promise(r => setTimeout(r, 4000));
    await grab('roof_web_1');

    // a second spot: more left
    await click(180, 200); await new Promise(r => setTimeout(r, 4000));
    await grab('roof_web_2');

    await browser.close();
})();
