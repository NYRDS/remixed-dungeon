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

    // wait for GameScene
    for (let i = 0; i < 45; i++) {
        await new Promise(r => setTimeout(r, 2000));
        const st = await page.evaluate(() => (window.__gameState || {}).scene);
        if (st === 'GameScene') break;
    }
    await new Promise(r => setTimeout(r, 3000));

    const canvas = await page.$('canvas');
    const box = await canvas.boundingBox();
    console.log('canvas box', JSON.stringify(box));

    const grab = async (name) => {
        const d = await page.evaluate(() => window.__frameData);
        if (d) { fs.writeFileSync(`/tmp/${name}.png`, Buffer.from(d.split(',')[1], 'base64')); console.log('saved', name); }
        else console.log('no framedata for', name);
    };

    const click = async (x, y) => {
        await page.mouse.click(box.x + x, box.y + y);
    };

    await grab('walk0');

    // walk down (click below hero)
    await click(397, 380);
    await new Promise(r => setTimeout(r, 2500));
    await grab('walk1');

    // walk down more
    await click(397, 420);
    await new Promise(r => setTimeout(r, 2500));
    await grab('walk2');

    // walk left
    await click(150, 300);
    await new Promise(r => setTimeout(r, 3500));
    await grab('walk3');

    await browser.close();
})();
