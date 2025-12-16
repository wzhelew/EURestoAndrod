#!/bin/sh

APP_BASE_DIR=$(dirname "$0")
APP_HOME=$(cd "$APP_BASE_DIR" && pwd)

JAVA_EXE="${JAVA_HOME:+$JAVA_HOME/bin/}java"
if [ ! -x "$JAVA_EXE" ]; then
  JAVA_EXE="java"
fi

WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
WRAPPER_PROPS="$APP_HOME/gradle/wrapper/gradle-wrapper.properties"

if [ ! -f "$WRAPPER_JAR" ]; then
  echo "Gradle wrapper JAR not found: $WRAPPER_JAR" >&2
  exit 1
fi
if [ ! -f "$WRAPPER_PROPS" ]; then
  echo "Gradle wrapper properties not found: $WRAPPER_PROPS" >&2
  exit 1
fi

exec "$JAVA_EXE" -classpath "$WRAPPER_JAR" org.gradle.wrapper.GradleWrapperMain "$@"
