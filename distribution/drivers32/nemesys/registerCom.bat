@echo off
REM First get admin rights...

:: BatchGotAdmin
:-------------------------------------
REM  --> Check for permissions
IF '%PROCESSOR_ARCHITECTURE%' EQU 'amd64' (
   >nul 2>&1 "%SYSTEMROOT%\SysWOW64\icacls.exe" "%SYSTEMROOT%\SysWOW64\config"
 ) ELSE (
   >nul 2>&1 "%SYSTEMROOT%\system32\icacls.exe" "%SYSTEMROOT%\system32\config"
)

REM --> If error flag set, we do not have admin.
if '%errorlevel%' NEQ '0' (
    echo Requesting administrative privileges...
    goto UACPrompt
) else ( goto gotAdmin )

:UACPrompt
    echo Set UAC = CreateObject^("Shell.Application"^) > "%temp%\getadmin.vbs"
    set params = %*:"=""
    echo UAC.ShellExecute "cmd.exe", "/c ""%~s0"" %params%", "", "runas", 1 >> "%temp%\getadmin.vbs"

    "%temp%\getadmin.vbs"
    del "%temp%\getadmin.vbs"
    exit /B

:gotAdmin
    pushd "%CD%"
    CD /D "%~dp0"
:--------------------------------------    
regsvr32 NemesysDotCom.dll