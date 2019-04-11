#!/bin/bash

## Build isoprod on Jenkins
## Script based on Jenkins env var
#   JOB_NAME
#   WORKSPACE

# repo cloned from
# git clone ssh://jenkins@arenal.genoscope.cns.fr:/env/cns/genosphere/git/lis/dev/ngl.git
# git clone ssh://jenkins@arenal.genoscope.cns.fr:/env/cns/genosphere/git/lis/dev/ngl-config.git


#==== FUNCTION ================================================================
#   NAME: echoerr
#   DESCRIPTION: print on stderr
#==============================================================================
echoerr() {
    printf "%s\n" "$*" >&2;
}

#==== FUNCTION ================================================================
#            NAME: die
#     DESCRIPTION: Management of subprocess errors
#   PARAMETERS  1: line number
#               2: message
#               3: exit code
#==============================================================================
die(){
    local parent_lineno message code
    parent_lineno="$1"
    message="$2"
    [[ -n $3 ]] && code="$3" || code=1
    if [[ -n "$message" ]]
    then
        echoerr "Error on or near line ${parent_lineno}: ${message}; exiting with status ${code}"
    else
        echoerr "Error on or near line ${parent_lineno}; exiting with status ${code}"
    fi
    exit "${code}"
}


###############
# Main script #
###############

trap 'die ${LINENO}' 1 15 ERR

export _JAVA_OPTIONS="-Xms1024m -Xmx4G -Xss256m -XX:MaxMetaspaceSize=1024m"

echo "Build ${JOB_NAME} (master)"
java -XX:MaxMetaspaceSize=1024m -Dsbt.log.noformat=true -Dlogger.file="${WORKSPACE}/ngl-config/ngl-tu-logger.xml" -jar /var/jenkins_home/sbt-launch.jar "-Dngl.test.conf.dir=${WORKSPACE}/ngl-config/TEST/" clean clean-files compile
echo "Build done"

echo "Run tests"
java -XX:MaxMetaspaceSize=1024m -Dsbt.log.noformat=true -Dlogger.file="${WORKSPACE}/ngl-config/ngl-tu-logger.xml" -jar /var/jenkins_home/sbt-launch.jar "-Dngl.test.conf.dir=${WORKSPACE}/ngl-config/TEST/" test
#java -Dsbt.log.noformat=true -Dlogger.file="${WORKSPACE}/ngl-config/ngl-tu-logger.xml" -jar /var/jenkins_home/sbt-launch.jar "-Dngl.test.conf.dir=${WORKSPACE}/ngl-config/TEST/" "ngl-common-tests/testOnly fr.cea.ig.ngl.test.NGLTests"

