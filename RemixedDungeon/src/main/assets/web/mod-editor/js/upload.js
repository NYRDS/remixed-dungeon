// Shared binary upload helper: picks a local file, stores it in the mod via
// save_binary (magic-checked server-side). Bytes pass through unchanged -
// no re-encode, so uploaded sheets/audio keep their exact layout.
//
// uploadPng(mod, suggestedName)      -> Promise<path string | null>
// uploadFile(mod, suggestedName, acceptMime, exts) -> Promise<path | null>
// (both resolve null when the user cancels the file picker)

import * as api from './api.js';

function arrayBufferToBase64(buffer) {
  const bytes = new Uint8Array(buffer);
  let binary = '';
  const chunk = 0x8000;
  for (let i = 0; i < bytes.length; i += chunk) {
    binary += String.fromCharCode.apply(null, bytes.subarray(i, i + chunk));
  }
  return btoa(binary);
}

export function uploadFile(mod, suggestedName, accept, exts) {
  return new Promise(resolve => {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = accept;
    input.addEventListener('change', async () => {
      const file = input.files && input.files[0];
      if (!file) { resolve(null); return; }
      let name = suggestedName || file.name;
      if (exts && !exts.some(ext => name.toLowerCase().endsWith(ext))) {
        // pick the extension from what was actually picked - the server
        // magic-checks against the extension, so a mismatched one bounces
        const picked = exts.find(ext => file.name.toLowerCase().endsWith(ext));
        name += picked || exts[0];
      }
      try {
        const buffer = await file.arrayBuffer();
        await api.saveBinary(mod, name, arrayBufferToBase64(buffer));
        resolve(name);
      } catch (e) {
        alert(e.message);
        resolve(null);
      }
    });
    // cancel yields no change event in every browser we care about - the
    // promise just never settles from cancel, callers do not await forever
    // for their use cases (button click handlers)
    input.click();
  });
}

export function uploadPng(mod, suggestedName) {
  return uploadFile(mod, suggestedName, 'image/png', ['.png']);
}
