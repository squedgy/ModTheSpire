$JAVA_EXEC=""
if (Get-Command java -errorAction SilentlyContinue ) {
    $JAVA_EXEC="java"
} else {
    $JAVA_EXEC="jre/bin/java"
}

$JAVA_VERSION_CHECK="--version"
if (!(Invoke-Expression "$JAVA_EXEC $JAVA_VERSION_CHECK")) {
    $JAVA_VERSION_CHECK="-version"
}

# Print version, grab things that roughly match the version, and assume the first one found is the version
$JAVA_VERSION=(Invoke-Expression "$JAVA_EXEC $JAVA_VERSION_CHECK" | Select-String -Pattern '[0-9]+\.[0-9]+\.[0-9]+' -AllMatches | Select-Object -ExpandProperty Matches -First 1 | Select-Object -ExpandProperty Value)
$JAVA_VERSION_START=((echo $JAVA_VERSION) -Split '\.')[0]
$JAVA_AGENT_FLAG="-javaagent:squedgy-agent.jar"
$JAVA_JAR_FLAG="-jar ModTheSpire.jar"

if ("$JAVA_VERSION_START" -eq "1") {
    # Pre Java 9
    Invoke-Expression "$JAVA_EXEC $JAVA_AGENT_FLAG $JAVA_JAR_FLAG"
} else {
    # Java 9+
    echo "Java 9+ detected, adding relevant --add-opens"
    $ADD_OPEN_FLAGS=""
    $TARGETS=@("java.base/java.net", "java.base/jdk.internal.loader", "java.base/java.lang", "java.base/java.lang.reflect", "java.base/jdk.internal.reflect")
    foreach ($item in $TARGETS) {
        $ADD_OPEN_FLAGS="--add-opens $item=ALL-UNNAMED $ADD_OPEN_FLAGS"
    }
    Invoke-Expression "$JAVA_EXEC $ADD_OPEN_FLAGS $JAVA_AGENT_FLAG $JAVA_JAR_FLAG"
}
