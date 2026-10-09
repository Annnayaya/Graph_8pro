#requires -Version 7.0
<#
.SYNOPSIS
    生成 Graph 标准版 ZIP: Graph_8_2.2.8.zip
#>
[CmdletBinding()]
param(
    [string]$VersionTag = '2.2.8',
    [string]$OutputDir = ''
)

$script = Join-Path $PSScriptRoot "Package-GraphApp.ps1"
& pwsh -NoProfile -ExecutionPolicy Bypass -File $script -Mode Standard -VersionTag $VersionTag -OutputDir $OutputDir
