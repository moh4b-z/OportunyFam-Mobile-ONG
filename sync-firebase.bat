@echo off
echo ================================================
echo Sincronizando dependencias do Firebase
echo ================================================

cd /d "%~dp0"

echo.
echo Executando Gradle Sync...
call gradlew --refresh-dependencies

echo.
echo ================================================
echo Sincronizacao concluida!
echo ================================================
echo.
echo Agora voce pode:
echo 1. Reconstruir o projeto no Android Studio
echo 2. Ou executar: gradlew assembleDebug
echo ================================================
pause

