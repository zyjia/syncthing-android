#!/bin/bash

set -e

NEW_VERSION_NAME=$1
OLD_VERSION_NAME=$(grep "versionName" "build.gradle" | awk '{print $2}')
if [[ -z $NEW_VERSION_NAME ]]
then
    echo "New version name is empty. Please set a new version. Current version: $OLD_VERSION_NAME"
    exit
fi

echo "

Checking for Syncthing Update
-----------------------------
"
PROJECT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "ext/syncthing/src/github.com/syncthing/syncthing/"
git fetch
CURRENT_TAG=$(git describe)
LATEST_TAG=$(git describe $(git rev-list --tags --max-count=1))
if [ $CURRENT_TAG != $LATEST_TAG ]
then
    git checkout -f $LATEST_TAG
    cd $PROJECT_DIR
    git add "ext/syncthing/src/github.com/syncthing/syncthing"
    git commit -m "Updated Syncthing to $LATEST_TAG"
    ./gradlew cleanNative buildNative
fi
cd $PROJECT_DIR


echo "

Updating Translations
-----------------------------
"
tx push -s
tx pull -a
./gradlew deleteUnsupportedPlayTranslations publishListingFatRelease
git add -A "src/fat/play/"
git add -A "src/main/res/values-*/strings.xml"
if ! git diff --cached --exit-code;
then
    git commit -m "Imported translations"
fi


echo "

Running Tests
-----------------------------
"
./gradlew lint
./gradlew connectedFatDebugAndroidTest
./gradlew testFatDebugUnitTest

echo "

Updating Version
-----------------------------
"
OLD_VERSION_CODE=$(grep "versionCode" "build.gradle" -m 1 | awk '{print $2}')
NEW_VERSION_CODE=$(($OLD_VERSION_CODE + 1))
sed -i "s/versionCode $OLD_VERSION_CODE/versionCode $NEW_VERSION_CODE/" build.gradle

OLD_VERSION_NAME=$(grep "versionName" "build.gradle" | awk '{print $2}')
sed -i "s/$OLD_VERSION_NAME/\"$1\"/" build.gradle
git add "build.gradle"
git commit -m "Bumped version to $NEW_VERSION_NAME"
git tag $NEW_VERSION_NAME

echo "
Update ready.
1. Run \`git push --follow-tags\`
2. Enter release notes at https://github.com/syncthing/syncthing-android/releases
3. Enter release notes at https://android.syncthing.net/job/Syncthing-Android-Release/configure
4. Start build at https://android.syncthing.net/job/Syncthing-Android-Release
"
