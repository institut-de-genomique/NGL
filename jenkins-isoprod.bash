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

#==== FUNCTION ================================================================
#            NAME: sbt_built
#     DESCRIPTION: launch build commands
#   PARAMETERS  1: branch name
#==============================================================================
function sbt_built {
    branch_name="$1"
    echo "Build ${JOB_NAME} (${branch_name})"
	java -XX:MaxMetaspaceSize=1024m -Dsbt.log.noformat=true -Dlogger.file="${WORKSPACE}/ngl-config/ngl-tu-logger.xml" -jar /var/jenkins_home/sbt-launch.jar "-Dngl.test.conf.dir=${WORKSPACE}/ngl-config/TEST/" clean clean-files compile
	echo "Build done"

	echo "Run tests"
	java -XX:MaxMetaspaceSize=1024m -Dsbt.log.noformat=true -Dlogger.file="${WORKSPACE}/ngl-config/ngl-tu-logger.xml" -jar /var/jenkins_home/sbt-launch.jar "-Dngl.test.conf.dir=${WORKSPACE}/ngl-config/TEST/" "ngl-common-tests/test"
	java -XX:MaxMetaspaceSize=1024m -Dsbt.log.noformat=true -Dlogger.file="${WORKSPACE}/ngl-config/ngl-tu-logger.xml" -jar /var/jenkins_home/sbt-launch.jar "-Dngl.test.conf.dir=${WORKSPACE}/ngl-config/TEST/" "ngl-${application}/test"
    echo "Tests done"
}

###############
# Main script #
###############

trap 'die ${LINENO}' 1 15 ERR

optspec="v:a:"
while getopts "$optspec" optchar; do
    case "${optchar}" in
        # options
        v)
            version="${OPTARG}"
            ;;
        a)
            application="${OPTARG}"
            ;;
    esac
done


export _JAVA_OPTIONS="-Xms1024m -Xmx4G -Xss256m -XX:MaxMetaspaceSize=1024m"

if [[ "${version}" == "all" && "${application}" == "all" ]]
then
    for branch in $(git branch -r | grep -E 'master-isoprod-.+-.+\.X$')
        do
            branch_name=$(echo "${branch}" | sed 's/origin\///')
            git checkout -b "${branch_name}" "${branch}"
            sbt_built "${branch_name}"
        done
else
	branch_name="master-isoprod-${application}-${version}"
	git checkout -b "${branch_name}" "origin/${branch_name}"
    sbt_built "${branch_name}"
fi
