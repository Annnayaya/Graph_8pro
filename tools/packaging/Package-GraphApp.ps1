#requires -Version 7.0
<#
.SYNOPSIS
    Graph 8Pro 自动化打包脚本 (PowerShell 7 版)

.DESCRIPTION
    支持3种打包模式：
    1. 标准版：忽略 \GraphConfigManager.exe、\lib\license.prop、\历史版本、生成授权文件(仅内部使用)、获取授权文件(客户使用)
       输出名称：Graph_8_<版本号>.zip (默认: Graph_8_2.2.8.zip)
    2. Pro版：忽略 \lib\license.prop、\历史版本、生成授权文件(仅内部使用)
       输出名称：Graph_8Pro<版本号>.zip (默认: Graph_8Pro2.2.8.zip)
    3. 内部调试使用版：忽略 \历史版本、生成授权文件(仅内部使用)、获取授权文件(客户使用)
       输出名称：Graph_8内部调试版<版本号>.zip (默认: Graph_8内部调试版2.2.8.zip)

.PARAMETER Mode
    打包模式: Standard (1), Pro (2), Debug (3), All (4)

.PARAMETER VersionTag
    版本号，默认: 2.2.8

.PARAMETER SourceDir
    打包源目录，默认为当前目录下的 "Graph8Pro_Lic"

.PARAMETER OutputDir
    Zip 压缩包输出目录，默认为脚本所在工作台目录

.PARAMETER CompressionLevel
    压缩级别: Optimal (默认推荐), Fastest (最快), NoCompression (仅存储)
#>

[CmdletBinding()]
param(
    [Parameter(Position = 0)]
    [ValidateSet('Standard', 'Pro', 'Debug', 'All', '1', '2', '3', '4', '')]
    [string]$Mode = '',

    [Parameter()]
    [string]$VersionTag = '2.2.8',

    [Parameter()]
    [string]$SourceDir = '',

    [Parameter()]
    [string]$OutputDir = '',

    [Parameter()]
    [ValidateSet('Optimal', 'Fastest', 'NoCompression')]
    [string]$CompressionLevel = 'Optimal'
)

# 确保控制台使用 UTF-8 输出
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

# 1. 确定源目录（优先取当前目录下的 Graph8Pro_Lic，100% 相对路径自适应）
if (-not $SourceDir) {
    $candidates = @('Graph8Pro_Lic', 'Graph8ProLic(完整功能)')
    foreach ($candName in $candidates) {
        $cand = Join-Path $PSScriptRoot $candName
        if (Test-Path -LiteralPath $cand -PathType Container) {
            $SourceDir = (Get-Item -LiteralPath $cand).FullName
            break
        }
    }
    if (-not $SourceDir) {
        if (Test-Path -LiteralPath (Join-Path $PSScriptRoot 'Graph_8Pro.exe')) {
            $SourceDir = $PSScriptRoot
        } else {
            $SourceDir = Join-Path $PSScriptRoot 'Graph8Pro_Lic'
        }
    }
}

if (-not (Test-Path -LiteralPath $SourceDir -PathType Container)) {
    Write-Error "打包源目录不存在: $SourceDir (请确保 'Graph8Pro_Lic' 文件夹与脚本在同一目录下，或通过 -SourceDir 参数指定)"
    exit 1
}

# 2. 确定输出目录（默认输出到当前工作台目录）
if (-not $OutputDir) {
    if ($SourceDir -eq $PSScriptRoot) {
        $OutputDir = Split-Path -Parent $SourceDir
    } else {
        $OutputDir = $PSScriptRoot
    }
}

if (-not (Test-Path -LiteralPath $OutputDir)) {
    $null = New-Item -ItemType Directory -Path $OutputDir -Force
}

# 3. 三种模式配置定义
$ModeConfigs = @{
    'Standard' = @{
        Id             = 1
        Alias          = '1'
        Name           = '标准版'
        FileNameFormat = 'Graph_8_{0}.zip'
        Description    = '忽略: \GraphConfigManager.exe, \lib\license.prop, \历史版本, 生成授权文件(内部), 获取授权文件(客户)'
        Exclusions     = @(
            'GraphConfigManager.exe',
            'lib\license.prop',
            '历史版本',
            '生成授权文件(仅内部使用)',
            '获取授权文件(客户使用)'
        )
    }
    'Pro' = @{
        Id             = 2
        Alias          = '2'
        Name           = 'Pro版'
        FileNameFormat = 'Graph_8Pro{0}.zip'
        Description    = '忽略: \lib\license.prop, \历史版本, 生成授权文件(内部) (包含客户授权工具与配置器)'
        Exclusions     = @(
            'lib\license.prop',
            '历史版本',
            '生成授权文件(仅内部使用)'
        )
    }
    'Debug' = @{
        Id             = 3
        Alias          = '3'
        Name           = '内部调试使用版'
        FileNameFormat = 'Graph_8内部调试版{0}.zip'
        Description    = '忽略: \历史版本, 生成授权文件(内部), 获取授权文件(客户) (包含配置器与内置授权)'
        Exclusions     = @(
            '历史版本',
            '生成授权文件(仅内部使用)',
            '获取授权文件(客户使用)'
        )
    }
}

# 4. 判断相对路径是否属于忽略项
function Test-IsExcluded {
    param(
        [string]$RelativePath,
        [string[]]$ExclusionRules
    )
    $normPath = $RelativePath.Replace('\', '/').TrimStart('/')

    # 忽略任何生成的 .zip 及临时文件
    if ($normPath.EndsWith('.zip', [System.StringComparison]::OrdinalIgnoreCase) -or
        $normPath.EndsWith('.tmp', [System.StringComparison]::OrdinalIgnoreCase)) {
        return $true
    }

    # 忽略根目录下的打包脚本与说明文件（不影响 lib 下的批处理文件）
    if ($normPath -in @('Package-GraphApp.ps1', '打包说明.txt') -or
        $normPath -match '^[123]-生成.*\.ps1$' -or
        $normPath -match '^[123]-生成.*\.bat$' -or
        $normPath -match '^.*打包.*\.bat$' -or
        $normPath -match '^.*打包.*\.ps1$') {
        return $true
    }

    # 匹配业务排除规则
    foreach ($rule in $ExclusionRules) {
        $normRule = $rule.Replace('\', '/').TrimStart('/').TrimEnd('/')
        if ($normPath -ieq $normRule) {
            return $true
        }
        if ($normPath.StartsWith("$normRule/", [System.StringComparison]::OrdinalIgnoreCase)) {
            return $true
        }
    }
    return $false
}

# 5. 递归收集待打包的文件 (直接剪枝忽略目录，大幅提速)
function Get-FilesToArchive {
    param(
        [string]$BaseDir,
        [string[]]$ExclusionRules
    )
    $queue = [System.Collections.Generic.Queue[string]]::new()
    $queue.Enqueue($BaseDir)
    $fileList = [System.Collections.Generic.List[System.IO.FileInfo]]::new()

    while ($queue.Count -gt 0) {
        $currentDir = $queue.Dequeue()
        $dirInfo = [System.IO.DirectoryInfo]::new($currentDir)

        # 扫描并加入非忽略子目录
        try {
            foreach ($subDir in $dirInfo.GetDirectories()) {
                $rel = [System.IO.Path]::GetRelativePath($BaseDir, $subDir.FullName)
                if (-not (Test-IsExcluded $rel $ExclusionRules)) {
                    $queue.Enqueue($subDir.FullName)
                }
            }
        } catch {
            Write-Warning "无法访问目录: $currentDir - $($_.Exception.Message)"
        }

        # 扫描并加入非忽略文件
        try {
            foreach ($file in $dirInfo.GetFiles()) {
                $rel = [System.IO.Path]::GetRelativePath($BaseDir, $file.FullName)
                if (-not (Test-IsExcluded $rel $ExclusionRules)) {
                    $fileList.Add($file)
                }
            }
        } catch {
            Write-Warning "无法读取文件列表: $currentDir - $($_.Exception.Message)"
        }
    }

    return $fileList
}

# 6. 执行单模式打包
function Invoke-ZipPackaging {
    param(
        [hashtable]$Config,
        [string]$SourcePath,
        [string]$OutputPath,
        [string]$Version,
        [string]$Compression
    )

    $zipFileName = [string]::Format($Config.FileNameFormat, $Version)
    $zipFilePath = Join-Path $OutputPath $zipFileName

    Write-Host ""
    Write-Host ("=" * 72) -ForegroundColor Cyan
    Write-Host "【打包模式】: $($Config.Name)" -ForegroundColor Yellow
    Write-Host "【源 目 录】: $SourcePath" -ForegroundColor DarkGray
    Write-Host "【输出文件】: $zipFilePath" -ForegroundColor Gray
    Write-Host "【忽略规则】: $($Config.Description)" -ForegroundColor DarkGray
    Write-Host ("-" * 72) -ForegroundColor DarkCyan

    $sw = [System.Diagnostics.Stopwatch]::StartNew()

    Write-Host "正在扫描文件目录..." -NoNewline
    $files = Get-FilesToArchive -BaseDir $SourcePath -ExclusionRules $Config.Exclusions
    $totalFiles = $files.Count
    $totalRawSizeMB = [math]::Round(($files | Measure-Object -Property Length -Sum).Sum / 1MB, 2)
    Write-Host " 完成 (待打包文件: $totalFiles 个, 未压缩体积: $totalRawSizeMB MB)" -ForegroundColor Green

    # 若目标 zip 已存在，先删除旧文件
    if (Test-Path -LiteralPath $zipFilePath) {
        Write-Host "检测到已存在同名 Zip，正在覆盖..." -ForegroundColor Yellow
        Remove-Item -LiteralPath $zipFilePath -Force
    }

    Write-Host "正在生成 ZIP 压缩包 (UTF-8 字符集)..." -ForegroundColor Cyan

    $zipStream = [System.IO.File]::Create($zipFilePath)
    $archive = [System.IO.Compression.ZipArchive]::new(
        $zipStream,
        [System.IO.Compression.ZipArchiveMode]::Create,
        $false,
        [System.Text.Encoding]::UTF8
    )

    $compLevel = [System.IO.Compression.CompressionLevel]::$Compression

    $counter = 0
    foreach ($file in $files) {
        $counter++
        $relPath = [System.IO.Path]::GetRelativePath($SourcePath, $file.FullName).Replace('\', '/')
        
        $percent = [math]::Round(($counter / $totalFiles) * 100)
        if ($counter % 25 -eq 0 -or $counter -eq $totalFiles) {
            Write-Progress -Activity "正在打包 [$($Config.Name)]" -Status "进度: $counter / $totalFiles ($percent%)" -PercentComplete $percent
        }

        $null = [System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile($archive, $file.FullName, $relPath, $compLevel)
    }

    Write-Progress -Activity "正在打包 [$($Config.Name)]" -Completed
    $archive.Dispose()
    $zipStream.Dispose()
    $sw.Stop()

    $finalZipSizeMB = [math]::Round((Get-Item -LiteralPath $zipFilePath).Length / 1MB, 2)
    $elapsedSec = [math]::Round($sw.ElapsedMilliseconds / 1000, 2)

    Write-Host ">>> 打包成功!" -ForegroundColor Green
    Write-Host "    - 文件名: $zipFileName" -ForegroundColor White
    Write-Host "    - 文件数: $totalFiles 个" -ForegroundColor Gray
    Write-Host "    - 压缩后: $finalZipSizeMB MB (原大小: $totalRawSizeMB MB)" -ForegroundColor Gray
    Write-Host "    - 耗时:   $elapsedSec 秒" -ForegroundColor Gray
    Write-Host ("=" * 72) -ForegroundColor Cyan

    return [PSCustomObject]@{
        Mode        = $Config.Name
        ZipFile     = $zipFileName
        ZipPath     = $zipFilePath
        FileCount   = $totalFiles
        SizeMB      = $finalZipSizeMB
        ElapsedSec  = $elapsedSec
    }
}

# 7. 交互式菜单逻辑 (当未指定 Mode 参数时进入)
if (-not $Mode) {
    Clear-Host
    Write-Host "==================================================================" -ForegroundColor Cyan
    Write-Host "            Graph 8Pro 自动化打包工具 (PowerShell 7)              " -ForegroundColor Yellow
    Write-Host "==================================================================" -ForegroundColor Cyan
    Write-Host "工作台目录: $PSScriptRoot" -ForegroundColor Gray
    Write-Host "源  目  录: $SourceDir" -ForegroundColor Gray
    Write-Host "输 出 目录: $OutputDir" -ForegroundColor Gray
    Write-Host "默 认 版本: $VersionTag" -ForegroundColor Magenta
    Write-Host "------------------------------------------------------------------" -ForegroundColor DarkCyan
    Write-Host " [1] 生成 标准版 ZIP       -> Graph_8_${VersionTag}.zip" -ForegroundColor White
    Write-Host "     - 忽略: GraphConfigManager.exe, license.prop, 历史版本, 内部授权, 客户授权" -ForegroundColor DarkGray
    Write-Host " [2] 生成 Pro版 ZIP        -> Graph_8Pro${VersionTag}.zip" -ForegroundColor White
    Write-Host "     - 忽略: license.prop, 历史版本, 内部授权 (包含客户授权工具与配置器)" -ForegroundColor DarkGray
    Write-Host " [3] 生成 内部调试使用版   -> Graph_8内部调试版${VersionTag}.zip" -ForegroundColor White
    Write-Host "     - 忽略: 历史版本, 内部授权, 客户授权 (包含配置器与内置授权)" -ForegroundColor DarkGray
    Write-Host " [4] 一键生成 全部3个版本" -ForegroundColor Green
    Write-Host " [Q] 退出" -ForegroundColor Red
    Write-Host "------------------------------------------------------------------" -ForegroundColor DarkCyan

    $choice = Read-Host "请选择打包模式 [1/2/3/4/Q] (直接回车默认 1)"
    if (-not $choice) { $choice = '1' }

    if ($choice -match '^[Qq退出]$') {
        Write-Host "已取消操作。" -ForegroundColor Yellow
        exit 0
    }

    $Mode = $choice

    # 询问是否自定义版本号
    $customTag = Read-Host "请输入版本号 (直接回车默认: $VersionTag)"
    if ($customTag.Trim()) {
        $VersionTag = $customTag.Trim()
    }
}

# 8. 执行打包任务
$modesToRun = @()
switch ($Mode) {
    { $_ -in 'Standard', '1' } { $modesToRun += $ModeConfigs['Standard'] }
    { $_ -in 'Pro', '2' }      { $modesToRun += $ModeConfigs['Pro'] }
    { $_ -in 'Debug', '3' }    { $modesToRun += $ModeConfigs['Debug'] }
    { $_ -in 'All', '4' }      {
        $modesToRun += $ModeConfigs['Standard']
        $modesToRun += $ModeConfigs['Pro']
        $modesToRun += $ModeConfigs['Debug']
    }
    Default {
        Write-Error "无效的模式: $Mode. 有效值为 Standard(1), Pro(2), Debug(3), All(4)"
        exit 1
    }
}

$results = [System.Collections.Generic.List[PSObject]]::new()
$overallSw = [System.Diagnostics.Stopwatch]::StartNew()

foreach ($cfg in $modesToRun) {
    $res = Invoke-ZipPackaging -Config $cfg -SourcePath $SourceDir -OutputPath $OutputDir -Version $VersionTag -Compression $CompressionLevel
    $results.Add($res)
}

$overallSw.Stop()

# 9. 输出汇总报表
Write-Host ""
Write-Host "=========================== 打包汇总报表 ===========================" -ForegroundColor Green
$results | Format-Table -Property @(
    @{Label="打包模式"; Expression={$_.Mode}; Width=16},
    @{Label="生成文件名"; Expression={$_.ZipFile}; Width=34},
    @{Label="文件数"; Expression={$_.FileCount}; Width=8},
    @{Label="体积(MB)"; Expression={$_.SizeMB}; Width=10},
    @{Label="耗时(秒)"; Expression={$_.ElapsedSec}; Width=8}
)
Write-Host "全部完成! 总耗时: $([math]::Round($overallSw.ElapsedMilliseconds / 1000, 2)) 秒" -ForegroundColor Cyan
Write-Host "输出保存路径: $OutputDir" -ForegroundColor White
Write-Host "====================================================================" -ForegroundColor Green
