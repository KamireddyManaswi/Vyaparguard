@echo off
echo ========================================================
echo Starting VyaparGuard End-to-End System
echo "Know your cash. Act before the shortage."
echo ========================================================
start "VyaparGuard Backend" cmd /k "%~dp0start-backend.bat"
timeout /t 5 /nobreak >nul
start "VyaparGuard Frontend" cmd /k "%~dp0start-frontend.bat"
timeout /t 3 /nobreak >nul
start http://localhost:5173
echo.
echo Both servers started!
echo Frontend: http://localhost:5173
echo Backend API: http://localhost:8080/api/dashboard
echo ========================================================
