@echo off
chcp 65001 >nul
title 生成 Graph Pro版 (Graph_8Pro2.2.8.zip)
pwsh -NoProfile -ExecutionPolicy Bypass -File "%~dp0Package-GraphApp.ps1" -Mode Pro
echo.
pause
