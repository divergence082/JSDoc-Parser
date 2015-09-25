#!/usr/bin/env bash

VERSION=$1
BRANCH=`git branch | grep "*" | cut -d " " -f 2`

echo "BUILD: $VERSION $BRANCH"

sbt scalastyle && sbt compile

if [ "$BRANCH" == 'master' ];
then
    git tag -a "$VERSION" -m "$VERSION"
    git push --tags
    sed -i "s/version := .*/version := \"$VERSION-RELEASE\"/" build.sbt;
else
    sed -i "s/version := .*/version := \"$VERSION-SNAPSHOT\"/" build.sbt;
fi


sbt publish

git commit -am "$VERSION"
git push origin ${BRANCH}