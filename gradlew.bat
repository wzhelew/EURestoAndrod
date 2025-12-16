@ECHO OFF
SETLOCAL

SET APP_HOME=%~dp0
SET WRAPPER_JAR=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar
SET WRAPPER_PROPS=%APP_HOME%\gradle\wrapper\gradle-wrapper.properties

SET JAVA_EXE=%JAVA_HOME%\bin\java.exe
IF NOT EXIST "%JAVA_EXE%" SET JAVA_EXE=java.exe

IF NOT EXIST "%WRAPPER_JAR%" (
  ECHO Gradle wrapper JAR not found: %WRAPPER_JAR%
  EXIT /B 1
)
IF NOT EXIST "%WRAPPER_PROPS%" (
  ECHO Gradle wrapper properties not found: %WRAPPER_PROPS%
  EXIT /B 1
)

"%JAVA_EXE%" -classpath "%WRAPPER_JAR%" org.gradle.wrapper.GradleWrapperMain %*
ENDLOCAL
