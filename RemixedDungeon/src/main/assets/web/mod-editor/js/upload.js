// Shared png upload helper: picks a local file, stores it in the mod via
// save_binary (magic-checked server-side, png only). Bytes pass through
// unchanged - no canvas re-encode, so uploaded sheets keep their layout.
//
// uploadPng(mod, suggestedName) -> Promise<path string | null>
// (resolves null when the user cancels the file picker)

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

export function uploadPng(mod, suggestedName) {
  return new Promise(resolve => {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = 'image/png';
    input.addEventListener('change', async () => {
      const file = input.files && input.files[0];
      if (!file) { resolve(null); return; }
      let name = suggestedName || file.name;
      if (!name.toLowerCase().endsWith('.png')) { name += '.png'; }
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
