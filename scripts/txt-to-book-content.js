#!/usr/bin/env node
/**
 * Конвертирует TXT книги (с заголовками PROLOGUE, CHAPTER N, EPILOGUE) в content.json для storage.
 * Сохраняет структуру: главы (section), абзацы (text), отступы и переносы внутри абзацев.
 *
 * Использование: node scripts/txt-to-book-content.js <input.txt> [output-dir]
 * По умолчанию output: storage/books/the-secret-of-secrets/content.json (относительно cwd).
 */

const fs = require('fs');
const path = require('path');

const SECTION_HEADER = /^\s*(PROLOGUE|CHAPTER\s+(\d+)|EPILOGUE)\s*$/i;
const OCEAN_LINE = /^\s*OceanofPDF\.com\s*$/i;
const END_MARKER = /^\s*-\s*the\s+end\s*-\s*$/i;
const ACK_HEADER = /^\s*A\s*C\s*K\s*N\s*O\s*W\s*L\s*E\s*D\s*G\s*M\s*E\s*N\s*T\s*S\s*$/i;

function sectionTitle(match) {
  const raw = (match[1] || '').trim();
  if (/^PROLOGUE$/i.test(raw)) return 'Prologue';
  if (/^EPILOGUE$/i.test(raw)) return 'Epilogue';
  const ch = raw.match(/CHAPTER\s+(\d+)/i);
  if (ch) return `Chapter ${parseInt(ch[1], 10)}`;
  return raw;
}

function parseSections(lines) {
  const sections = [];
  let i = 0;
  while (i < lines.length) {
    const m = lines[i].match(SECTION_HEADER);
    if (m) {
      const title = sectionTitle(m);
      const start = i + 1;
      i++;
      while (i < lines.length) {
        if (lines[i].match(SECTION_HEADER) || lines[i].match(ACK_HEADER)) break;
        if (lines[i].match(END_MARKER)) {
          i++;
          break;
        }
        i++;
      }
      const end = lines[i - 1].match(END_MARKER) ? i - 1 : i;
      const block = lines.slice(start, end);
      sections.push({ title, lines: block });
      if (lines[i - 1].match(END_MARKER)) break;
      continue;
    }
    i++;
  }
  return sections;
}

function cleanLines(lines) {
  return lines.filter((line) => !OCEAN_LINE.test(line));
}

function splitParagraphs(lines) {
  const text = lines.join('\n');
  return text
    .split(/\n\n+/)
    .map((p) => p.trim())
    .filter((p) => p.length > 0);
}

function uuid() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0;
    const v = c === 'x' ? r : (r & 0x3) | 0x8;
    return v.toString(16);
  });
}

function main() {
  const inputPath = process.argv[2];
  if (!inputPath || !fs.existsSync(inputPath)) {
    console.error('Usage: node txt-to-book-content.js <input.txt> [output-dir]');
    process.exit(1);
  }

  const cwd = process.cwd();
  const outputDir = process.argv[3]
    ? path.resolve(process.argv[3])
    : path.join(cwd, 'storage', 'books', 'the-secret-of-secrets');
  const outputPath = path.join(outputDir, 'content.json');

  const raw = fs.readFileSync(inputPath, 'utf-8');
  const lines = raw.split(/\r?\n/);

  const sections = parseSections(lines);
  const content = [];

  for (const { title, lines: sectionLines } of sections) {
    const cleaned = cleanLines(sectionLines);
    const paragraphs = splitParagraphs(cleaned);

    content.push({
      type: 'section',
      id: `section-${title.toLowerCase().replace(/\s+/g, '-')}`,
      title,
    });

    for (let i = 0; i < paragraphs.length; i++) {
      content.push({
        type: 'text',
        id: uuid(),
        text: paragraphs[i],
      });
    }
  }

  const book = {
    name: 'The Secret of Secrets',
    content_key: 'the-secret-of-secrets',
    author: 'Dan Brown',
    year: 2025,
    description: null,
    note: null,
  };

  const result = { book, content };
  fs.mkdirSync(outputDir, { recursive: true });
  const pretty = process.argv.includes('--pretty') || process.env.PRETTY === '1';
  const json = pretty ? JSON.stringify(result, null, 2) : JSON.stringify(result);
  fs.writeFileSync(outputPath, json, 'utf-8');
  console.log('Written:', outputPath, 'sections:', sections.length, 'blocks:', content.length, pretty ? '(pretty)' : '(compact)');
}

main();
