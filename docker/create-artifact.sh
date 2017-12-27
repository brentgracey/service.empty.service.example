#!/usr/bin/env bash

####
# This script builds an sbt project and generates a binary in the form of a docker image.
#
# docker build container is defined to call this scrip on its start up
# ... ENTRYPOINT ["/code/docker/scripts/create-artifact.sh"]
####

IVY_DIR=/root/.ivy2
chmod 666 $IVY_DIR/.qordobaArtifactory*

SBT="sbt -no-colors"

echo "Building from source and publishing to a local Docker image"
cd /code
$SBT clean
env JAVA_OPTS="-Xmx1536m" $SBT docker:publishLocal

echo "Giving full perms; so Jenkins can remove its working dir of this build process."
chmod 777 -R /code
