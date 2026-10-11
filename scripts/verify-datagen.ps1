$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot
function Snapshot {
    @(Get-ChildItem (Join-Path $root 'src/generated/resources') -File -Recurse |
        Where-Object FullName -NotMatch '[\\/]\.cache[\\/]' |
        Sort-Object FullName | ForEach-Object {
            $_.FullName.Substring($root.Length) + ':' + (Get-FileHash -LiteralPath $_.FullName).Hash
        }) -join "`n"
}
$before = Snapshot
Push-Location $root
try {
    if ($IsWindows) { & .\gradlew.bat runData --console=plain } else { & ./gradlew runData --console=plain }
    if ($LASTEXITCODE -ne 0) { throw 'Datagen failed' }
    if ((Snapshot) -cne $before) { throw 'Generated resources changed. Review and commit runData output.' }
} finally { Pop-Location }
Write-Output 'Datagen output matches the committed resource snapshot.'
