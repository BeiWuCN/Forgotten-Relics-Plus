const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');
const {spawnSync} = require('node:child_process');
const assert = require('node:assert/strict');
const root = path.resolve(__dirname, '..');
const generated = path.join(root, 'src/generated/resources');
function snapshot(dir, prefix = '') {
  const result = {};
  if (!fs.existsSync(dir)) return result;
  for (const entry of fs.readdirSync(dir, {withFileTypes: true})) {
    if (entry.name === '.cache') continue;
    const file = path.join(dir, entry.name), key = `${prefix}${entry.name}`;
    if (entry.isDirectory()) Object.assign(result, snapshot(file, `${key}/`));
    else result[key] = crypto.createHash('sha256').update(fs.readFileSync(file)).digest('hex');
  }
  return result;
}
const before = snapshot(generated);
const windows = process.platform === 'win32';
const result = windows
  ? spawnSync('cmd.exe', ['/d', '/s', '/c', 'gradlew.bat runData --console=plain'], {cwd: root, stdio: 'inherit'})
  : spawnSync('./gradlew', ['runData', '--console=plain'], {cwd: root, stdio: 'inherit'});
if (result.error) throw result.error;
if (result.status !== 0) process.exit(result.status || 1);
assert.deepStrictEqual(snapshot(generated), before, 'Generated resources changed. Review and commit runData output.');
console.log('Datagen output matches the committed resource snapshot.');
