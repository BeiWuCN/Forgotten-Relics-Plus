$ErrorActionPreference = 'Stop'
$tag = $env:RELEASE_TAG
if ($tag -notmatch '^v\d+\.\d+\.\d+(?:[.-][A-Za-z0-9.-]+)?$') { throw 'Expected a release tag such as v1.7.1' }
$repository = if ($env:GITHUB_REPOSITORY) { $env:GITHUB_REPOSITORY } else { 'BeiWuCN/Forgotten-Relics-Plus' }
$release = gh release view $tag --repo $repository --json assets,body,name,isDraft,isPrerelease | ConvertFrom-Json
if ($LASTEXITCODE -ne 0 -or $release.isDraft) { throw 'Only published releases can be synced' }
$version = $tag.Substring(1)
$filename = "forgotten_relics_plus-1.21.1-neoforge-$version.jar"
$assets = @($release.assets | Where-Object name -eq $filename)
if ($assets.Count -ne 1 -or $assets[0].state -ne 'uploaded') { throw 'Expected one uploaded release mod JAR' }
$destination = 'build/curseforge'
New-Item -ItemType Directory -Path $destination -Force | Out-Null
gh release download $tag --repo $repository --pattern $filename --dir $destination --clobber
if ($LASTEXITCODE -ne 0) { throw 'Release download failed' }
$artifact = "$destination/$filename"
$digest = 'sha256:' + (Get-FileHash -LiteralPath $artifact).Hash.ToLowerInvariant()
if ($assets[0].digest -ne $digest) { throw 'Release artifact failed SHA-256 validation' }
Add-Type -AssemblyName System.IO.Compression.FileSystem
$archive = [IO.Compression.ZipFile]::OpenRead((Join-Path (Get-Location) $artifact))
try {
    $reader = [IO.StreamReader]::new($archive.GetEntry('META-INF/neoforge.mods.toml').Open())
    try { $metadata = $reader.ReadToEnd() } finally { $reader.Dispose() }
    if ($metadata -notmatch ('(?m)^version\s*=\s*"' + [regex]::Escape($version) + '"') -or
        $metadata -notmatch '(?m)^modId\s*=\s*"forgotten_relics_plus"') { throw 'Release metadata mismatch' }
} finally { $archive.Dispose() }
[IO.File]::WriteAllText((Join-Path (Get-Location) "$destination/changelog.md"), $release.body)
$outputs = @{ file = $artifact; name = $release.name; version = $version; type = $(if ($release.isPrerelease) { 'beta' } else { 'release' }) }
foreach ($key in $outputs.Keys) {
    if ($outputs[$key] -match '[\r\n]') { throw 'Invalid multiline release metadata' }
    Add-Content -LiteralPath $env:GITHUB_OUTPUT -Value "$key=$($outputs[$key])" -Encoding utf8
}
Write-Output "Validated $filename; ready to sync to CurseForge"
