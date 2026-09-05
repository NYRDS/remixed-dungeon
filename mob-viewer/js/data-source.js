// Data source - resolves viewer asset paths against the local server or
// directly against GitHub (raw.githubusercontent.com + contents API).

const REPO = 'NYRDS/remixed-dungeon';
const BRANCH = 'master';
// viewer paths start with 'assets/', which maps here in the repo
const ASSETS_DIR = 'RemixedDungeon/src/main/assets';

const STORAGE_KEY = 'mobviewer_datasource';

export function getMode() {
    return localStorage.getItem(STORAGE_KEY) === 'github' ? 'github' : 'local';
}

export function setMode(mode) {
    localStorage.setItem(STORAGE_KEY, mode === 'github' ? 'github' : 'local');
}

// 'assets/foo.png' -> absolute URL for the current mode
export function resolve(path) {
    if (getMode() !== 'github') {
        return path;
    }
    const rel = path.replace(/^assets\//, '');
    return `https://raw.githubusercontent.com/${REPO}/${BRANCH}/${ASSETS_DIR}/${rel}`;
}

// Directory listing for spritesDesc. Returns names ('Rat', ...) or null
// when the mode/server provides no listing (caller falls back).
export async function listSpritesDesc() {
    if (getMode() !== 'github') {
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
