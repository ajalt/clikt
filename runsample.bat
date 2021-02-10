@if "%DEBUG%"=="" @echo off
:: Run one of the samples.
:: The first argument must be the name of the sample task (e.g. echo).
:: Any remaining arguments are forwarded to the sample's argv.

if "%OS%"=="Windows_NT" setlocal EnableDelayedExpansion

set TASK=%~1

set UNKNOWN_SAMPLE=false
if "%TASK: =%"==""            set UNKNOWN_SAMPLE=true
if not exist "samples\%TASK%" set UNKNOWN_SAMPLE=true

if "%UNKNOWN_SAMPLE%"=="true" (
    echo Unknown sample: '%TASK%'
    exit /b 1
)

set ARGS=%*
set ARGS=!ARGS:*%1=!
if "!ARGS:~0,1!"==" " set ARGS=!ARGS:~1!

call gradlew --quiet ":samples:%TASK%:installDist" && call "samples\%TASK%\build\install\%TASK%\bin\%TASK%" %ARGS%

if "%OS%"=="Windows_NT" endlocal
