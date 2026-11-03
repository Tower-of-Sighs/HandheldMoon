@echo off
setlocal EnableExtensions
cd /d "%~dp0"

rem ============================================================
rem  HandheldMoon dev launcher - Minecraft 26.1 targets
rem
rem  Finds a local JDK 25 and uses it for this window only, so
rem  the machine-wide JAVA_HOME is never modified.
rem
rem  Usage:
rem    run-client.bat                  gradlew runClient   (default)
rem    run-client.bat clean build      gradlew clean build
rem    run-client.bat :common:compileJava
rem
rem  Override the JDK with:  set HM_JAVA_HOME=D:\path\to\jdk-25
rem ============================================================

if defined HM_JAVA_HOME (
    set "JDK=%HM_JAVA_HOME%"
    goto :verify
)

rem ---- candidate locations (first hit wins) ----
set "JDK="
for %%D in (
    "%USERPROFILE%\.jdks\openjdk-25.0.2"
    "%USERPROFILE%\.jdks\openjdk-25"
    "%ProgramFiles%\Java\jdk-25"
    "%ProgramFiles%\Eclipse Adoptium\jdk-25"
    "%ProgramFiles%\Microsoft\jdk-25"
    "%ProgramFiles%\Zulu\zulu-25"
    "%ProgramFiles%\Amazon Corretto\jdk25"
) do (
    if not defined JDK if exist "%%~D\bin\java.exe" set "JDK=%%~D"
)

rem ---- fallback: any *25* folder under %USERPROFILE%\.jdks ----
if not defined JDK (
    for /d %%D in ("%USERPROFILE%\.jdks\*25*") do (
        if not defined JDK if exist "%%~fD\bin\java.exe" set "JDK=%%~fD"
    )
)

:verify
if defined JDK if not exist "%JDK%\bin\java.exe" (
    echo [HandheldMoon] Not a usable JDK home: %JDK%
    set "JDK="
)
if not defined JDK (
    echo [HandheldMoon] No JDK 25 found in the usual places.
    echo [HandheldMoon] Set HM_JAVA_HOME to a JDK 25 home and retry, e.g.
    echo [HandheldMoon]   set HM_JAVA_HOME=C:\path\to\jdk-25
    echo [HandheldMoon] Falling back to the current JAVA_HOME: %JAVA_HOME%
    goto :run
)

echo [HandheldMoon] Using JDK: %JDK%
set "JAVA_HOME=%JDK%"
set "PATH=%JDK%\bin;%PATH%"

:run
if "%~1"=="" (
    echo [HandheldMoon] gradlew.bat runClient
    call "%~dp0gradlew.bat" runClient --console plain
) else (
    echo [HandheldMoon] gradlew.bat %*
    call "%~dp0gradlew.bat" %* --console plain
)
set "EXITCODE=%ERRORLEVEL%"
echo [HandheldMoon] Exit code: %EXITCODE%
endlocal & exit /b %EXITCODE%
