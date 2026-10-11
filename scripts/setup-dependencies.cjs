const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');
const {Readable} = require('node:stream');
const {pipeline} = require('node:stream/promises');
const root = path.resolve(__dirname, '..');
const libs = path.join(root, 'libs');
const lock = JSON.parse(fs.readFileSync(path.join(libs, 'dependencies.lock.json'), 'utf8'));
const options = process.argv.slice(2);
const option = name => options.includes(name) ? options[options.indexOf(name) + 1] : undefined;
const source = option('--source');
const baseUrl = option('--base-url') || process.env.DEPS_BASE_URL;
async function hash(file) {
  const digest = crypto.createHash('sha256');
  for await (const chunk of fs.createReadStream(file)) digest.update(chunk);
  return digest.digest('hex');
}
async function setup() {
  for (const dependency of lock.dependencies) {
    const file = path.join(libs, dependency.file);
    if (fs.existsSync(file)) {
      if (await hash(file) !== dependency.sha256) throw new Error(`Checksum mismatch: ${dependency.file}`);
      continue;
    }
    if (dependency.optional && !options.includes('--optional')) continue;
    const temporary = `${file}.download`;
    try {
      const local = source && path.join(path.resolve(source), dependency.file);
      if (local && fs.existsSync(local)) fs.copyFileSync(local, temporary);
      else {
        const url = dependency.url || (baseUrl && new URL(encodeURIComponent(dependency.file), `${baseUrl.replace(/\/$/, '')}/`).href);
        if (!url) throw new Error(`No download source for ${dependency.file}. Use --source <directory> or DEPS_BASE_URL.`);
        const response = await fetch(url, {signal: AbortSignal.timeout(120000)});
        if (!response.ok) throw new Error(`Download failed for ${dependency.file}: HTTP ${response.status}`);
        await pipeline(Readable.fromWeb(response.body), fs.createWriteStream(temporary));
      }
      if (await hash(temporary) !== dependency.sha256) throw new Error(`Checksum mismatch: ${dependency.file}`);
      fs.renameSync(temporary, file);
      console.log(`Installed ${dependency.file}`);
    } finally {
      if (fs.existsSync(temporary)) fs.unlinkSync(temporary);
    }
  }
  console.log('Locked dependencies verified.');
}
setup().catch(error => { console.error(error.message); process.exitCode = 1; });
