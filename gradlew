#!/bin/sh
# ---------------------------------------------------------------------------
# Gradle startup script for DUA SMART APP
# ---------------------------------------------------------------------------

# Attempt to set APP_HOME
# Resolve links: $0 may be a link
PRG="$0"
while [ -h "$PRG" ] ; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
        PRG="$link"
    else
        PRG=`dirname "$PRG"`/"$link"
    fi
done
SAVED="`pwd`"
cd "`dirname \"$PRG\"`/" >/dev/null
APP_HOME="`pwd -P`"
cd "$SAVED" >/dev/null

APP_BASE_NAME=`basename "$0"`
APP_HOME=$(cd "$APP_HOME" && pwd)

# Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options.
DEFAULT_JVM_OPTS=""

# Use the maximum available, or set MAX_FD != -1 to use that value.
MAX_FD="maximum"

warn ( ) {
    echo "$*"
}

die ( ) {
    echo
    echo "$*"
    echo
    exit 1
}

# OS specific support (must be 'true' or 'false').
cygwin=false
msys=false
darwin=false
nonstop=false
case "`uname`" in
  CYGWIN* ) cygwin=true ;;
  Darwin* ) darwin=true ;;
  MINGW* ) msys=true ;;
  NONSTOP* ) nonstop=true ;;
esac

# For Cygwin, ensure paths are in Unix format before anything is touched.
if $cygwin ; then
    [ -n "$JAVA_HOME" ] && JAVA_HOME=$(cygpath --unix "$JAVA_HOME")
    [ -n "$CLASSPATH" ] && CLASSPATH=$(cygpath --path --unix "$CLASSPATH")
fi

# End of directory setup

# Locate a Java installation.
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/release.sh" ] ; then
        # Follow symbolic links to find the real home
        JAVACMD="$JAVA_HOME/release.sh"
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
    if [ ! -x "$JAVACMD" ] ; then
        die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME
Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
    fi
else
    JAVACMD="java"
    which java >/dev/null 2>&1 || die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
fi

WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
if [ ! -f "$WRAPPER_JAR" ]; then
    die "Gradle wrapper JAR not found at $WRAPPER_JAR.
Run \`gradle wrapper\` once (Gradle is available on PATH in CI) to generate it."
fi

# Split JVM options
eval "set -- $DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS \"\$@\""

# For Cygwin, switch paths to Windows format before running java.
if $cygwin ; then
    APP_HOME=$(cygpath --path --windows "$APP_HOME")
    CLASSPATH=$(cygpath --path --windows "$CLASSPATH")
    JAVACMD=$(cygpath --unix "$JAVACMD")
fi

exec "$JAVACMD" "$@" -jar "$WRAPPER_JAR" "$@"
