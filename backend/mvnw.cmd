@echo off
setlocal
set "MAVEN_VERSION=3.9.9"
set "WRAPPER_DIR=%~dp0.mvn\wrapper"
set "MAVEN_HOME=%USERPROFILE%\.m2\wrapper\dists\apache-maven-%MAVEN_VERSION%"
if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
  echo Downloading Apache Maven %MAVEN_VERSION%...
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; $root='%USERPROFILE%\.m2\wrapper\dists'; New-Item -ItemType Directory -Force -Path $root | Out-Null; $zip=Join-Path $env:TEMP 'apache-maven-%MAVEN_VERSION%-bin.zip'; Invoke-WebRequest 'https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/%MAVEN_VERSION%/apache-maven-%MAVEN_VERSION%-bin.zip' -OutFile $zip; Expand-Archive -Force $zip $root"
  if errorlevel 1 exit /b 1
)
"%MAVEN_HOME%\bin\mvn.cmd" %*
endlocal
