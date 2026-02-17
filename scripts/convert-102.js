#!/usr/bin/env node
const fs = require('fs');
const raw = fs.readFileSync(
  '/Users/alexander/Desktop/Education/English/FriendsEnglish/server/src/db/friends/1/102.ts',
  'utf8'
);

// Extract blocks array - find from "blocks: [" to matching "]"
const start = raw.indexOf('blocks: [');
if (start === -1) throw new Error('blocks not found');
let depth = 0;
let begin = start + 'blocks: '.length; // index of [
let i = begin;
let inString = false;
let escape = false;
let strChar = '';

for (; i < raw.length; i++) {
  const c = raw[i];
  if (inString) {
    if (escape) { escape = false; continue; }
    if (c === '\\') { escape = true; continue; }
    if (c === strChar) { inString = false; continue; }
    continue;
  }
  if (c === '"' || c === "'") { inString = true; strChar = c; continue; }
  if (c === '[' || c === '{') { depth++; continue; }
  if (c === ']' || c === '}') {
    depth--;
    if (depth === 0 && c === ']') {
      const blocksStr = raw.slice(begin, i + 1);
      const fixed = blocksStr.replace(/,(\s*[}\]])/g, '$1');
      const blocks = JSON.parse(fixed);
      const content = blocks.map((b) => {
        const out = { type: b.type };
        if (b.speaker) out.speaker = b.speaker;
        if (b.text !== undefined) out.text = b.text;
        if (b.description !== undefined) out.description = b.description;
        if (b.title !== undefined) out.title = b.title;
        if (b.parenthetical !== undefined) out.parenthetical = b.parenthetical;
        if (b.isUncut !== undefined) out.isUncut = b.isUncut;
        return out;
      });
      const outDir = 'src/main/resources/db/changelog/titles/friends/season-1/episode-2';
      fs.mkdirSync(outDir, { recursive: true });
      fs.writeFileSync(outDir + '/content.json', JSON.stringify(content, null, 2));
      console.log('Converted', content.length, 'blocks');
      process.exit(0);
    }
  }
}
throw new Error('Could not parse blocks');
