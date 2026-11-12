// Thin client for the mod-scoped game server API (/api/mods/*).
// Every call throws Error(message) with the server's error text on failure.

let busyCb = null;
export function onBusy(cb) { busyCb = cb; }

async function request(url, options) {
  if (busyCb) { busyCb(true); }
  // a hung keep-alive connection must not wedge the page forever
  const ctrl = new AbortController();
  const timer = setTimeout(() => ctrl.abort(), 30000);
  try {
    const resp = await fetch(url, Object.assign({ signal: ctrl.signal }, options));
    let data = null;
    try { data = await resp.json(); } catch (e) { /* non-json */ }
    if (!resp.ok) {
      throw new Error((data && data.error) || `HTTP ${resp.status}`);
    }
    return data;
  } catch (e) {
    if (e.name === 'AbortError') { throw new Error('request timeout: ' + url); }
    throw e;
  } finally {
    clearTimeout(timer);
    if (busyCb) { busyCb(false); }
  }
}

function post(action, payload) {
  return request('/api/mods/' + action, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  });
}

export function listMods() {
  return request('/api/mods/list');
}

export function modFiles(mod, path, builtin) {
  const q = new URLSearchParams({ mod, path: path || '' });
  if (builtin) { q.set('builtin', '1'); }
  return request('/api/mods/files?' + q);
}

export function readModFile(mod, file, builtin) {
  const q = new URLSearchParams({ mod, file });
  if (builtin) { q.set('builtin', '1'); }
  return request('/api/mods/read?' + q);
}

export function createMod(payload) { return post('create', payload); }
export function duplicateMod(from, to) { return post('duplicate', { from, to }); }
export function cloneBuiltin(to, title, author) { return post('clone_builtin', { to, title, author }); }
export function deleteMod(name) { return post('delete', { name }); }
export function selectMod(name) { return post('select', { name }); }
export function saveModFile(mod, file, content) { return post('save', { mod, file, content }); }
export function saveBinary(mod, file, base64) { return post('save_binary', { mod, file, data: base64 }); }
export function deleteModFile(mod, file) { return post('delete_file', { mod, file }); }
export function renameModFile(mod, from, to) { return post('rename_file', { mod, from, to }); }
export function validateMod(name) { return post('validate', { name }); }

export function exportUrl(mod) { return `/api/mods/export?mod=${encodeURIComponent(mod)}`; }

// raw bytes (img/canvas): mod file first, builtin fallback
export function rawUrl(mod, file, bust) {
  const q = new URLSearchParams({ mod, file });
  return '/api/mods/raw?' + q + (bust ? '&t=' + bust : '');
}

// terrain constants for the level editor
export function terrainTable() {
  return request('/api/mods/terrain');
}

// base64 without the data: prefix (canvas → PNG saves)
export function canvasToBase64(canvas) {
  return canvas.toDataURL('image/png').split(',')[1];
}
