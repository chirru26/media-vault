$ErrorActionPreference = 'Stop'

mvn clean package dependency:copy-dependencies -DincludeScope=runtime -DoutputDirectory=target/lib

$version = '0.1.0-SNAPSHOT'
$input = Join-Path (Get-Location) 'target'
$output = Join-Path (Get-Location) 'target/installer'
New-Item -ItemType Directory -Force $output | Out-Null

jpackage `
  --type app-image `
  --name MediaVault `
  --input $input `
  --main-jar "media-vault-$version.jar" `
  --main-class com.chirru26.mediavault.MediaVaultApplication `
  --dest $output `
  --app-version 0.1.0

Write-Host "MediaVault desktop image created in $output"
