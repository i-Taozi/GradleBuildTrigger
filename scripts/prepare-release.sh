#!/bin/bash

SCRIPT_NAME="Dolphin Platform Release preparation"
SCRIPT_VERSION="v1.0.0 (2018-02-15)";

function show_help() {
    echo "Usage: prepare-release <package> <version> [candidate]"
    echo
    echo "  package:      The package that should be released. Possible packages are: javascript, js-frameworks, java-examples, java"
    echo "  version:      The release version number in the format major.minor.patch, e.g. 1.0.0"
    echo "  candidate:    Optional release candidate number, e.g 4"
    echo
    echo "Full example:  ./prepare-release javascript 1.0.0 4"
    echo "  Will release javascript and frameworks as 1.0.0-CR.4 and java as 1.0.0.CR4"
    echo
    echo "This script expects the packages to be found as subdirectories (git projects) relative to this script"
    echo "  - javascript       dolphin-platform-js"
    echo "  - js-frameworks    dolphin-platform-polymer, dolphin-platform-angularjs"
    echo "  - java-examples    dolphin-platform examples"
    echo "  - java             dolphin-platform"
    echo
    echo "Because of the project dependencies, the order of releases MUST be"
    echo "  javascript, js-frameworks, java-examples, java"
    exit 1
}

function notify() {
    if [ `uname -s` == "Darwin" ]; then
        # On Mac OS, notify via Notification Center
        osascript -e "display notification \"$1\" with title \"$SCRIPT_NAME\" subtitle \"$2\""
    fi
    if [ `uname -s` == "Linux" ]; then
        # On Linux, notify via notify-send
        which notify-send && notify-send "$1" "$2"
    fi
}

echo "$SCRIPT_NAME $SCRIPT_VERSION"

echo

if [ $# -lt 1 ]; then
    echo "No arguments supplied."
    echo
    show_help
fi

if [[ $1 =~ ^help$ ]]; then
    show_help
elif [[ $1 =~ ^--help$ ]]; then
    show_help
elif [[ $1 =~ ^-h$ ]]; then
    show_help
fi

PACKAGE=0

if [ $1 == "javascript" ]; then
    PACKAGE=1
elif [ $1 == "js-frameworks" ]; then
    PACKAGE=2
elif [ $1 == "java-examples" ]; then
    PACKAGE=3
elif [ $1 == "java" ]; then
    PACKAGE=4
fi

if [ $PACKAGE -eq 0 ]; then
    echo "$1 does not match a valid package name. Possible values are: javascript, js-frameworks, java"
    exit 1
fi

VERSION_VALID=0
RC_VALID=1

if [[ $2 =~ ^[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}$ ]]; then
    VERSION_VALID=1
fi

if [ $VERSION_VALID -eq 0 ]; then
    echo "$2 does not match a valid version number"
    exit 1
fi

VERSION_NUMBER=$2
RELEASE_CANDIDATE_NUMBER=""

if [ $# -eq 2 ]; then
    if [[ $3 =~ ^[0-9]{1,3}$ ]]; then
        RC_VALID=1
    else
        RC_VALID=0
    fi
fi

if [ $RC_VALID -eq 0 ]; then
    echo "$3 does not match a valid release candidate number"
    exit 1
else
    RELEASE_CANDIDATE_NUMBER=$3
fi

if [ $RELEASE_CANDIDATE_NUMBER == "" ]; then
    echo "Release will be created as version: $VERSION_NUMBER"
    JAVA_VERSION="$VERSION_NUMBER"
    JAVASCRIPT_VERSION="$VERSION_NUMBER"
else
    echo "Release will be created as version: $VERSION_NUMBER, Release Candidate $RELEASE_CANDIDATE_NUMBER"
    JAVA_VERSION="$VERSION_NUMBER.CR$RELEASE_CANDIDATE_NUMBER"
    JAVASCRIPT_VERSION="$VERSION_NUMBER-CR.$RELEASE_CANDIDATE_NUMBER"
fi

echo
echo "Java version will be: $JAVA_VERSION"
echo "JavaScript versions will be: $JAVASCRIPT_VERSION"
echo

DIRECTORY_MAIN_PROJECT="dolphin-platform"
DIRECTORY_JS_CORE_PROJECT="dolphin-platform-js"
DIRECTORY_ANGULAR_PROJECT="dolphin-platform-angularjs"
DIRECTORY_POLYMER_PROJECT="dolphin-platform-polymer"

PROJECT_DIRECTORIES=($DIRECTORY_MAIN_PROJECT $DIRECTORY_JS_CORE_PROJECT $DIRECTORY_ANGULAR_PROJECT $DIRECTORY_POLYMER_PROJECT)

JAVA_DIRECTORIES=($DIRECTORY_MAIN_PROJECT)
JAVASCRIPT_DIRECTORIES=($DIRECTORY_JS_CORE_PROJECT $DIRECTORY_ANGULAR_PROJECT $DIRECTORY_POLYMER_PROJECT)

SCRIPT_DIR="$(pwd)";

echo "Checking release requirements"

FAULT=0
for i in "${PROJECT_DIRECTORIES[@]}"
do
    cd $SCRIPT_DIR
    if [ ! -d "$i" ]; then
        echo "- Project directory $i not found!"
        FAULT=1
    else
        cd "$i"
        CURRENT_DIR="$(pwd)"
        BRANCH="$(git symbolic-ref -q HEAD)"
        BRANCH=${BRANCH##refs/heads/}
        BRANCH=${BRANCH:-HEAD}
        
        if [ "$BRANCH" != "master" ]; then
            echo "- Branch '$BRANCH' found for $CURRENT_DIR! Not ok. You will have to fix that by hand."
            FAULT=1
        else
            echo "- Branch 'master' found for $CURRENT_DIR! Ok."
        fi

        UNCOMMITED_CHANGES=0

        MODIFYED_FILES="$(git status -s | grep M)"
        MODIFYED_FILES=${MODIFYED_FILES/\?\? prepare-release.sh/}
        if [ "$MODIFYED_FILES" != "" ]; then
            UNCOMMITED_CHANGES=1
        fi

        DELETED_FILES="$(git status -s | grep D)"
        DELETED_FILES=${DELETED_FILES/\?\? prepare-release.sh/}
        if [ "$DELETED_FILES" != "" ]; then
            UNCOMMITED_CHANGES=1
        fi

        UNKOWN_FILES="$(git status -s | grep \?\?)"
        UNKOWN_FILES=${UNKOWN_FILES/\?\? prepare-release.sh/}
        if [ "$UNKOWN_FILES" != "" ]; then
            UNCOMMITED_CHANGES=1
        fi

        if [ $UNCOMMITED_CHANGES -eq 1 ]; then
            echo "- Uncommited changes found in $CURRENT_DIR! Please fix that!"
            FAULT=1
        fi
    fi

done

cd $SCRIPT_DIR

if [ $FAULT -eq 1 ]; then
    echo
    echo "Fix problems listed above!"
    exit 1
fi

# params <project-directory> <bower-directory>
function bower_install() {
    cd $SCRIPT_DIR
    cd "$1"

    cd "$2"

    echo "- Bower install for $2"

    rm -rf bower_components/
    bower install --force-latest > /dev/null 2>&1
    RETURN=$?
    if [[ $RETURN != 0 ]]; then
        echo "- Bower install failed!"
        exit $RETURN;
    else
        echo "- Bower install ok."
    fi
}

# params <directory>
function update_master() {
    cd $SCRIPT_DIR
    cd "$1"
    CURRENT_DIR="$(pwd)"

    echo "- Pull latest version of master for $CURRENT_DIR"
    git pull > /dev/null 2>&1
}

# params <directory> <version>
function create_branch() {
    cd $SCRIPT_DIR
    cd "$1"
    CURRENT_DIR="$(pwd)"

    BRANCH_NAME="release/$2"
    echo "- Creating branch $BRANCH_NAME for $CURRENT_DIR"

    git branch $BRANCH_NAME > /dev/null 2>&1
}

# params <directory> <version>
function switch_branch() {
    cd $SCRIPT_DIR
    cd "$1"
    CURRENT_DIR="$(pwd)"

    BRANCH_NAME="release/$2"
    echo "- Switching branch $BRANCH_NAME for $CURRENT_DIR"

    git checkout $BRANCH_NAME > /dev/null 2>&1
}

# params <regex> <file>
function sed_withfix() {
    sed -i -e "$1" $2
    # macos fix
    if [ -f "$2-e" ]; then
        rm "$2-e"
    fi
}

# params <version>
function release_javascript() {
    cd $SCRIPT_DIR
    echo "Build for dolphin-platform-js on master branch"
    update_master $DIRECTORY_JS_CORE_PROJECT  

    npm run clean:all > /dev/null 2>&1
    RETURN=$?
    if [[ $RETURN != 0 ]]; then
        echo "- Clean failed!"
        exit $RETURN;
    else
        echo "- Clean ok."
    fi

    npm run build:install > /dev/null 2>&1
    RETURN=$?
    if [[ $RETURN != 0 ]]; then
        echo "- Install+Build failed!"
        exit $RETURN;
    else
        echo "- Install+Build ok."
    fi

    npm run test > /dev/null 2>&1
    RETURN=$?
    if [[ $RETURN != 0 ]]; then
        echo "- Test failed!"
        exit $RETURN;
    else
        echo "- Test ok."
    fi

    echo "Create release for dolphin-platform-js on master branch"

    echo "- Set up version number $1 for sonar-project.properties, package.json and bower.json"
    sed_withfix "s/sonar.projectVersion=.*/sonar.projectVersion=${1}/g" sonar-project.properties
    sed_withfix "s/\"version\"\: \".*\",/\"version\"\: \"${1}\",/g" package.json
    sed_withfix "s/\"version\"\: \".*\",/\"version\"\: \"${1}\",/g" bower.json

    echo "- Commit changes to master"

    git add -A > /dev/null 2>&1
    git commit -m "Version updated to $1" > /dev/null 2>&1

    create_branch $DIRECTORY_JS_CORE_PROJECT $1
    switch_branch $DIRECTORY_JS_CORE_PROJECT $1

    echo "- Create tag for $1"
    git tag $1 > /dev/null 2>&1
}

# params <version>
function release_polymer() {
    cd $SCRIPT_DIR
    echo "Build for dolphin-platform-polymer on master"
    update_master $DIRECTORY_POLYMER_PROJECT

    echo "- Update dependency for dolphin-platform.js in bower.json"
    sed_withfix "s/\"dolphin-platform-js\": \".*\"/\"dolphin-platform-js\"\: \"${1}\"/g" bower.json

    bower install --force-latest > /dev/null 2>&1
    RETURN=$?
    if [[ $RETURN != 0 ]]; then
        echo "- Bower install failed!"
        exit $RETURN;
    else
        echo "- Bower install ok."
    fi

    npm run install:all > /dev/null 2>&1
    RETURN=$?
    if [[ $RETURN != 0 ]]; then
        echo "- NPM install:all failed!"
        exit $RETURN;
    else
        echo "- NPM install:all ok."
    fi

    npm run lint > /dev/null 2>&1
    RETURN=$?
    if [[ $RETURN != 0 ]]; then
        echo "- NPM lint failed!"
        exit $RETURN;
    else
        echo "- NPM lint ok."
    fi

    npm run test > /dev/null 2>&1
    RETURN=$?
    if [[ $RETURN != 0 ]]; then
        echo "- NPM test failed!"
        exit $RETURN;
    else
        echo "- NPM test ok."
    fi

    echo "Create release for dolphin-platform-polymer on master branch"

    echo "- Set up version number $1 for package.json and bower.json"
    # sonar-project.properties does not exist in that project
    # sed -i -e "s/sonar.projectVersion=.*/sonar.projectVersion=${1}/g" sonar-project.properties
    sed_withfix "s/\"version\"\: \".*\",/\"version\"\: \"${1}\",/g" package.json
    sed_withfix "s/\"version\"\: \".*\",/\"version\"\: \"${1}\",/g" bower.json

    echo "- Commit changes to master"

    git add -A > /dev/null 2>&1
    git commit -m "Version updated to $1" > /dev/null 2>&1

    create_branch $DIRECTORY_POLYMER_PROJECT $1
    switch_branch $DIRECTORY_POLYMER_PROJECT $1

    echo "- Create tag for $1"
    git tag $1 > /dev/null 2>&1
}

# params <version>
function release_angularjs() {
    cd $SCRIPT_DIR
    echo "Build for dolphin-platform-angularjs on master"
    update_master $DIRECTORY_ANGULAR_PROJECT

    echo "- Update dependency for dolphin-platform.js in bower.json"
    sed_withfix "s/\"dolphin-platform-js\": \".*\",/\"dolphin-platform-js\"\: \"${1}\",/g" bower.json

    bower install > /dev/null 2>&1
    RETURN=$?
    if [[ $RETURN != 0 ]]; then
        echo "- Bower install failed!"
        exit $RETURN;
    else
        echo "- Bower install ok."
    fi

    npm install > /dev/null 2>&1
    RETURN=$?
    if [[ $RETURN != 0 ]]; then
        echo "- NPM install failed!"
        exit $RETURN;
    else
        echo "- NPM install ok."
    fi

    gulp clean > /dev/null 2>&1
    RETURN=$?
    if [[ $RETURN != 0 ]]; then
        echo "- Clean failed!"
        exit $RETURN;
    else
        echo "- Clean ok."
    fi

    gulp verify > /dev/null 2>&1
    RETURN=$?
    if [[ $RETURN != 0 ]]; then
        echo "- Verify failed!"
        exit $RETURN;
    else
        echo "- Verify ok."
    fi

    gulp build > /dev/null 2>&1
    RETURN=$?
    if [[ $RETURN != 0 ]]; then
        echo "- Build failed!"
        exit $RETURN;
    else
        echo "- Build ok."
    fi

    echo "Create release for dolphin-platform-angularjs on master branch"

    echo "- Set up version number $1 for sonar-project.properties, package.json and bower.json"
    sed_withfix "s/sonar.projectVersion=.*/sonar.projectVersion=${1}/g" sonar-project.properties
    sed_withfix "s/\"version\"\: \".*\",/\"version\"\: \"${1}\",/g" package.json
    sed_withfix "s/\"version\"\: \".*\",/\"version\"\: \"${1}\",/g" bower.json

    echo "- Commit changes to master"

    git add -A > /dev/null 2>&1
    git commit -m "Version updated to $1" > /dev/null 2>&1

    create_branch $DIRECTORY_ANGULAR_PROJECT $1
    switch_branch $DIRECTORY_ANGULAR_PROJECT $1

    echo "- Create tag for $1"
    git tag $1 > /dev/null 2>&1
}

# params <javascript-version> <version>
function release_java_examples() {
    cd $SCRIPT_DIR
    echo "Build for dolphin-platform on master"
    update_master $DIRECTORY_MAIN_PROJECT

    echo "- Update dependency for dolphin-platform-angularjs in bower.json for all examples"
    sed_withfix "s/\"dolphin-platform-angularjs\": \".*\",/\"dolphin-platform-angularjs\"\: \"${1}\",/g" platform-examples/todo-example/angularjs-client/bower.json
    sed_withfix "s/\"dolphin-platform-angularjs\": \".*\",/\"dolphin-platform-angularjs\"\: \"${1}\",/g" platform-examples/process-monitor-sample/process-monitor-client-angularjs/bower.json

    echo "- Update dependency for dolphin-platform-polymer in bower.json for all examples"
    sed_withfix "s/\"dolphin-platform-polymer\": \".*\",/\"dolphin-platform-polymer\"\: \"${1}\",/g" platform-examples/todo-example/polymer-client/bower.json
    sed_withfix "s/\"dolphin-platform-polymer\": \".*\",/\"dolphin-platform-polymer\"\: \"${1}\",/g" platform-examples/process-monitor-sample/process-monitor-client-polymer/bower.json
    sed_withfix "s/\"dolphin-platform-polymer\": \".*\",/\"dolphin-platform-polymer\"\: \"${1}\",/g" platform-examples/web-deployment-example/polymer-client/bower.json
    sed_withfix "s/\"dolphin-platform-polymer\": \".*\",/\"dolphin-platform-polymer\"\: \"${1}\",/g" platform-extras/dolphin-platform-projector-polymer/bower.json

    bower_install $DIRECTORY_MAIN_PROJECT platform-examples/todo-example/angularjs-client
    bower_install $DIRECTORY_MAIN_PROJECT platform-examples/process-monitor-sample/process-monitor-client-angularjs

    bower_install $DIRECTORY_MAIN_PROJECT platform-examples/todo-example/polymer-client
    bower_install $DIRECTORY_MAIN_PROJECT platform-examples/process-monitor-sample/process-monitor-client-polymer
    bower_install $DIRECTORY_MAIN_PROJECT platform-examples/web-deployment-example/polymer-client
    bower_install $DIRECTORY_MAIN_PROJECT platform-extras/dolphin-platform-projector-polymer

    cd $SCRIPT_DIR
    cd $DIRECTORY_MAIN_PROJECT

    DATE=`date '+%b, %d %Y'`
    NL='
'
    sed_withfix "s/The annotations defines rhe split between provate and public API, too./The annotations defines rhe split between provate and public API, too.\\${NL}\\${NL}\\${NL}== Version ${2}\\${NL}_Release date: ${DATE}_\\${NL}\\${NL} \* Insert Changelog here!\\${NL}/g" documentation/src/docs/asciidoc/changelog.adoc
}

# params <javascript-version> <version>
function release_java() {
    cd $SCRIPT_DIR
    echo "Build for dolphin-platform on master"
    
    create_branch $DIRECTORY_MAIN_PROJECT $2
    switch_branch $DIRECTORY_MAIN_PROJECT $2

    ./gradlew clean > /dev/null 2>&1
    RETURN=$?
    if [[ $RETURN != 0 ]]; then
        echo "- Clean failed!"
        exit $RETURN;
    else
        echo "- Clean ok."
    fi

    ./gradlew build > /dev/null 2>&1
    RETURN=$?
    if [[ $RETURN != 0 ]]; then
        echo "- Build failed!"
        exit $RETURN;
    else
        echo "- Build ok."
    fi

    sed_withfix "s/version=.*/version=${2}/g" gradle.properties

    echo "- Create tag for $2"
    git tag $2 > /dev/null 2>&1

}

if [ $PACKAGE -eq 1 ]; then
    release_javascript $JAVASCRIPT_VERSION

    echo "- Please check the new release manually in $DIRECTORY_JS_CORE_PROJECT"
    echo "  + Check files changed for the master branch with 'git show' and 'git log'"
    echo "  + Check files changed for the release/$JAVASCRIPT_VERSION branch with 'git show' and 'git log'"
    echo "  + Check the existence of the tag for '$JAVASCRIPT_VERSION' with 'git tag'"
    echo "  + If the check is okay, upload the release to github.com. For the master do 'git push', for the branch do 'git push --set-upstream origin', and for the tag do 'git push origin $JAVASCRIPT_VERSION'"

    notify "dolphin-platform-js $JAVASCRIPT_VERSION" "Release prepared"
elif [ $PACKAGE -eq 2 ]; then
    release_angularjs $JAVASCRIPT_VERSION
    release_polymer $JAVASCRIPT_VERSION

    echo "- Please check the new release manually in $DIRECTORY_POLYMER_PROJECT and $DIRECTORY_ANGULAR_PROJECT"
    echo "  + Check files changed for the master branch with 'git show' and 'git log'"
    echo "  + Check files changed for the release/$JAVASCRIPT_VERSION branch with 'git show' and 'git log'"
    echo "  + Check the existence of the tag for '$JAVASCRIPT_VERSION' with 'git tag'"
    echo "  + If the check is okay, upload the release to github.com. For the master do 'git push', for the branch do 'git push --set-upstream origin', and for the tag do 'git push origin $JAVASCRIPT_VERSION'"

    notify "dolphin-platform-polymer and dolphin-platform-angular $JAVASCRIPT_VERSION" "Release prepared"
elif [ $PACKAGE -eq 3 ]; then
    release_java_examples $JAVASCRIPT_VERSION $JAVA_VERSION

    echo "- Please check the examples manually in $DIRECTORY_MAIN_PROJECT/platform-examples, because of Bower they are many files changed and maybe not added to version control"
    echo "  + Add changes to the CHANGELOG in $DIRECTORY_MAIN_PROJECT/documentation/src/docs/asciidoc/changelog.adoc"
    echo "  + Add all files with 'git add -A'"
    echo "  + Do a commit for updated examples with 'commit -m \"Examples and Changelog updated for $JAVA_VERSION\""

    notify "dolphin-platform-examples $JAVA_VERSION" "Release prepared"
elif [ $PACKAGE -eq 4 ]; then
    release_java $JAVASCRIPT_VERSION $JAVA_VERSION

    echo "- Please check the new release manually in $DIRECTORY_MAIN_PROJECT"
    echo "  + Check files changed for the master branch with 'git show' and 'git log'"
    echo "  + Check files changed for the release/$JAVA_VERSION branch with 'git show' and 'git log'"
    echo "  + Check the existence of the tag for '$JAVA_VERSION' with 'git tag'"
    echo "  + If the check is okay, upload the release to github.com. For the master do 'git push', for the branch do 'git push --set-upstream origin', and for the tag do 'git push origin $JAVA_VERSION'"
    echo "  + Upload at Bintray with './gradlew clean bintrayUpload'"

    notify "dolphin-platform $JAVA_VERSION" "Release prepared"
fi

cd $SCRIPT_DIR

echo "Finished!"
