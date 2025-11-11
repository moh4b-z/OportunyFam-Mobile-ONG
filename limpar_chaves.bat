@echo off
REM ===============================================
REM  Script para Limpar Chaves do Código
REM  Use antes de fazer commit no Git
REM ===============================================

echo.
echo ============================================
echo   LIMPANDO CHAVES SENSIVEIS DO CODIGO
echo ============================================
echo.

echo [1/2] Verificando arquivo AzureConfig.kt...

set "AZURE_CONFIG=app\src\main\java\com\oportunyfam_mobile_ong\Config\AzureConfig.kt"

if not exist "%AZURE_CONFIG%" (
    echo [ERRO] Arquivo nao encontrado: %AZURE_CONFIG%
    pause
    exit /b 1
)

echo [2/2] Criando backup...
copy "%AZURE_CONFIG%" "%AZURE_CONFIG%.backup" >nul
echo Backup criado: %AZURE_CONFIG%.backup

echo.
echo ============================================
echo   IMPORTANTE:
echo ============================================
echo   1. Edite manualmente o arquivo:
echo      %AZURE_CONFIG%
echo.
echo   2. Altere a linha:
echo      private const val AZURE_STORAGE_KEY = "sua_chave..."
echo.
echo   3. Para:
echo      private const val AZURE_STORAGE_KEY = ""
echo.
echo   4. A chave continuara funcionando via local.properties
echo.
echo ============================================

echo.
echo Deseja abrir o arquivo agora? (S/N)
set /p RESPOSTA=

if /i "%RESPOSTA%"=="S" (
    start notepad "%AZURE_CONFIG%"
    echo Arquivo aberto no Notepad.
) else (
    echo Lembre-se de editar o arquivo antes do commit!
)

echo.
echo Processo concluido!
echo.
pause

