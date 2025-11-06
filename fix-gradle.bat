@echo off
echo ========================================
echo  LIMPEZA COMPLETA DO PROJETO
echo ========================================
echo.

echo [1/5] Parando processos do Gradle...
taskkill /F /IM java.exe /T 2>nul
timeout /t 2 /nobreak >nul

echo [2/5] Removendo cache do Gradle local...
if exist ".gradle" (
    rmdir /s /q ".gradle"
    echo    - Cache .gradle removido
)

if exist ".idea" (
    rmdir /s /q ".idea"
    echo    - Cache .idea removido
)

if exist "build" (
    rmdir /s /q "build"
    echo    - Pasta build removida
)

if exist "app\build" (
    rmdir /s /q "app\build"
    echo    - Pasta app\build removida
)

echo [3/5] Removendo cache global do Gradle...
if exist "%USERPROFILE%\.gradle\caches" (
    rmdir /s /q "%USERPROFILE%\.gradle\caches"
    echo    - Cache global do Gradle removido
)

echo [4/5] Limpando projeto com Gradlew...
call gradlew clean

echo [5/5] Pronto! Agora:
echo    1. Abra o Android Studio
echo    2. File -^> Invalidate Caches -^> Invalidate and Restart
echo    3. Aguarde a sincronização automática
echo    4. Build -^> Rebuild Project
echo.
echo ========================================
pause

