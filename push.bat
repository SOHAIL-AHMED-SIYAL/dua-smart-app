@echo off
setlocal
rem ================================================================
rem  DUA SMART APP  -  One-click Git push script
rem ================================================================
rem 1. Edit USERNAME and TOKEN below with your GitHub credentials.
rem    TOKEN = a Personal Access Token (repo scope) from GitHub.
rem 2. Run this script:  push.bat
rem ================================================================

set "USERNAME=YOUR_GITHUB_USERNAME"
set "TOKEN=YOUR_GITHUB_TOKEN"
set "REPO=dua-smart-app"

if "%USERNAME%"=="YOUR_GITHUB_USERNAME" (
    echo.
    echo ERROR: Please edit push.bat and set YOUR_GITHUB_USERNAME and YOUR_GITHUB_TOKEN.
    echo.
    pause
    exit /b 1
)

echo Initializing git repository...
git init
git add .
git commit -m "Initial release: DUA SMART APP - attendance, fees, offline DB backup" >nul 2>&1 || echo "Nothing new to commit."

echo Adding remote...
git remote remove origin 2>nul
git remote add origin "https://%USERNAME%:%TOKEN%@github.com/%USERNAME%/%REPO%.git"

echo Pushing to main...
git branch -M main
git push -u origin main --force

echo.
echo ================================================================
echo Pushed successfully to https://github.com/%USERNAME%/%REPO%
echo.
echo Next:
echo   1. Open the repository on GitHub.
echo   2. Go to the "Actions" tab - the "Build Debug APK" workflow
echo      runs automatically on every push to main.
echo   3. When it completes, download app-debug.apk from the
echo      workflow run's "Artifacts" section.
echo ================================================================
pause
