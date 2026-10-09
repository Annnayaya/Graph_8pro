@echo off
chcp 65001 >nul
title 生成 Graph 内部调试使用版 (Graph_8内部调试版2.2.8.zip)
pwsh -NoProfile -ExecutionPolicy Bypass -File "%~dp0Package-GraphApp.ps1" -Mode Debug
echo.
pause
