// Lazy mod file tree for the sidebar.

import * as api from './api.js';

// opts: { mod, onOpen(file), onNewFile(dir), onRename(entryPath), onDelete(entryPath) }
export function renderTree(container, opts) {
  container.innerHTML = '';
  const root = document.createElement('ul');
  root.className = 'tree';
  container.appendChild(root);

  const mod = opts.mod;

  function dirNode(parentUl, dirPath, name) {
    const li = document.createElement('li');
    li.className = 'dir';
    const label = document.createElement('span');
    label.className = 'label';
    label.textContent = name + '/';
    li.appendChild(label);
    const children = document.createElement('ul');
    children.style.display = 'none';
    li.appendChild(children);

    let loaded = false;
    label.addEventListener('click', async () => {
      const open = li.classList.toggle('open');
      children.style.display = open ? '' : 'none';
      if (open && !loaded) {
        loaded = true;
        await fillDir(children, dirPath);
      }
    });
    parentUl.appendChild(li);
    return { children, dirPath };
  }

  async function fillDir(ul, dirPath) {
    const data = await api.modFiles(mod, dirPath);
    for (const entry of data.entries) {
      if (entry.dir) {
        dirNode(ul, dirPath ? dirPath + '/' + entry.name : entry.name, entry.name);
      } else {
        fileNode(ul, dirPath ? dirPath + '/' + entry.name : entry.name, entry.name);
      }
    }
    const actions = document.createElement('li');
    actions.className = 'file';
    const newFile = document.createElement('span');
    newFile.className = 'label';
    newFile.textContent = '+ new file';
    newFile.addEventListener('click', () => opts.onNewFile(dirPath));
    actions.appendChild(newFile);
    ul.appendChild(actions);
  }

  function fileNode(ul, filePath, name) {
    const li = document.createElement('li');
    li.className = 'file';
    li.dataset.path = filePath;
    const label = document.createElement('span');
    label.className = 'label';
    label.textContent = name;
    label.addEventListener('click', () => {
      container.querySelectorAll('.label.active').forEach(el => el.classList.remove('active'));
      label.classList.add('active');
      opts.onOpen(filePath);
    });
    li.appendChild(label);

    if (opts.onRename || opts.onDelete) {
      if (opts.onRename) {
        const ren = document.createElement('span');
        ren.textContent = ' ✎';
        ren.style.color = '#aa8';
        ren.style.cursor = 'pointer';
        ren.title = 'rename';
        ren.addEventListener('click', e => {
          e.stopPropagation();
          opts.onRename(filePath);
        });
        li.appendChild(ren);
      }
      const x = document.createElement('span');
      x.textContent = ' ×';
      x.style.color = '#a55';
      x.style.cursor = 'pointer';
      x.title = 'delete';
      x.addEventListener('click', e => {
        e.stopPropagation();
        if (opts.onDelete) { opts.onDelete(filePath); }
      });
      li.appendChild(x);
    }
    ul.appendChild(li);
  }

  fillDir(root, '');
}
