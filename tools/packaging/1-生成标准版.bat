@echo off
chcp 65001 >nul
title 生成 Graph 标准版 (Graph_8_2.2.8.zip)
pwsh -NoProfile -ExecutionPolicy Bypass -File "%~dp0Package-GraphApp.ps1" -Mode Standard
echo.
pause
