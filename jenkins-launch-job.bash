#!/bin/bash


#==== FUNCTION ================================================================
#   NAME: usage
#   DESCRIPTION: Display usage information for this script
#==============================================================================
usage(){
cat << EOF

Script to submit job on Jenkins to build NGL

EXAMPLES:
    # Build master branch (all tests)
    $ ./jenkins-launch-job.bash master

    # Build master branch (only fr.cea.ig.ngl.test.NGLTests suite)
    $ ./jenkins-launch-job.bash suite

    # Build one isoprod branch (here master-isoprod-sq-2.5.X)    
    $ ./jenkins-launch-job.bash isoprod -a sq -v 2.5.X
    
    # Build all isoprod branch (master-isoprod-...-...)
    $ ./jenkins-launch-job.bash isoprod -a all -v all

EOF
}


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

mode_isoprod=false
mode_master=false
mode_suite=false

# Check execution mode
case "${1}" in
    master)
        echo '## Submit Jenkins Building Job: NGL ##'
        mode_master=true
        OPTIND=$(( $OPTIND + 1 ))
        ;;
    isoprod)
        echo '## Submit Jenkins Building Job: NGL-isoprod ##'
        mode_isoprod=true
        OPTIND=$(( $OPTIND + 1 ))
        ;;
    suite)
        echo '## Submit Jenkins Building Job: NGL-master-NGLTests ##'
        mode_suite=true
        OPTIND=$(( $OPTIND + 1 ))
        ;;
    help)
        usage
        ;;
    *)
        usage
        ;;
esac


if [[ "$mode_isoprod" = true ]]
then
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
fi

if [[ "$mode_master" = true ]]
then
    curl -X GET -i 'http://rama.genoscope.cns.fr:8080/buildByToken/build?job=NGL&token=ngl-master'
    echo 'Job submitted'
elif [[ "$mode_suite" = true ]]
then
    curl -X GET -i 'http://rama.genoscope.cns.fr:8080/buildByToken/build?job=NGL-master-NGLTests&token=ngl-master'
    echo 'Job submitted'
elif [[ "$mode_isoprod" = true ]] && [[ -n ${application} ]] && [[ -n ${version} ]]
then
    echo "Build ngl-${application} - ${version}"
    curl -X GET -i "http://rama.genoscope.cns.fr:8080/buildByToken/buildWithParameters?job=NGL-isoprod&token=ngl-isoprod&application=${application}&version=${version}"
    echo 'Job submitted'
else
    echoerr 'No job submitted'
    echoerr 'check the command line'
    usage
    exit 1
fi
