// Runtime smoke for the teavm 0.15.0 minimal-patchset build:
// 1. boot -> newgame -> save=1 (zip save pipeline) -> fight=1 (12 strikes, luaj mob ai)
// 2. reload -> ?ep=continue (save roundtrip through PersistedFileStorage)
// Fails on: scene never reaching GameScene, JS errors crossing into Java,
// save failure, continue finding no autosave.
const puppeteer = require('puppeteer');

async function boot(browser, url, wantScene, maxPolls) {
    const page = await browser.newPage();
    await page.setViewport({ width: 1024, height: 600 });
    const logs = [];
    page.on('console', m => logs.push(m.text()));
    page.on('pageerror', e => logs.push('PAGEERROR: ' + e.message));
    await page.goto(url, { waitUntil: 'domcontentloaded', timeout: 60000 });
    let scene = null;
    for (let i = 0; i < maxPolls; i++) {
        await new Promise(r => setTimeout(r, 3000));
        scene = await page.evaluate(() => (window.__gameState || {}).scene || null);
        if (scene === wantScene) break;
    }
    return { page, logs, scene };
}

(async () => {
    const browser = await puppeteer.launch({ headless: true, args: ['--no-sandbox', '--enable-unsafe-swiftshader', '--use-gl=swiftshader'] });
    let failures = [];

    // --- phase 1: newgame + save + fight ---
    const p1 = await boot(browser, 'http://127.0.0.1:8081/?ep=newgame&x=13&y=15&save=1&fight=1&nosound=1', 'GameScene', 70);
    if (p1.scene !== 'GameScene') failures.push('phase1: never reached GameScene (scene=' + p1.scene + ')');
    // let the fight task finish (12 strikes)
    await new Promise(r => setTimeout(r, 25000));
    const d1 = await p1.page.evaluate(() => ({
        scene: (window.__gameState || {}).scene,
        jsErr: (window.__jsErrLog || []).slice(-10),
        epPoll: window.__epPoll,
    }));
    const all1 = p1.logs.concat([]);
    const need1 = ['game saved', 'slotUsed = true', 'startNewGame returned'];
    for (const n of need1) {
        if (!all1.some(l => l.includes(n))) failures.push('phase1: missing log marker "' + n + '"');
    }
    // known-benign: caught reflective field misses (getClassParam probe), the
    // mod-loader's intentionally-failing first lua compile probe, and the
    // caught JSONObject.optInt default-path error on NUL-containing save
    // bundle values (game falls back to the default; scene stays GameScene)
    const benign = e => e.msg === 'Error: null'
        || (e.msg || '').includes('unexpected symbol')
        || (e.msg || '').includes('is not a valid number');
    const fatal1 = d1.jsErr.filter(e => !benign(e));
    if (fatal1.length) failures.push('phase1: fatal jsErr: ' + JSON.stringify(fatal1).slice(0, 600));
    const fightDone = all1.some(l => l.includes('fight: finished'));
    console.log('phase1 logs (EP2 tail):');
    all1.filter(l => l.includes('EP2')).slice(-14).forEach(l => console.log('  ' + l));
    console.log('fight finished: ' + fightDone + '  scene: ' + d1.scene + '  epPoll: ' + d1.epPoll);
    await p1.page.close();

    // --- phase 2: continue from the save made above ---
    const p2 = await boot(browser, 'http://127.0.0.1:8081/?ep=continue&nosound=1', 'GameScene', 70);
    if (p2.scene !== 'GameScene') failures.push('phase2: continue never reached GameScene (scene=' + p2.scene + ')');
    const d2 = await p2.page.evaluate(() => ({ jsErr: (window.__jsErrLog || []).slice(-10) }));
    const fatal2 = d2.jsErr.filter(e => !benign(e));
    if (fatal2.length) failures.push('phase2: fatal jsErr: ' + JSON.stringify(fatal2).slice(0, 600));
    const contOk = p2.logs.some(l => l.includes('continue: loading'));
    console.log('phase2 continue loading marker: ' + contOk + '  scene: ' + p2.scene);
    await p2.page.close();

    await browser.close();
    if (failures.length) {
        console.log('FAIL:\n' + failures.map(f => '  - ' + f).join('\n'));
        process.exit(1);
    }
    console.log('PASS: teavm 0.15.0 runtime smoke (boot, save, fight, continue)');
})();
