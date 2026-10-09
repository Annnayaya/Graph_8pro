#requires -Version 7.0
<#
.SYNOPSIS
    生成 Graph Pro版 ZIP: Graph_8Pro2.2.8.zip
#>
[CmdletBinding()]
param(
    [string]$VersionTag = '2.2.8',
    [string]$OutputDir = ''
)

$script = Join-Path $PSScriptRoot "Package-GraphApp.ps1"
& pwsh -NoProfile -ExecutionPolicy Bypass -File $script -Mode Pro -VersionTag $VersionTag -OutputDir $OutputDir
