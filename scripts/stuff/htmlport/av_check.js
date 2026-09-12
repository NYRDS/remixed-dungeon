// save+audio verification for the web port.
// Phase A: audio - boot plain title, check AudioContext state, decode
//          failures, and that a trusted (CDP) click resumes the context.
// Phase B: saves - boot ?ep=newgame&level=1&save=1, dump rdg_file_ keys,
//          reload plain, verify keys survive and the game-file bundle is
//          readable. NO clicks after saves exist (title/start clicks run
//          the real New Game flow, which deletes saves).
const puppeteer = require('puppeteer');

const sleep = ms => new Promise(r => setTimeout(r, ms));

const dumpKeys = page => page.evaluate(() => {
    const keys = Object.keys(localStorage).filter(k => k.startsWith('rdg_file_'));
    return keys.map(k => ({ key: k, bytes: (localStorage.getItem(k) || '').length }));
});

(async () => {
    const browser = await puppeteer.launch({ headless: true, args: ['--no-sandbox', '--enable-unsafe-swiftshader', '--use-gl=swiftshader'] });
    const page = await browser.newPage();
    await page.setViewport({ width: 1024, height: 600 });

    console.log('=== Phase A: audio ===');
    await page.goto('http://127.0.0.1:8081/', { waitUntil: 'domcontentloaded', timeout: 60000 });
    for (let i = 0; i < 30; i++) {
        await sleep(2000);
        const st = await page.evaluate(() => ({ scene: (window.__gameState || {}).scene, raf: window.__rafTicks }));
        if (st.scene === 'TitleScene' && st.raf > 50) { console.log(`TitleScene at t=${i * 2}s`); break; }
    }
    let audio = await page.evaluate(() => ({
        ctx: window.__audioCtx ? window.__audioCtx.state : 'NO CTX',
        decodeFails: (window.__errors || []).filter(e => String(e).includes('audio_decode_failed')).length,
    }));
    console.log('before gesture:', JSON.stringify(audio));
    await page.mouse.click(500, 300);
    await sleep(1500);
    audio = await page.evaluate(() => ({
        ctx: window.__audioCtx ? window.__audioCtx.state : 'NO CTX',
        sampleRate: window.__audioCtx ? window.__audioCtx.sampleRate : 0,
        decodeFails: (window.__errors || []).filter(e => String(e).includes('audio_decode_failed')).length,
        audioErrs: (window.__errors || []).filter(e => /audio_decode|quota/i.test(String(e))).slice(-3),
    }));
    console.log('after gesture:', JSON.stringify(audio));

    console.log('=== Phase B: saves ===');
    await page.goto('http://127.0.0.1:8081/?ep=newgame&level=1&save=1', { waitUntil: 'domcontentloaded', timeout: 60000 });
    let saved = false;
    for (let i = 0; i < 60; i++) {
        await sleep(2000);
        const st = await page.evaluate(() => ({
            scene: (window.__gameState || {}).scene,
            saved: (window.__errors || []).some(e => String(e).includes('game saved')),
        }));
        if (st.saved) { console.log(`save marker at t=${i * 2}s, scene=${st.scene}`); saved = true; break; }
        if (i % 5 === 0) console.log(`t=${i * 2}s scene=${st.scene}`);
    }
    if (!saved) console.log('WARN: never saw EP2 "game saved" marker');

    const keysAfterSave = await dumpKeys(page);
    console.log('keys after save:', JSON.stringify(keysAfterSave, null, 1));

    const saveBundle = await page.evaluate(() => {
        const k = Object.keys(localStorage).find(k => k.endsWith('/warrior.dat/'));
        return k ? { key: k, value: localStorage.getItem(k) } : null;
    });

    await page.goto('http://127.0.0.1:8081/', { waitUntil: 'domcontentloaded', timeout: 60000 });
    for (let i = 0; i < 30; i++) {
        await sleep(2000);
        const st = await page.evaluate(() => ({ scene: (window.__gameState || {}).scene, raf: window.__rafTicks }));
        if (st.scene === 'TitleScene' && st.raf > 50) { console.log(`TitleScene after reload at t=${i * 2}s`); break; }
    }

    const keysAfterReload = await dumpKeys(page);
    console.log('keys after reload:', JSON.stringify(keysAfterReload, null, 1));

    if (saveBundle) {
        const zlib = require('zlib');
        try {
            const json = JSON.parse(zlib.gunzipSync(Buffer.from(saveBundle.value, 'base64')).toString());
            console.log(`${saveBundle.key} -> bundle ok: hero=${!!json.hero} depth=${json.depth} keys=${Object.keys(json).join(',')}`);
        } catch (e) {
            console.log(`${saveBundle.key} -> UNREADABLE: ${e.message}`);
        }
    } else {
        console.log('no warrior.dat key found');
    }

    await browser.close();
})();
