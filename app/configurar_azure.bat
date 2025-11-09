@echo off
REM ============================================
REM Configurar Azure Storage Key
REM ============================================
REM Este arquivo configura a variável de ambiente
REM para a chave do Azure Storage temporariamente
REM ============================================

echo.
echo ========================================
echo   Configurando Azure Storage Key
echo ========================================
echo.

REM Ler a chave do local.properties
for /f "tokens=2 delims==" %%a in ('findstr "azure.storage.key" local.properties') do set AZURE_KEY=%%a

if "%AZURE_KEY%"=="" (
    echo [ERRO] Chave do Azure nao encontrada no local.properties!
    echo.
    echo Por favor, adicione a seguinte linha no arquivo local.properties:
    echo azure.storage.key=SUA_CHAVE_AQUI
    echo.
    pause
    exit /b 1
)

REM Configurar a variável de ambiente temporariamente
set AZURE_STORAGE_KEY=%AZURE_KEY%

echo [OK] Variavel de ambiente configurada com sucesso!
echo.
echo Agora execute o Android Studio a partir deste terminal:
echo.
echo 1. Navegue ate o Android Studio:
echo    cd "C:\Program Files\Android\Android Studio\bin"
echo.
echo 2. Execute o Android Studio:
echo    studio64.exe
echo.
echo Ou simplesmente abra o Android Studio e faca Gradle Sync:
echo    File ^> Sync Project with Gradle Files
echo.
pause
