#!/bin/sh

JAVA_EXEC=""
if which java ; then
    JAVA_EXEC="java"
else
    JAVA_EXEC="jre/bin/java"
#    ./jre/bin/java -jar ModTheSpire.jar
fi

JAVA_VERSION_CHECK="--version"
if $JAVA_EXEC $JAVA_VERSION_CHECK ; then
    printf ""
else
    JAVA_VERSION_CHECK="-version"
fi

# Print version, grab things that roughly match the version, and assume the first one found is the version
JAVA_VERSION=$($JAVA_EXEC $JAVA_VERSION_CHECK | grep -Eo '[0-9]+\.[0-9]+\.[0-9]+' | cut -d\  -f1)
JAVA_VERSION_START=$(echo $JAVA_VERSION | cut -d. -f1)
JAVA_AGENT_FLAG="-javaagent:squedgy-agent.jar"
JAVA_JAR_FLAG="-jar ModTheSpire.jar"

if test "$JAVA_VERSION_START" = "1" ; then
    # Pre Java 9
    $JAVA_EXEC $JAVA_AGENT_FLAG $JAVA_JAR_FLAG
else
    echo Java 9+ detected, adding relevant --add-opens
    # Java 9+
    ADD_OPEN_FLAGS=""
    for item in java.base/java.net java.base/jdk.internal.loader java.base/java.lang java.base/java.lang.reflect java.base/jdk.internal.reflect ; do
        ADD_OPEN_FLAGS="--add-opens $item=ALL-UNNAMED $ADD_OPEN_FLAGS"
    done
    $JAVA_EXEC $ADD_OPEN_FLAGS $JAVA_AGENT_FLAG $JAVA_JAR_FLAG
fi
