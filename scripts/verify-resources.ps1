$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem
$root = Split-Path $PSScriptRoot
$namespace = 'forgotten_relics_plus'
$resources = @{}
$items = @{}
$tags = @{}
$aspects = @{}
$categories = @{}
$research = @{}
$external = @{}
$entries = @{}
$recipes = @{}
$languages = @{}
function ReadJson($file) { Get-Content -LiteralPath $file -Raw | ConvertFrom-Json -AsHashtable }
function Require($condition, $message) { if (!$condition) { throw $message } }
foreach ($directory in @('src/main/resources', 'src/generated/resources')) {
    $base = Join-Path $root $directory
    foreach ($file in Get-ChildItem $base -File -Recurse | Where-Object FullName -NotMatch '[\\/]\.cache[\\/]') {
        $relative = [IO.Path]::GetRelativePath($base, $file.FullName).Replace('\', '/')
        Require (!$resources.ContainsKey($relative)) "Duplicate resource: $relative"
        $resources[$relative] = $true
        if ($file.Extension -in @('.json', '.mcmeta')) { $null = ReadJson $file.FullName }
    }
}
$lock = ReadJson (Join-Path $root 'libs/dependencies.lock.json')
$jars = @($lock.dependencies | ForEach-Object { Join-Path $root "libs/$($_.file)" } | Where-Object { Test-Path -LiteralPath $_ })
$jars += @(Get-ChildItem (Join-Path $root 'build/moddev/artifacts') -Filter '*.jar' | Where-Object Name -NotLike '*-sources.jar' | ForEach-Object FullName)
foreach ($jar in $jars) {
    $archive = [IO.Compression.ZipFile]::OpenRead($jar)
    try {
        foreach ($entry in $archive.Entries.FullName) {
            $external[$entry] = $true
            if ($entry -match '^assets/([^/]+)/models/item/(.+)\.json$') { $items["$($Matches[1]):$($Matches[2])"] = $true }
            if ($entry -match '^data/([^/]+)/tags/item/(.+)\.json$') { $tags["$($Matches[1]):$($Matches[2])"] = $true }
            if ($entry -match '^data/([^/]+)/thaumaturge/(aspect|research_category|research_entry)/(.+)\.json$') {
                $target = switch ($Matches[2]) { aspect { $aspects } research_category { $categories } research_entry { $research } }
                $target["$($Matches[1]):$($Matches[3])"] = $true
            }
        }
    } finally { $archive.Dispose() }
}
$ownItems = @{}
$itemSource = Get-Content (Join-Path $root 'src/main/java/com/beiwu/forgottenrelicsplus/registry/FRItems.java') -Raw
foreach ($match in [regex]::Matches($itemSource, 'ITEMS\.registerItem\(\s*"([a-z0-9_]+)"')) {
    $id = "${namespace}:$($match.Groups[1].Value)"
    $items[$id] = $true
    $ownItems[$id] = $true
}
foreach ($file in Get-ChildItem (Join-Path $root "src/generated/resources/data/$namespace/thaumaturge/research_entry") -Filter '*.json') {
    $id = "${namespace}:$($file.BaseName)"
    $entries[$id] = ReadJson $file.FullName
    $research[$id] = $true
}
foreach ($file in Get-ChildItem (Join-Path $root "src/generated/resources/data/$namespace/thaumaturge/research_category") -Filter '*.json') {
    $categories["${namespace}:$($file.BaseName)"] = $true
    $category = ReadJson $file.FullName
    if ($category.required_research) { Require ($research.ContainsKey($category.required_research)) 'Missing category research' }
}
$definitions = @{}
foreach ($file in Get-ChildItem (Join-Path $root 'src/main/java/com/beiwu/forgottenrelicsplus/data') -Filter '*Research.java') {
    foreach ($match in [regex]::Matches((Get-Content $file.FullName -Raw), '\.entry\(\s*"([a-z0-9_]+)"')) {
        $id = "${namespace}:$($match.Groups[1].Value)"
        Require (!$definitions.ContainsKey($id)) "Duplicate research: $id"
        $definitions[$id] = $true
        Require ($entries.ContainsKey($id)) "Missing generated research: $id"
    }
}
Require ($definitions.Count -eq $entries.Count) 'Stale generated research'
foreach ($language in @('en_us', 'zh_cn')) { $languages[$language] = ReadJson (Join-Path $root "src/main/resources/assets/$namespace/lang/$language.json") }
$recipeRoot = Join-Path $root "src/main/resources/data/$namespace/recipe"
foreach ($file in Get-ChildItem $recipeRoot -Filter '*.json' -Recurse) {
    $id = "${namespace}:" + [IO.Path]::GetRelativePath($recipeRoot, $file.FullName).Replace('\', '/').Replace('.json', '')
    $recipes[$id] = ReadJson $file.FullName
}
function CheckText($key) { foreach ($language in $languages.Values) { Require ($language.ContainsKey($key)) "Missing translation: $key" } }
function CheckResearch($id) { $id = $id.TrimStart('~').Split('@')[0]; Require ($research.ContainsKey($id)) "Missing research: $id" }
function CheckItem($id, $optional = @()) {
    $itemNamespace = $id.Split(':')[0]
    if ($optional -contains $itemNamespace -and !@($items.Keys | Where-Object { $_.StartsWith($itemNamespace + ':') }).Count) { return }
    $valid = if ($itemNamespace -eq $namespace) { $ownItems.ContainsKey($id) } else { $items.ContainsKey($id) }
    Require $valid "Missing item: $id"
}
function CheckAspects($list) { foreach ($aspect in $list) { Require ($aspects.ContainsKey($aspect.aspect)) "Missing aspect: $($aspect.aspect)"; if ($aspect.ContainsKey('amount')) { Require ($aspect.amount -gt 0) 'Invalid aspect amount' } } }
function CheckIngredient($value, $optional = @()) {
    if ($value -is [System.Collections.IDictionary]) {
        if ($value['item']) { CheckItem $value['item'] $optional }
        if ($value.tag) { Require ($tags.ContainsKey($value.tag)) "Missing tag: $($value.tag)" }
        foreach ($child in $value.Values) { CheckIngredient $child $optional }
    } elseif ($value -is [array]) { foreach ($child in $value) { CheckIngredient $child $optional } }
}
foreach ($id in $entries.Keys) {
    $entry = $entries[$id]
    Require ($categories.ContainsKey($entry.category)) "Missing category: $id"
    CheckText $entry.name
    Require ($entry.stages.Count -gt 0) "Missing stages: $id"
    foreach ($parent in $entry.parents) { CheckResearch $parent }
    foreach ($icon in $entry.icons) {
        if ($icon.StartsWith('focus:')) { continue }
        if ($icon.EndsWith('.png')) { $resource = 'assets/' + $icon.Replace(':', '/'); Require ($resources.ContainsKey($resource) -or $external.ContainsKey($resource)) "Missing icon: $icon" } else { CheckItem $icon }
    }
    CheckAspects $entry.note_aspects
    if (@($entry.stages | Where-Object { $_.recipes.Count -gt 0 }).Count) {
        Require ($entry.complexity -ge 2 -and $entry.complexity -le 8 -and $entry.note_aspects.Count -ge 3) "Invalid research note: $id"
        Require ($entry.stages.Count -ge 2 -and !$entry.stages[0].recipes.Count) "Recipe precedes note: $id"
        $theory = @($entry.stages[0].required_knowledge | Where-Object type -eq theory)
        Require ($theory.Count -eq 1 -and $theory[0].amount -eq 1) "Missing note gate: $id"
    }
    foreach ($stage in $entry.stages) {
        CheckText $stage.text
        foreach ($reference in $stage.required_research) { CheckResearch $reference }
        foreach ($knowledge in @($stage.required_knowledge) + @($stage.knowledge)) { if ($knowledge) { Require ($categories.ContainsKey($knowledge.category)) 'Missing knowledge category' } }
        foreach ($requirement in @($stage.obtain) + @($stage.craft)) { if (!$requirement) { continue }; foreach ($item in $requirement['items']) { if ($item.StartsWith('#')) { Require ($tags.ContainsKey($item.Substring(1))) "Missing tag: $item" } else { CheckItem $item } } }
        foreach ($recipe in $stage.recipes) { Require ($recipes.ContainsKey($recipe) -and $recipes[$recipe].research.entry -eq $id) "Invalid recipe reference: $recipe" }
    }
}
foreach ($recipe in $recipes.Values) {
    $optional = @($recipe['neoforge:conditions'] | Where-Object type -eq 'neoforge:mod_loaded' | ForEach-Object modid)
    if ($recipe.research.entry) { CheckResearch $recipe.research.entry }
    if ($recipe.type -eq 'thaumaturge:infusion') { Require ([bool]$recipe.research.entry) 'Infusion bypasses research' }
    CheckIngredient $recipe $optional
    CheckAspects $recipe.aspects
    if ($recipe.result.id) { CheckItem $recipe.result.id }
}
foreach ($resource in @($entries["${namespace}:omega_core"], $recipes["${namespace}:infusion/omega_core"])) {
    Require (@($resource['neoforge:conditions'] | Where-Object { $_.type -eq 'neoforge:mod_loaded' -and $_.modid -eq 'avaritia' }).Count -gt 0) 'Missing Omega Core mod condition'
}
$baseAspects = ReadJson (Join-Path $root 'src/main/resources/data/thaumaturge/data_maps/item/base_aspects.json')
foreach ($id in $baseAspects['values'].Keys) { if (!$id.StartsWith('#')) { CheckItem $id }; CheckAspects $baseAspects['values'][$id] }
$visiting = @{}
$visited = @{}
function Visit($id) {
    Require (!$visiting.ContainsKey($id)) "Research cycle: $id"
    if ($visited.ContainsKey($id)) { return }
    $visiting[$id] = $true
    foreach ($parent in $entries[$id].parents) { $target = $parent.TrimStart('~').Split('@')[0]; if ($entries.ContainsKey($target)) { Visit $target } }
    $visiting.Remove($id)
    $visited[$id] = $true
}
foreach ($id in $entries.Keys) { Visit $id }
$layout = @($entries.Keys)
for ($index = 0; $index -lt $layout.Count; $index++) {
    $id = $layout[$index]
    $node = $entries[$id]
    for ($otherIndex = $index + 1; $otherIndex -lt $layout.Count; $otherIndex++) {
        $other = $entries[$layout[$otherIndex]]
        $distance = [Math]::Pow($node.column - $other.column, 2) + [Math]::Pow($node.row - $other.row, 2)
        Require ($distance -ge 1) "Research nodes overlap: $id and $($layout[$otherIndex])"
    }
    foreach ($reference in $node.parents) {
        if ($reference.StartsWith('~')) { continue }
        $parentId = $reference.Split('@')[0]
        if (!$entries.ContainsKey($parentId)) { continue }
        $parent = $entries[$parentId]
        foreach ($otherId in $layout) {
            if ($otherId -eq $id -or $otherId -eq $parentId) { continue }
            $other = $entries[$otherId]
            $vertical = $other.column -eq $node.column -and
                $other.row -ge [Math]::Min($node.row, $parent.row) -and
                $other.row -le [Math]::Max($node.row, $parent.row)
            $horizontal = $other.row -eq $parent.row -and
                $other.column -ge [Math]::Min($node.column, $parent.column) -and
                $other.column -le [Math]::Max($node.column, $parent.column)
            Require (!$vertical -and !$horizontal) "Research connector $parentId -> $id passes through $otherId"
        }
    }
}
$release = @(Get-ChildItem (Join-Path $root 'build/libs') -Filter "$namespace-*.jar" | Where-Object Name -NotLike '*-sources.jar')
Require ($release.Count -eq 1) 'Expected one release JAR'
$archive = [IO.Compression.ZipFile]::OpenRead($release[0].FullName)
try {
    $packed = @{}
    foreach ($entry in $archive.Entries.FullName) { Require (!$packed.ContainsKey($entry)) "Duplicate JAR resource: $entry"; Require (!$entry.StartsWith('.cache/')) 'Datagen cache in JAR'; $packed[$entry] = $true }
    foreach ($resource in $resources.Keys) { Require ($packed.ContainsKey($resource)) "Missing packaged resource: $resource" }
} finally { $archive.Dispose() }
Write-Output "Verified $($entries.Count) research entries, $($recipes.Count) recipes and JAR resources."
