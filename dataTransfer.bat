@echo off
setlocal

cls

:NOPATH
set /p source=Enter Source Path: 
echo %source% | findstr /r ^[a-z]\:\\ > nul 2>&1
if errorlevel 1 ( 
echo Input does not contain a valid path%!
GOTO :NOPATH )
echo contains valid ^path

echo Destination: %cd%\Transfer-%date% 
set /p correct=Is this the destionation path y/n? 
 
if /I "%correct%"=="y" ( set destination=%cd%\Transfer-%date%  )

if /I "%correct%"=="n" ( 
echo If absolute path is not specified, path will be set from cwd. Where the script is executed from.
echo Example If you enter "folder\somefolder" the destination ^path would be "%cd%\folder\somefolder"
set /p destination=Enter destination path: 
)

robocopy %source% %destination% /e /xj /w:0 /r:0 
