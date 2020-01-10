#!/bin/sh
${JAVA_HOME}/bin/java -Xmx1024m -DbirdsProjectConfiguration=${PROJECT_PROPERTIES_DIR}/${PROJECT_PROPERTIES_FILE} -classpath ${PROJECT_LIBRARY}/${JAR_NAME}.jar ${clientMainClass} -c $@
