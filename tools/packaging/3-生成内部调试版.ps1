#requires -Version 7.0
<#
.SYNOPSIS
    生成 Graph 内部调试使用版 ZIP: Graph_8内部调试版2.2.8.zip
#>
[CmdletBinding()]
param(
    [string]$VersionTag = '2.2.8',
    [string]$OutputDir = ''
)

$script = Join-Path $PSScriptRoot "Package-GraphApp.ps1"
& pwsh -NoProfile -ExecutionPolicy Bypass -File $script -Mode Debug -VersionTag $VersionTag -OutputDir $OutputDir
