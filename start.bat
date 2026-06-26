@echo off
REM 开放实验室预约系统 · 一键启动 (Windows 双击或 cmd 运行)
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0start.ps1" %*
pause

