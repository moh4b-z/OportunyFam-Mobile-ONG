@echo off
chcp 65001 >nul
echo ========================================
echo  🔧 CORREÇÃO AVANÇADA - Firebase Gradle
echo ========================================
echo.

echo [ETAPA 1/8] Fechando Android Studio...
taskkill /F /IM studio64.exe 2>nul
taskkill /F /IM java.exe /T 2>nul
timeout /t 3 /nobreak >nul
echo ✅ Processos encerrados

echo.
echo [ETAPA 2/8] Limpando cache local do projeto...
if exist ".gradle" (
    rmdir /s /q ".gradle"
    echo ✅ .gradle removido
)
if exist ".idea" (
    rmdir /s /q ".idea"
    echo ✅ .idea removido
)
if exist "build" (
    rmdir /s /q "build"
    echo ✅ build removido
)
if exist "app\build" (
    rmdir /s /q "app\build"
    echo ✅ app\build removido
)

echo.
echo [ETAPA 3/8] Limpando cache global do Gradle...
set GRADLE_HOME=%USERPROFILE%\.gradle
if exist "%GRADLE_HOME%\caches" (
    rmdir /s /q "%GRADLE_HOME%\caches"
    echo ✅ Cache global removido
)

echo.
echo [ETAPA 4/8] Limpando cache do Android Studio...
if exist "%LOCALAPPDATA%\Google\AndroidStudio2024.2" (
    rmdir /s /q "%LOCALAPPDATA%\Google\AndroidStudio2024.2\caches"
    echo ✅ Cache do Android Studio removido
)
if exist "%LOCALAPPDATA%\Google\AndroidStudio2024.1" (
    rmdir /s /q "%LOCALAPPDATA%\Google\AndroidStudio2024.1\caches"
    echo ✅ Cache do Android Studio removido
)

echo.
echo [ETAPA 5/8] Limpando cache do Android SDK...
if exist "%USERPROFILE%\.android\build-cache" (
    rmdir /s /q "%USERPROFILE%\.android\build-cache"
    echo ✅ Build cache do Android removido
)

echo.
echo [ETAPA 6/8] Executando Gradle Clean...
call gradlew clean
if %ERRORLEVEL% EQU 0 (
    echo ✅ Gradle clean executado com sucesso
) else (
    echo ⚠️ Gradle clean com problemas, mas continuando...
)

echo.
echo [ETAPA 7/8] Forçando download de dependências...
call gradlew --refresh-dependencies
if %ERRORLEVEL% EQU 0 (
    echo ✅ Dependências atualizadas
) else (
    echo ⚠️ Problemas ao atualizar dependências
)

echo.
echo [ETAPA 8/8] Tentando build...
call gradlew build --stacktrace
if %ERRORLEVEL% EQU 0 (
    echo ✅ Build executado com sucesso!
) else (
    echo ⚠️ Build com erros, mas cache foi limpo
)

echo.
echo ========================================
echo  ✅ LIMPEZA COMPLETA FINALIZADA!
echo ========================================
echo.
echo 📋 PRÓXIMOS PASSOS:
echo.
echo 1. Abra o Android Studio
echo 2. File -^> Invalidate Caches -^> Invalidate and Restart
echo 3. Aguarde sincronização (barra inferior)
echo 4. Build -^> Clean Project
echo 5. Build -^> Rebuild Project
echo.
echo Se ainda houver erros, veja: SOLUCAO_DEFINITIVA.md
echo ========================================
pause

