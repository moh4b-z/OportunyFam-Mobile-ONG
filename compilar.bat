@echo off
echo ========================================
echo Compilando OportunyFam Mobile App
echo ========================================
cd /d C:\Users\bruno\StudioProjects\OportunyFam-Mobile-ONGagr
call gradlew.bat assembleDebug --no-daemon
echo.
echo ========================================
echo Compilacao concluida!
echo APK gerado em: app\build\outputs\apk\debug\app-debug.apk
echo ========================================
pause

