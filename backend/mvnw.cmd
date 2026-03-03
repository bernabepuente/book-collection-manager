@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@REM ----------------------------------------------------------------------------
@REM Apache Maven Wrapper startup batch script, version 3.3.2
@REM ----------------------------------------------------------------------------

@IF "%__MVNW_ARG0_NAME__%"=="" (SET __MVNW_ARG0_NAME__=%~nx0)
@SET __MVNW_CMD__=
@SET __MVNW_ERROR__=
@SET __MVNW_SAVE_ERRORLEVEL__=
@SET __MVNW_SAVE_CD__=%CD%
@CD /D %~dp0
@SET __MVNW_SAVE_ERRORLEVEL__=%ERRORLEVEL%

@CALL :init_wrapperdir %~dp0
@IF NOT "%__MVNW_WDIR__%"=="" (
  @CD /D %__MVNW_WDIR__%
  @IF NOT "%ERRORLEVEL%"=="0" (SET __MVNW_ERROR__=&GOTO :mvnw_end)
)

@SET MAVEN_PROJECTBASEDIR=%CD%

@CALL :find_mvn_cmd
@IF NOT "%ERRORLEVEL%"=="0" (SET __MVNW_ERROR__=&GOTO :mvnw_end)

@SET WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

@SET __MVNW_OUT__=
@FOR /F "usebackq tokens=1* delims==" %%A IN ("%__MVNW_WDIR__%\.mvn\wrapper\maven-wrapper.properties") DO (
  @IF "%%A"=="wrapperUrl" SET WRAPPER_JAR_URL=%%B
)

@IF NOT EXIST "%__MVNW_WDIR__%\.mvn\wrapper\maven-wrapper.jar" (
  @IF NOT "%MVNW_REPOURL%"=="" SET WRAPPER_JAR_URL=%MVNW_REPOURL%/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar
  @powershell -Command "&{"^
    "$webclient = New-Object System.Net.WebClient;"^
    "if (!([string]::IsNullOrEmpty('%MVNW_USERNAME%') -and [string]::IsNullOrEmpty('%MVNW_PASSWORD%'))) {"^
    "$webclient.Credentials = New-Object Net.NetworkCredential('%MVNW_USERNAME%', '%MVNW_PASSWORD%');"^
    "}"^
    "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12;"^
    "$webclient.DownloadFile('%WRAPPER_JAR_URL%', '%__MVNW_WDIR__%\.mvn\wrapper\maven-wrapper.jar');"^
  "}"
  @IF NOT "%ERRORLEVEL%"=="0" (
    @ECHO Cannot download maven-wrapper.jar >&2
    SET __MVNW_ERROR__=&GOTO :mvnw_end
  )
)

@SET __MVNW_EXEC__="%JAVA_HOME%\bin\java.exe"
@IF NOT EXIST %__MVNW_EXEC__% SET __MVNW_EXEC__=java.exe
%__MVNW_EXEC__% %MAVEN_OPTS% %MAVEN_DEBUG_OPTS% -classpath "%__MVNW_WDIR__%\.mvn\wrapper\maven-wrapper.jar" "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" %WRAPPER_LAUNCHER% %MAVEN_CONFIG% %*
@GOTO :mvnw_end

:find_mvn_cmd
@SET __MVNW_CMD__=%__MVNW_WDIR__%\.mvn\wrapper\maven-wrapper.jar
@EXIT /B 0

:init_wrapperdir
@SET __MVNW_WDIR__=%~1
@IF NOT "%__MVNW_WDIR__:~-1%"=="\" SET __MVNW_WDIR__=%__MVNW_WDIR__%\
@SET __MVNW_WDIR__=%__MVNW_WDIR__:~0,-1%
@EXIT /B 0

:mvnw_end
@CD /D %__MVNW_SAVE_CD__%
@SET __MVNW_SAVE_CD__=
@IF NOT "%__MVNW_SAVE_ERRORLEVEL__%"=="" EXIT /B %__MVNW_SAVE_ERRORLEVEL__%
@IF NOT "%__MVNW_ERROR__%"=="" EXIT /B 1
