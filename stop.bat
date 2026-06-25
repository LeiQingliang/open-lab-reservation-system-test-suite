@echo off
REM 停止服务。 传入 -Wipe 可清空数据卷： stop.bat -Wipe
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0stop.ps1" %*
pause
