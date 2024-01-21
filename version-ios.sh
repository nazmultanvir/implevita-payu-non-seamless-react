#!/usr/bin/env bash -e

PROJECT_DIR="ios"
INFOPLIST_FILE="PayUBizSdkInfo.plist"
INFOPLIST_DIR="${PROJECT_DIR}/${INFOPLIST_FILE}"

PACKAGE_VERSION=$(cat package.json | grep version | head -1 | awk -F: '{ print $2 }' | sed 's/[\",]//g' | tr -d '[[:space:]]')

#BUILD_NUMBER=$(/usr/libexec/PlistBuddy -c "Print SDKVersion" "${INFOPLIST_DIR}")
#BUILD_NUMBER=$(($BUILD_NUMBER + 1))


# Update plist with new values
/usr/libexec/PlistBuddy -c "Set :SDKVersion ${PACKAGE_VERSION#*v}" "${INFOPLIST_DIR}"

echo "Printing ${INFOPLIST_DIR} file after updation"
cat $INFOPLIST_DIR

git add "${INFOPLIST_DIR}"
