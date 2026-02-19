#!/usr/bin/env node
/**
 * One-time script: adds a stable unique "id" (UUID v4) to each object in content[]
 * in all storage content.json files. Skip items that already have id.
 */
const fs = require('fs');
const path = require('path');
const crypto = require('crypto');

const storageRoot = path.join(__dirname, 'storage');

function findContentJson(dir, list = []) {
  const entries = fs.readdirSync(dir, { withFileTypes: true });
  for (const e of entries) {
    const full = path.join(dir, e.name);
    if (e.isDirectory()) findContentJson(full, list);
    else if (e.name === 'content.json') list.push(full);
  }
  return list;
}

const files = findContentJson(storageRoot);

for (const fullPath of files) {
  const rel = path.relative(__dirname, fullPath);
  const raw = fs.readFileSync(fullPath, 'utf8');
  const data = JSON.parse(raw);
  if (!Array.isArray(data.content)) {
    console.log(rel + ': no content array, skip');
    continue;
  }
  let added = 0;
  for (const item of data.content) {
    if (item.id == null) {
      item.id = crypto.randomUUID();
      added++;
    }
  }
  fs.writeFileSync(fullPath, JSON.stringify(data, null, 2), 'utf8');
  console.log(rel + ': added ' + added + ' id(s), total items ' + data.content.length);
}

console.log('Done.');
