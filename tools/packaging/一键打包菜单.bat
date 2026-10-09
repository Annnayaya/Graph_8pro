@echo off
chcp 65001 >nul
title Graph 8Pro 自动化打包工具 (PowerShell 7)
pwsh -NoProfile -ExecutionPolicy Bypass -File "%~dp0Package-GraphApp.ps1"
echo.
pause
