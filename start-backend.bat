@echo off
echo ========================================================
echo Starting VyaparGuard Spring Boot Backend (Port 8080)
echo ========================================================
cd /d "%~dp0\backend"
set "JAVA_HOME=%~dp0\.tools\jdk17.0.20_12"
set "PATH=%JAVA_HOME%\bin;%~dp0\.tools\apache-maven-3.9.6\bin;%PATH%"
"%~dp0\.tools\apache-maven-3.9.6\bin\mvn.cmd" spring-boot:run
pause
