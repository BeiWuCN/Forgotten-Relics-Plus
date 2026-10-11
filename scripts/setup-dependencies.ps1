param([string]$Source, [string]$BaseUrl = $env:DEPS_BASE_URL, [switch]$Optional)
$ErrorActionPreference = 'Stop'
$libs = Join-Path (Split-Path $PSScriptRoot) 'libs'
$lock = Get-Content (Join-Path $libs 'dependencies.lock.json') -Raw | ConvertFrom-Json
foreach ($dependency in $lock.dependencies) {
    $file = Join-Path $libs $dependency.file
    if (!(Test-Path -LiteralPath $file)) {
        if ($dependency.optional -and !$Optional) { continue }
        $temporary = "$file.download"
        try {
            $local = if ($Source) { Join-Path $Source $dependency.file } else { $null }
            if ($local -and (Test-Path -LiteralPath $local)) {
                Copy-Item -LiteralPath $local -Destination $temporary
            } else {
                $url = $dependency.url
                if (!$url -and $BaseUrl) { $url = $BaseUrl.TrimEnd('/') + '/' + [Uri]::EscapeDataString($dependency.file) }
                if (!$url) { throw "No download source for $($dependency.file). Use -Source or DEPS_BASE_URL." }
                Invoke-WebRequest -Uri $url -OutFile $temporary -TimeoutSec 120
            }
            if ((Get-FileHash -LiteralPath $temporary -Algorithm SHA256).Hash.ToLowerInvariant() -ne $dependency.sha256) {
                throw "Checksum mismatch: $($dependency.file)"
            }
            Move-Item -LiteralPath $temporary -Destination $file
        } finally {
            if (Test-Path -LiteralPath $temporary) { Remove-Item -LiteralPath $temporary }
        }
    }
    if ((Get-FileHash -LiteralPath $file -Algorithm SHA256).Hash.ToLowerInvariant() -ne $dependency.sha256) {
        throw "Checksum mismatch: $($dependency.file)"
    }
}
Write-Output 'Locked dependencies verified.'
