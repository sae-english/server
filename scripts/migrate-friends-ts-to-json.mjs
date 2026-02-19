#!/usr/bin/env node
/**
 * Миграция эпизодов Friends из старого приложения (.ts) в storage series_episode/friends по сезонам.
 * Использование: node scripts/migrate-friends-ts-to-json.mjs
 * Путь к старым данным и к storage задаётся в константах ниже.
 */

import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));

const SOURCE_ROOT = path.join(__dirname, '../../../../Education/English/FriendsEnglish/server/src/db/friends');
const TARGET_ROOT = path.join(__dirname, '../storage/series_episode/friends');

function uuid() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0;
    const v = c === 'x' ? r : (r & 0x3) | 0x8;
    return v.toString(16);
  });
}

function extractTranscript(tsContent) {
  const match = tsContent.match(/export\s+const\s+transcript[^=]*=\s*(\{[\s\S]*?\});\s*$/m);
  if (!match) throw new Error('Could not find transcript object');
  const objStr = match[1];
  return new Function('return ' + objStr)();
}

function blockToContent(block) {
  const base = { id: uuid(), type: block.type };
  if (block.type === 'section') return { ...base, title: block.title };
  if (block.type === 'scene') return { ...base, description: block.description };
  if (block.type === 'action') return { ...base, text: block.text, ...(block.isUncut != null && { isUncut: block.isUncut }) };
  if (block.type === 'transition') return { ...base, text: block.text };
  if (block.type === 'dialogue') {
    const out = { ...base, speaker: block.speaker, text: block.text };
    if (block.parenthetical != null) out.parenthetical = block.parenthetical;
    if (block.isUncut != null) out.isUncut = block.isUncut;
    return out;
  }
  return { ...base, ...block };
}

function run() {
  if (!fs.existsSync(SOURCE_ROOT)) {
    console.error('Source not found:', SOURCE_ROOT);
    process.exit(1);
  }

  const seasonDirs = fs.readdirSync(SOURCE_ROOT)
    .filter((n) => /^\d+$/.test(n))
    .map((n) => ({ name: n, num: parseInt(n, 10) }))
    .sort((a, b) => a.num - b.num);

  let total = 0;
  for (const { name: seasonName, num: seasonNum } of seasonDirs) {
    const seasonPath = path.join(SOURCE_ROOT, seasonName);
    const files = fs.readdirSync(seasonPath)
      .filter((n) => n.endsWith('.ts'))
      .map((n) => ({ name: n, num: parseInt(n.replace('.ts', ''), 10) }))
      .sort((a, b) => a.num - b.num);

    const targetSeasonDir = path.join(TARGET_ROOT, `season-${seasonNum}`);
    fs.mkdirSync(targetSeasonDir, { recursive: true });

    for (let i = 0; i < files.length; i++) {
      const file = files[i];
      const episodeNum = i + 1;
      const tsPath = path.join(seasonPath, file.name);
      const content = fs.readFileSync(tsPath, 'utf8');
      const transcript = extractTranscript(content);

      const contentJson = {
        season: seasonNum,
        episode_number: episodeNum,
        episode_title: transcript.title,
        credits: transcript.credits || {},
        note: transcript.note ?? null,
        content: (transcript.blocks || []).map(blockToContent),
      };

      const episodeDir = path.join(targetSeasonDir, `episode-${episodeNum}`);
      fs.mkdirSync(episodeDir, { recursive: true });
      fs.writeFileSync(
        path.join(episodeDir, 'content.json'),
        JSON.stringify(contentJson, null, 2),
        'utf8'
      );
      total++;
    }
  }

  console.log('Migrated', total, 'episodes to', TARGET_ROOT);
}

run();
