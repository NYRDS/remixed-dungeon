// Data source - resolves viewer asset paths against the RPD game server,
// the local static server, or directly against GitHub.

const REPO = 'NYRDS/remixed-dungeon';
const BRANCH = 'master';
// viewer paths start with 'assets/', which maps here in the repo
const ASSETS_DIR = 'RemixedDungeon/src/main/assets';

const STORAGE_KEY = 'mobviewer_datasource';

// 'game' is only offered (and auto-detected) when the page is served by the
// game's own web server; probing happens once in detectGameServer().
let gameServerAvailable = false;

export function getMode() {
    const stored = localStorage.getItem(STORAGE_KEY);
    if (stored === 'github' || stored === 'local' || stored === 'game') {
        return stored;
    }
    return gameServerAvailable ? 'game' : 'local';
}

export function setMode(mode) {
    localStorage.setItem(STORAGE_KEY, mode);
}

export function isGameMode() {
    return getMode() === 'game' && gameServerAvailable;
}

// One-time boot probe: the game server answers /api/sprites/list with JSON.
// A plain static server (serve.py, github.io, ...) 404s or serves HTML -
// both fail the probe and 'game' stays unavailable.
export async function detectGameServer() {
    try {
        const resp = await fetch('/api/sprites/list', { method: 'GET' });
        if (resp.ok) {
            const data = await resp.json();
            gameServerAvailable = Array.isArray(data.sprites);
        }
    } catch (e) {
        gameServerAvailable = false;
    }
    return gameServerAvailable;
}

// 'assets/foo.png' -> absolute URL for the current mode
export function resolve(path) {
    const mode = getMode();
    if (mode === 'game') {
        // game server: mod-relative path behind /raw/ (assets live in the
        // active mod - Remixed - both baked-in and via the rundir overlay)
        let url = '/raw/' + path.replace(/^assets\//, '');
        if (bustCounter > 0) {
            // a sprite was saved this session - force the browser past its
            // cache of the pre-edit PNG
            url += '?t=' + bustCounter;
        }
        return url;
    }
    if (mode === 'github') {
        const rel = path.replace(/^assets\//, '');
        return `https://raw.githubusercontent.com/${REPO}/${BRANCH}/${ASSETS_DIR}/${rel}`;
    }
    return path;
}

let bustCounter = 0;

// After a successful sprite save: all subsequent texture loads bypass the
// browser cache (the URL changed, the file content did too).
export function bustTextures() {
    bustCounter = Date.now();
}

// Mod-relative asset path ('bat.png', 'hero_modern/body/man.png') for save
// requests; null when the current mode has no game server to save to.
export function modRelativePath(path) {
    if (!isGameMode()) {
        return null;
    }
    // tolerate a resolved game-mode URL slipping in ('/raw/bat.png')
    return path.replace(/^\/raw\//, '').replace(/^assets\//, '');
}

// Save a PNG sprite sheet (base64 without the data: prefix) into the active
// mod. Resolves to {ok, message}.
export async function saveSprite(modPath, base64Png) {
    const resp = await fetch('/api/save_sprite', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ file: modPath, image: base64Png })
    });
    const data = await resp.json().catch(() => ({}));
    if (!resp.ok) {
        throw new Error(data.error || `save failed: HTTP ${resp.status}`);
    }
    return data;
}

// Ask the running game to drop its texture cache so edited sheets rebind.
export async function reloadGameTextures() {
    try {
        await fetch('/debug/reload_textures');
    } catch (e) {
        // optional nicety - viewer-side reload still shows the edit
    }
}

// Directory listing for spritesDesc. Returns names ('Rat', ...) or null
// when the mode/server provides no listing (caller falls back).
export async function listSpritesDesc() {
    const mode = getMode();
    if (mode === 'game') {
        try {
            const resp = await fetch('/api/sprites/list');
            if (!resp.ok) {
                return null;
            }
            const data = await resp.json();
            return Array.isArray(data.sprites) ? data.sprites : null;
        } catch (e) {
            return null;
        }
    }
    if (mode !== 'github') {
        return null;
    }
    try {
        const resp = await fetch(`https://api.github.com/repos/${REPO}/contents/${ASSETS_DIR}/spritesDesc`);
        if (!resp.ok) {
            console.warn('GitHub listing failed:', resp.status, '- using fallback mob list');
            return null;
        }
        const entries = await resp.json();
        return entries
            .filter(e => e.name.endsWith('.json'))
            .map(e => e.name.replace(/\.json$/, ''))
            .sort();
    } catch (e) {
        console.warn('GitHub listing error:', e);
        return null;
    }
}
