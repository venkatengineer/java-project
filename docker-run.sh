#!/usr/bin/env bash
# =========================================================================
# Online Examination System - Docker Launcher
# =========================================================================
set -e

echo "========================================================================="
echo " Starting Online Examination System in Docker"
echo "========================================================================="

if ! command -v docker &> /dev/null; then
    echo "[ERROR] Docker is not installed or not in PATH."
    exit 1
fi

echo "[INFO] Building and starting containers (MySQL 8 + Java 21 App)..."
docker compose up --build -d

echo ""
echo "========================================================================="
echo "[SUCCESS] Containers started successfully!"
echo ""
echo " - Web Application: http://localhost:8080"
echo " - Admin Portal:    http://localhost:8080/login/admin (admin@exam.com / admin123)"
echo " - Teacher Portal:  http://localhost:8080/login/teacher (sarah.teacher@exam.com / teacher123)"
echo " - Student Portal:  http://localhost:8080/login/student (alice.student@exam.com / student123)"
echo ""
echo "To view live logs: docker compose logs -f app"
echo "To stop:           docker compose down"
echo "========================================================================="
