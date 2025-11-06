@echo off
echo ========================================
echo Removendo secrets dos arquivos MD
echo ========================================

cd /d C:\Users\bruno\StudioProjects\OportunyFam-Mobile-ONGagr

echo.
echo Verificando arquivos com secrets...
findstr /S /N "1dY9IPE70" *.md

echo.
echo ========================================
echo Execute este comando para ver o diff:
echo git diff
echo ========================================
pause

