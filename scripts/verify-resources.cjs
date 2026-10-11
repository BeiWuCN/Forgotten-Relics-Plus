const fs = require('node:fs');
const path = require('node:path');
const {execFileSync} = require('node:child_process');
const root = path.resolve(__dirname, '..');
const namespace = 'forgotten_relics_plus';
const main = path.join(root, 'src/main/resources');
const generated = path.join(root, 'src/generated/resources');
const problems = [];
const read = file => JSON.parse(fs.readFileSync(file, 'utf8').replace(/^\uFEFF/, ''));
function files(dir) {
  if (!fs.existsSync(dir)) return [];
  return fs.readdirSync(dir, {withFileTypes: true}).flatMap(entry => {
    const file = path.join(dir, entry.name);
    return entry.name === '.cache' ? [] : entry.isDirectory() ? files(file) : [file];
  });
}
function jarEntries(file) {
  return execFileSync('jar', ['tf', file], {encoding: 'utf8', maxBuffer: 64 * 1024 * 1024})
    .split(/\r?\n/).filter(Boolean);
}
const resources = new Set();
for (const dir of [main, generated]) {
  for (const file of files(dir)) {
    const relative = path.relative(dir, file).replaceAll(path.sep, '/');
    if (resources.has(relative)) problems.push(`Duplicate source resource: ${relative}`);
    resources.add(relative);
    if (file.endsWith('.json') || file.endsWith('.mcmeta')) {
      try { read(file); } catch (error) { problems.push(`${relative}: ${error.message}`); }
    }
  }
}
const ownItems = new Set([...fs.readFileSync(path.join(root,
  'src/main/java/com/beiwu/forgottenrelicsplus/registry/FRItems.java'), 'utf8')
  .matchAll(/ITEMS\.registerItem\(\s*"([a-z0-9_]+)"/g)].map(match => `${namespace}:${match[1]}`));
const items = new Set(ownItems);
const tags = new Set();
const aspects = new Set();
const categories = new Set();
const upstreamResearch = new Set();
const externalResources = new Set();
const lock = read(path.join(root, 'libs/dependencies.lock.json'));
const artifacts = path.join(root, 'build/moddev/artifacts');
const jars = lock.dependencies.map(dependency => path.join(root, 'libs', dependency.file)).filter(fs.existsSync);
if (fs.existsSync(artifacts)) {
  jars.push(...fs.readdirSync(artifacts).filter(file => file.endsWith('.jar') && !file.endsWith('-sources.jar'))
    .map(file => path.join(artifacts, file)));
}
for (const jar of jars) {
  for (const entry of jarEntries(jar)) {
    externalResources.add(entry);
    let match = /^assets\/([^/]+)\/models\/item\/(.+)\.json$/.exec(entry);
    if (match) items.add(`${match[1]}:${match[2]}`);
    match = /^data\/([^/]+)\/tags\/item\/(.+)\.json$/.exec(entry);
    if (match) tags.add(`${match[1]}:${match[2]}`);
    match = /^data\/([^/]+)\/thaumaturge\/(aspect|research_category|research_entry)\/(.+)\.json$/.exec(entry);
    if (match) ({aspect: aspects, research_category: categories, research_entry: upstreamResearch})[match[2]]
      .add(`${match[1]}:${match[3]}`);
  }
}
const itemNamespaces = new Set([...items].map(item => item.split(':')[0]));
const entries = new Map();
const researchDir = path.join(generated, `data/${namespace}/thaumaturge/research_entry`);
for (const file of files(researchDir)) entries.set(`${namespace}:${path.basename(file, '.json')}`, read(file));
const definitions = files(path.join(root, 'src/main/java/com/beiwu/forgottenrelicsplus/data'))
  .filter(file => /^FR(Basic|Relic|Tome|Weapon)Research\.java$/.test(path.basename(file)))
  .flatMap(file => [...fs.readFileSync(file, 'utf8').matchAll(/\.entry\(\s*"([a-z0-9_]+)"/g)]
    .map(match => `${namespace}:${match[1]}`));
if (new Set(definitions).size !== definitions.length) problems.push('Duplicate research definitions');
for (const id of definitions) if (!entries.has(id)) problems.push(`Missing generated research: ${id}`);
for (const id of entries.keys()) if (!definitions.includes(id)) problems.push(`Stale generated research: ${id}`);
const categoryDir = path.join(generated, `data/${namespace}/thaumaturge/research_category`);
for (const file of files(categoryDir)) categories.add(`${namespace}:${path.basename(file, '.json')}`);
const languages = ['en_us', 'zh_cn'].map(language => [language,
  read(path.join(main, `assets/${namespace}/lang/${language}.json`))]);
function text(key, location) {
  for (const [language, values] of languages) if (!Object.hasOwn(values, key)) problems.push(`${location}: missing ${language} key ${key}`);
}
function item(id, location, conditionalMods = new Set()) {
  const ns = id.split(':')[0];
  if (conditionalMods.has(ns) && !itemNamespaces.has(ns)) return;
  if (id.startsWith(`${namespace}:`) ? !ownItems.has(id) : !items.has(id)) problems.push(`${location}: missing item ${id}`);
}
function requirement(value, location) {
  if (Array.isArray(value)) value.forEach(entry => requirement(entry, location));
  else if (typeof value === 'string') {
    if (value.startsWith('#')) {
      if (!tags.has(value.slice(1))) problems.push(`${location}: missing item tag ${value}`);
    } else item(value, location);
  }
}
function aspectList(list, location) {
  for (const entry of list || []) {
    if (!aspects.has(entry.aspect)) problems.push(`${location}: missing aspect ${entry.aspect}`);
    if (!((entry.amount ?? 1) > 0)) problems.push(`${location}: nonpositive aspect amount`);
  }
}
function researchReference(value, location) {
  const id = value.replace(/^~/, '').split('@')[0];
  if (!entries.has(id) && !upstreamResearch.has(id)) problems.push(`${location}: missing research ${id}`);
  return id;
}
const recipes = new Map();
const recipeDir = path.join(main, `data/${namespace}/recipe`);
for (const file of files(recipeDir)) recipes.set(`${namespace}:${path.relative(recipeDir, file).replaceAll(path.sep, '/').replace(/\.json$/, '')}`, read(file));
for (const [id, resource] of [
  [`${namespace}:omega_core`, entries.get(`${namespace}:omega_core`)],
  [`${namespace}:infusion/omega_core`, recipes.get(`${namespace}:infusion/omega_core`)]
]) {
  const conditions = resource?.['neoforge:conditions'] || [];
  if (!conditions.some(condition => condition.type === 'neoforge:mod_loaded' && condition.modid === 'avaritia')) {
    problems.push(`${id}: missing Re:Avaritia loading condition`);
  }
}
for (const [id, entry] of entries) {
  if (!categories.has(entry.category)) problems.push(`${id}: missing category ${entry.category}`);
  text(entry.name, id);
  if (!entry.stages?.length) problems.push(`${id}: no stages`);
  for (const parent of entry.parents || []) researchReference(parent, id);
  for (const icon of entry.icons || []) {
    if (icon.startsWith('focus:')) continue;
    if (icon.endsWith('.png')) {
      const [ns, texture] = icon.split(':');
      const resource = `assets/${ns}/${texture}`;
      if (!resources.has(resource) && !externalResources.has(resource)) problems.push(`${id}: missing icon ${icon}`);
    } else item(icon, id);
  }
  aspectList(entry.note_aspects, id);
  const hasRecipes = (entry.stages || []).some(stage => stage.recipes?.length);
  if (hasRecipes) {
    if (!(entry.complexity >= 2 && entry.complexity <= 8)) problems.push(`${id}: note complexity must be 2..8`);
    if ((entry.note_aspects || []).length < 3) problems.push(`${id}: missing authored note anchors`);
    const first = entry.stages[0];
    if (first.recipes?.length || entry.stages.length < 2) problems.push(`${id}: recipe page precedes its note`);
    const theory = (first.required_knowledge || []).filter(value => value.type === 'theory');
    if (theory.length !== 1 || theory[0].amount !== 1) problems.push(`${id}: first page must require one completed research note`);
  }
  for (const stage of entry.stages || []) {
    text(stage.text, id);
    for (const reference of stage.required_research || []) researchReference(reference, id);
    for (const knowledge of [...(stage.required_knowledge || []), ...(stage.knowledge || [])]) {
      if (!categories.has(knowledge.category)) problems.push(`${id}: missing knowledge category ${knowledge.category}`);
    }
    for (const entry of [...(stage.obtain || []), ...(stage.craft || [])]) requirement(entry.items, id);
    for (const recipeId of stage.recipes || []) {
      const recipe = recipes.get(recipeId);
      if (!recipe) problems.push(`${id}: missing recipe ${recipeId}`);
      else if (recipe.research?.entry !== id) problems.push(`${id}: recipe ${recipeId} points to ${recipe.research?.entry}`);
    }
  }
}
for (const [id, recipe] of recipes) {
  const conditionalMods = new Set((recipe['neoforge:conditions'] || [])
    .filter(condition => condition.type === 'neoforge:mod_loaded')
    .map(condition => condition.modid));
  if (recipe.research?.entry) researchReference(recipe.research.entry, id);
  if (recipe.type === 'thaumaturge:infusion' && !recipe.research?.entry) problems.push(`${id}: infusion bypasses research notes`);
  aspectList(recipe.aspects, id);
  function ingredient(value) {
    if (!value || typeof value !== 'object') return;
    if (Array.isArray(value)) value.forEach(ingredient);
    else {
      if (value.item) item(value.item, id, conditionalMods);
      if (value.tag && !tags.has(value.tag)) problems.push(`${id}: missing tag ${value.tag}`);
      Object.values(value).forEach(ingredient);
    }
  }
  ingredient(recipe);
  if (recipe.result?.id) item(recipe.result.id, id);
}
for (const file of files(categoryDir)) {
  const category = read(file);
  if (category.required_research) researchReference(category.required_research, file);
  aspectList(category.formula, file);
}
const baseAspects = read(path.join(main, 'data/thaumaturge/data_maps/item/base_aspects.json'));
for (const [id, list] of Object.entries(baseAspects.values)) {
  if (!id.startsWith('#')) item(id, 'base_aspects');
  aspectList(list, `base_aspects/${id}`);
}
const visiting = new Set(), visited = new Set();
function visit(id) {
  if (visiting.has(id)) { problems.push(`Research dependency cycle at ${id}`); return; }
  if (visited.has(id)) return;
  visiting.add(id);
  for (const parent of entries.get(id)?.parents || []) {
    const target = parent.replace(/^~/, '').split('@')[0];
    if (entries.has(target)) visit(target);
  }
  visiting.delete(id); visited.add(id);
}
for (const id of entries.keys()) visit(id);
const jarDir = path.join(root, 'build/libs');
const output = fs.readdirSync(jarDir).filter(file => file.startsWith(`${namespace}-`) && file.endsWith('.jar') && !file.endsWith('-sources.jar'));
if (output.length !== 1) problems.push(`Expected one release JAR, found ${output.length}`);
else {
  const packed = jarEntries(path.join(jarDir, output[0]));
  const unique = new Set();
  for (const resource of packed) {
    if (unique.has(resource)) problems.push(`Duplicate JAR entry: ${resource}`);
    if (resource.startsWith('.cache/')) problems.push(`Datagen cache in JAR: ${resource}`);
    unique.add(resource);
  }
  for (const resource of resources) if (!unique.has(resource)) problems.push(`Missing packaged resource: ${resource}`);
  const expectedResearch = resources.size && [...resources].filter(file => /\/thaumaturge\/research_(entry|category)\/.+\.json$/.test(file)).length;
  const actualResearch = packed.filter(file => /\/thaumaturge\/research_(entry|category)\/.+\.json$/.test(file)).length;
  if (actualResearch !== expectedResearch) problems.push(`Research packaging mismatch: ${actualResearch}/${expectedResearch}`);
}
if (problems.length) {
  console.error(problems.join('\n'));
  process.exitCode = 1;
} else console.log(`Verified ${entries.size} research entries, ${recipes.size} recipes, all resource references and JAR packaging.`);
