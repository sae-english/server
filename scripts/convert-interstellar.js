#!/usr/bin/env node
/**
 * Converts IMSDb Interstellar HTML script to content.json (unified block format).
 * Run: node scripts/convert-interstellar.js [path-to-Interstellar.html]
 *
 * Block types: scene, dialogue, action, transition, section
 * Optional fields: isUncut, parenthetical
 */
const fs = require('fs');
const path = require('path');

const htmlPath = process.argv[2] || path.join(process.env.HOME || '', 'Downloads/Interstellar.html');
const outDir = path.join(__dirname, '../src/main/resources/db/changelog/movies/interstellar');

const raw = fs.readFileSync(htmlPath, 'utf8');

const preMatch = raw.match(/<pre[^>]*>([\s\S]*?)<\/pre>/);
if (!preMatch) throw new Error('Could not find <pre> block');
let html = preMatch[1]
  .replace(/&amp;/g, '&')
  .replace(/&lt;/g, '<')
  .replace(/&gt;/g, '>');

// Replace <b>...</b> (including across newlines) with markers: BOLD[content]BOLD
const boldBlocks = [];
html = html.replace(/<b>([\s\S]*?)<\/b>/gi, (_, g) => {
  const t = g.trim();
  boldBlocks.push(t);
  return `\n<<B${boldBlocks.length - 1}>>\n`;
});

// Split into tokens: lines of plain text and <<B0>> markers
const lines = html.split(/\r?\n/);

const content = [];
let currentDialogue = null;
let currentAction = null;

function flushAction() {
  if (currentAction) {
    const text = currentAction.join(' ').replace(/\s+/g, ' ').trim();
    if (text) content.push({ type: 'action', text });
    currentAction = null;
  }
}

function flushDialogue() {
  if (currentDialogue && currentDialogue.text) {
    const block = { type: 'dialogue', speaker: currentDialogue.speaker, text: currentDialogue.text.replace(/\s+/g, ' ').trim() };
    if (currentDialogue.parenthetical) block.parenthetical = currentDialogue.parenthetical;
    content.push(block);
  }
  currentDialogue = null;
}

function isScene(s) {
  const t = s.trim();
  return /^(INT\.|EXT\.|INT\/EXT\.)/i.test(t) && (t.includes('--') || t.includes(' - '));
}

function isTransition(s) {
  const t = s.trim().toUpperCase();
  return ['CUT TO:', 'FADE TO BLACK', 'FADE IN:', 'FADE OUT.', 'DISSOLVE TO:', 'SMASH CUT:'].some((x) => t === x || t.startsWith(x));
}

function isSection(s) {
  const t = s.trim().toUpperCase();
  const sections = ['END', 'CLOSING CREDITS', 'OPENING CREDITS', 'COMMERCIAL BREAK', 'FIFTY YEARS LATER', 'TITLE', 'INTERSTELLAR', 'STORY BY', 'MARCH'];
  if (sections.includes(t) || sections.some((x) => t.startsWith(x + ' '))) return true;
  if (/^\d{4}$/.test(t)) return true; // year
  return false;
}

function isParenthetical(s) {
  const t = s.trim();
  return t.startsWith('(') && t.endsWith(')') && t.length > 2;
}

function isSceneNumber(s) {
  return /^\d+\.?\s*$/.test(s.trim());
}

function isSpeaker(s) {
  const t = s.trim();
  if (!t || t.length > 55) return false;
  if (isSceneNumber(t)) return false;
  if (isScene(t) || isTransition(t) || isSection(t) || isParenthetical(t)) return false;
  if (t.endsWith('-')) return false; // Interruption e.g. "CALL ANYBODY-"
  if (/^[A-Z]+\.\s*$/.test(t)) return false; // "SPACE." etc - action slug
  if (/^[A-Z][A-Z0-9\s\.\'\-\&\/]+$/.test(t)) return true;
  if (/^[A-Z][A-Z0-9\s\.\'\-\&\/]*\s*\([A-Z\'\d\s]+\)\s*$/i.test(t)) return true;
  return false;
}

let i = 0;
while (i < lines.length) {
  const line = lines[i];
  const trimmed = line.trim();
  i++;

  // Bold marker (may have trailing text on same line)
  const boldMatch = trimmed.match(/<<B(\d+)>>/);
  if (boldMatch) {
    const remainder = trimmed.slice(boldMatch.index + boldMatch[0].length).trim();
    const boldText = boldBlocks[parseInt(boldMatch[1], 10)] || '';

    if (isSceneNumber(boldText)) continue;

    if (isScene(boldText)) {
      flushDialogue();
      flushAction();
      content.push({ type: 'scene', description: boldText.trim() });
      if (remainder) { currentAction = [remainder]; }
      continue;
    }

    if (isTransition(boldText)) {
      flushDialogue();
      flushAction();
      content.push({ type: 'transition', text: boldText.trim() });
      if (remainder) { currentAction = [remainder]; }
      continue;
    }

    if (isSection(boldText)) {
      flushDialogue();
      flushAction();
      content.push({ type: 'section', title: boldText.trim() });
      if (remainder) { currentAction = [remainder]; }
      continue;
    }

    if (isParenthetical(boldText)) {
      const p = boldText.replace(/^\(|\)$/g, '').trim();
      if (currentDialogue) { currentDialogue.parenthetical = p; currentDialogue.text = (currentDialogue.text + ' ' + remainder).trim(); }
      else if (remainder) { currentAction = currentAction || []; currentAction.push(remainder); }
      continue;
    }

    if (isSpeaker(boldText)) {
      flushDialogue();
      flushAction();
      currentDialogue = { speaker: boldText.trim(), text: remainder || '', parenthetical: null };
      continue;
    }

    // Bold that's not a block type - e.g. "SPACE.", "CALL ANYBODY-" (emphasis)
    flushDialogue();
    currentAction = currentAction || [];
    currentAction.push(boldText);
    if (remainder) currentAction.push(remainder);
    continue;
  }

  // Plain text line
  if (!trimmed) continue;

  if (currentDialogue) {
    currentDialogue.text = (currentDialogue.text + ' ' + trimmed).trim();
  } else {
    currentAction = currentAction || [];
    currentAction.push(trimmed);
  }
}

flushDialogue();
flushAction();

fs.mkdirSync(outDir, { recursive: true });
const outPath = path.join(outDir, 'content.json');
fs.writeFileSync(outPath, JSON.stringify(content, null, 2), 'utf8');
console.log(`Converted ${content.length} blocks -> ${outPath}`);
