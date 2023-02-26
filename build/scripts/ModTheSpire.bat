@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem

@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem
@rem  ModTheSpire startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem Add default JVM options here. You can also use JAVA_OPTS and MOD_THE_SPIRE_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS=

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto execute

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto execute

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\lib\ModTheSpire-3.30.3.jar;%APP_HOME%\lib\commons-lang3-3.7.jar;%APP_HOME%\lib\gson-2.8.9.jar;%APP_HOME%\lib\javassist-3.29.2-GA.jar;%APP_HOME%\lib\scannotation-1.0.3.jar;%APP_HOME%\lib\commons-logging-1.2.jar;%APP_HOME%\lib\asm-6.2.1.jar;%APP_HOME%\lib\semver4j-2.2.0.jar;%APP_HOME%\lib\steamworks4j-1.9.0.jar;%APP_HOME%\lib\kotlin-stdlib-jdk8-1.4.10.jar;%APP_HOME%\lib\kotlin-reflect-1.4.10.jar;%APP_HOME%\lib\gdx-controllers-lwjgl3-1.9.5.jar;%APP_HOME%\lib\gdx-backend-lwjgl3-1.9.5.jar;%APP_HOME%\lib\lwjgl-glfw-3.3.1.jar;%APP_HOME%\lib\lwjgl-glfw-3.3.1-natives-windows.jar;%APP_HOME%\lib\lwjgl-glfw-3.3.1-natives-linux.jar;%APP_HOME%\lib\lwjgl-glfw-3.3.1-natives-macos.jar;%APP_HOME%\lib\lwjgl-opengl-3.3.1.jar;%APP_HOME%\lib\lwjgl-openal-3.3.1.jar;%APP_HOME%\lib\lwjgl-openal-3.3.1-natives-windows.jar;%APP_HOME%\lib\lwjgl-openal-3.3.1-natives-linux.jar;%APP_HOME%\lib\lwjgl-openal-3.3.1-natives-macos.jar;%APP_HOME%\lib\lwjgl-jemalloc-3.1.0.jar;%APP_HOME%\lib\lwjgl-jemalloc-3.1.0-natives-windows.jar;%APP_HOME%\lib\lwjgl-jemalloc-3.1.0-natives-linux.jar;%APP_HOME%\lib\lwjgl-jemalloc-3.1.0-natives-macos.jar;%APP_HOME%\lib\lwjgl-3.3.1.jar;%APP_HOME%\lib\lwjgl-3.3.1-natives-windows.jar;%APP_HOME%\lib\lwjgl-3.3.1-natives-linux.jar;%APP_HOME%\lib\lwjgl-3.3.1-natives-macos.jar;%APP_HOME%\lib\javassist-3.12.1.GA.jar;%APP_HOME%\lib\kotlin-stdlib-jdk7-1.4.10.jar;%APP_HOME%\lib\kotlin-stdlib-1.4.10.jar;%APP_HOME%\lib\gdx-controllers-1.9.5.jar;%APP_HOME%\lib\gdx-1.9.5.jar;%APP_HOME%\lib\jlayer-1.0.1-gdx.jar;%APP_HOME%\lib\jorbis-0.0.17.jar;%APP_HOME%\lib\kotlin-stdlib-common-1.4.10.jar;%APP_HOME%\lib\annotations-13.0.jar


@rem Execute ModTheSpire
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %MOD_THE_SPIRE_OPTS%  -classpath "%CLASSPATH%" com.evacipated.cardcrawl.modthespire.Loader %*

:end
@rem End local scope for the variables with windows NT shell
if "%ERRORLEVEL%"=="0" goto mainEnd

:fail
rem Set variable MOD_THE_SPIRE_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
if  not "" == "%MOD_THE_SPIRE_EXIT_CONSOLE%" exit 1
exit /b 1

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
