@echo off
REM 开放实验室预约系统 · 停止服务 (Windows 双击或 cmd 运行)；加 -Wipe 清空数据卷
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0stop.ps1" %*
pause

