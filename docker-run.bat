@echo off
REM =========================================================================
REM Online Examination System - Docker Launcher for Windows
REM =========================================================================
echo =========================================================================
echo  Starting Online Examination System in Docker (Windows Compatible)
echo =========================================================================
echo.

docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Docker is not detected or not running.
    echo Please make sure Docker Desktop for Windows is installed and running.
    pause
    exit /b 1
)

echo [INFO] Building and starting containers (MySQL 8 + Java 21 App)...
docker compose up --build -d

if %errorlevel% neq 0 (
    echo [ERROR] Failed to start Docker containers.
    pause
    exit /b %errorlevel%
)

echo.
echo =========================================================================
echo [SUCCESS] Containers started successfully!
echo.
echo  - Web Application: http://localhost:8080
echo  - Admin Portal:    http://localhost:8080/login/admin (admin@exam.com / admin123)
echo  - Teacher Portal:  http://localhost:8080/login/teacher (sarah.teacher@exam.com / teacher123)
echo  - Student Portal:  http://localhost:8080/login/student (alice.student@exam.com / student123)
echo.
echo To view live container logs, run:
echo   docker compose logs -f app
echo.
echo To stop containers, run:
echo   docker compose down
echo =========================================================================
pause
