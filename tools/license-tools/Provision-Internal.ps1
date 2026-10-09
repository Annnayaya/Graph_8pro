#requires -Version 5.1
param([string]$Java='E:\000_JavaFX\Jdk\JDK21\bin\java.exe')
$ErrorActionPreference='Stop'
$root=$PSScriptRoot;$private=Join-Path $root 'private'
New-Item -ItemType Directory -Path $private -Force | Out-Null
$acl=[Security.AccessControl.DirectorySecurity]::new();$acl.SetAccessRuleProtection($true,$false)
foreach($sid in @([Security.Principal.WindowsIdentity]::GetCurrent().User.Value,'S-1-5-18')){
    $acl.AddAccessRule([Security.AccessControl.FileSystemAccessRule]::new([Security.Principal.SecurityIdentifier]::new($sid),'FullControl','ContainerInherit,ObjectInherit','None','Allow'))
}
Set-Acl -LiteralPath $private -AclObject $acl
$passwordFile=Join-Path $private 'initial-unlock-password.txt';$vault=Join-Path $private 'issuer.vault'
if(Test-Path -LiteralPath $vault){throw 'Existing company vault preserved; do not regenerate automatically.'}
if(Test-Path -LiteralPath $passwordFile){throw 'Existing unlock password preserved; inspect before provisioning.'}
$bytes=New-Object byte[] 24;$rng=[Security.Cryptography.RandomNumberGenerator]::Create()
try{$rng.GetBytes($bytes)}finally{$rng.Dispose()}
$password=[Convert]::ToBase64String($bytes);[Array]::Clear($bytes,0,$bytes.Length)
[IO.File]::WriteAllText($passwordFile,$password,[Text.UTF8Encoding]::new($false));$password=$null
$source=[IO.Path]::GetFullPath((Join-Path $root '..\..\src\main\java\com\zust\monitor\SmartStationMonitorApplication.java'))
$issuer=Join-Path $root 'dist\internal-windows-x64\PMB-LicenseIssuer\app\PMB-LicenseIssuer.jar'
& $Java -jar $issuer --init-source $source --vault $vault --password-file $passwordFile
if($LASTEXITCODE){throw 'Company vault initialization failed; private material retained for inspection.'}
foreach($relative in @('dist\internal-windows-x64\PMB-LicenseIssuer\issuer.vault','dist\internal-linux-x64\PMB-LicenseIssuer\issuer.vault')){
    $destination=Join-Path $root $relative;if(Test-Path -LiteralPath $destination){throw 'Internal output already contains a vault.'}
    Copy-Item -LiteralPath $vault -Destination $destination
    $autoPassword=Join-Path ([IO.Path]::GetDirectoryName($destination)) 'password.txt'
    if(Test-Path -LiteralPath $autoPassword){throw 'Existing root password file preserved.'}
    Copy-Item -LiteralPath $passwordFile -Destination $autoPassword
}
Write-Output 'Internal editions contain password.txt and issuer.vault in their software root for automatic loading. Customer editions contain neither.'
