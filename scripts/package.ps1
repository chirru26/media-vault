$ErrorActionPreference = 'Stop'

mvn clean package dependency:copy-dependencies -DincludeScope=runtime -DoutputDirectory=target/lib

$version = '0.1.0-SNAPSHOT'
$stage = Join-Path (Get-Location) 'target/jpackage-input'
$output = Join-Path (Get-Location) 'target/installer'
Remove-Item $stage -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force $stage | Out-Null
New-Item -ItemType Directory -Force $output | Out-Null
Copy-Item "target/media-vault-$version.jar" $stage
Copy-Item 'target/lib/*.jar' $stage

jpackage `
  --type app-image `
  --name MediaVault `
  --input $stage `
  --main-jar "media-vault-$version.jar" `
  --main-class com.chirru26.mediavault.MediaVaultApplication `
  --dest $output `
  --app-version 0.1.0

Write-Host "MediaVault desktop image created in $output"
