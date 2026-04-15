@echo off
title QuantumLibrary Backend Server
color 0A

echo.
echo  ████████████████████████████████████████████████████████████
echo  ██                                                        ██
echo  ██        📚  QuantumLibrary Backend Launcher             ██
echo  ██                                                        ██
echo  ████████████████████████████████████████████████████████████
echo.

:: ── Set paths ──────────────────────────────────────────────────
set "SCRIPT_DIR=%~dp0"
set "BACKEND_DIR=%SCRIPT_DIR%backend"
set "JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.18.8-hotspot"
set "MVN=%BACKEND_DIR%\apache-maven-3.9.6\bin\mvn.cmd"

:: ── Check Java 17 ──────────────────────────────────────────────
echo  [1/3] Checking Java 17...
if not exist "%JAVA_HOME%\bin\java.exe" (
    echo.
    echo  ERROR: Java 17 not found at: %JAVA_HOME%
    echo  Please install Java 17 from: https://adoptium.net/
    echo.
    pause
    exit /b 1
)
echo         ✅ Found Java 17

:: ── Check Maven ────────────────────────────────────────────────
echo  [2/3] Checking Maven...
if not exist "%MVN%" (
    echo.
    echo  ERROR: Maven not found at: %MVN%
    echo  Run this from the project folder containing start-backend.bat
    echo.
    pause
    exit /b 1
)
echo         ✅ Found Maven 3.9.6

:: ── Start Server ───────────────────────────────────────────────
echo  [3/3] Starting Spring Boot server...
echo.
echo  ┌──────────────────────────────────────────────────────┐
echo  │  🌐  API Base   :  http://localhost:8080             │
echo  │  🗄️   H2 Console :  http://localhost:8080/h2-console  │
echo  ├──────────────────────────────────────────────────────┤
echo  │  👤  Admin  :  admin@quantumlibrary.com              │
echo  │  🔑  Pass   :  admin123                              │
echo  ├──────────────────────────────────────────────────────┤
echo  │  👤  Member :  hussain0706w@gmail.com                │
echo  │  🔑  Pass   :  member123                             │
echo  └──────────────────────────────────────────────────────┘
echo.
echo  Press Ctrl+C to stop the server.
echo  ─────────────────────────────────────────────────────────
echo.

cd /d "%BACKEND_DIR%"
"%MVN%" spring-boot:run

echo.
echo  Server stopped.
pause
