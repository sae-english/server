#!/usr/bin/env node
/**
 * Generates episode.sql from meta.json and content.json.
 * Reads directories from scripts/episode-sources.json.
 * Run: node scripts/generate-episode-sql.js
 *
 * Structure:
 * - Series: season-N/episode-M/{meta.json, content.json, episode.sql (generated)}
 * - Movies: movie/{meta.json, content.json, episode.sql (generated)} — no episode-N subfolder
 */
const fs = require('fs');
const path = require('path');

const projectRoot = path.join(__dirname, '..');
const configPath = path.join(__dirname, 'episode-sources.json');

function escapeSqlString(str) {
  if (str == null) return 'NULL';
  return "'" + String(str).replace(/'/g, "''") + "'";
}

function generateSql(episodeDir, episodeId) {
  const metaPath = path.join(episodeDir, 'meta.json');
  const contentPath = path.join(episodeDir, 'content.json');

  if (!fs.existsSync(metaPath)) {
    console.warn(`Skipping ${episodeId}: meta.json not found`);
    return;
  }
  if (!fs.existsSync(contentPath)) {
    console.warn(`Skipping ${episodeId}: content.json not found`);
    return;
  }

  const meta = JSON.parse(fs.readFileSync(metaPath, 'utf8'));
  const content = JSON.parse(fs.readFileSync(contentPath, 'utf8'));

  const contentJson = JSON.stringify(content);
  const creditsJson = JSON.stringify(meta.credits || {});

  const contentPg = '$json$' + contentJson + '$json$';
  const creditsPg = '$credits$' + creditsJson + '$credits$';

  const epNum = String(meta.episode_number).padStart(2, '0');
  const comment = meta.season == null ? 'Movie' : `S${meta.season}E${epNum}`;
  const label = episodeId === 'script' ? 'script' : episodeId.replace('episode-', '');
  const sql = `-- ${label} ${comment}
INSERT INTO englishmovies.episode (title_id, season, episode_number, episode_title, content, credits, note)
VALUES (
  ${meta.title_id}, ${meta.season}, ${meta.episode_number},
  ${escapeSqlString(meta.episode_title)},
  ${contentPg}::jsonb,
  ${creditsPg}::jsonb,
  ${escapeSqlString(meta.note)}
)
ON CONFLICT (title_id, season, episode_number) DO NOTHING;
`;

  const sqlPath = path.join(episodeDir, 'episode.sql');
  fs.writeFileSync(sqlPath, sql);
  const relPath = path.relative(projectRoot, sqlPath);
  console.log(`Generated ${relPath}`);
}

// Read season dirs from config
if (!fs.existsSync(configPath)) {
  console.error('Config not found:', configPath);
  process.exit(1);
}

const seasonDirs = JSON.parse(fs.readFileSync(configPath, 'utf8'));
if (!Array.isArray(seasonDirs) || seasonDirs.length === 0) {
  console.warn('No season directories in episode-sources.json');
  process.exit(0);
}

let totalGenerated = 0;

for (const relPath of seasonDirs) {
  const dir = path.join(projectRoot, relPath);

  if (!fs.existsSync(dir)) {
    console.warn(`Skipping (not found): ${relPath}`);
    continue;
  }

  const metaPath = path.join(dir, 'meta.json');
  const contentPath = path.join(dir, 'content.json');

  // Movie: meta.json + content.json directly in dir (e.g. interstellar/movie/)
  if (fs.existsSync(metaPath) && fs.existsSync(path.join(dir, 'content.json'))) {
    generateSql(dir, 'script');
    totalGenerated++;
    continue;
  }

  // Series: episode-N subfolders (e.g. friends/season-1/episode-1/)
  const entries = fs.readdirSync(dir, { withFileTypes: true });
  const episodeDirs = entries
    .filter((e) => e.isDirectory() && /^episode-\d+$/.test(e.name))
    .map((e) => ({ id: e.name, path: path.join(dir, e.name) }));

  for (const { id, path: episodeDir } of episodeDirs) {
    generateSql(episodeDir, id);
    totalGenerated++;
  }
}

if (totalGenerated === 0) {
  console.warn('No episode-N folders found in configured season directories');
} else {
  console.log(`Done. Generated ${totalGenerated} episode(s).`);
}
