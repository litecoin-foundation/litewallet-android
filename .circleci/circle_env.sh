#!/usr/bin/env bash

function copyEnvironmentVariablesToGradleProperties {
    DIR="$(pwd)"
    GRADLE_PROPERTIES=${DIR}"/gradle.properties"
    export GRADLE_PROPERTIES
    echo "Gradle Properties should exist at $GRADLE_PROPERTIES"

    if [[ ! -f "$GRADLE_PROPERTIES" ]]; then
        echo "Gradle Properties does not exist"

        echo "Creating Gradle Properties file..."
        touch $GRADLE_PROPERTIES

        echo "Writing keys to gradle.properties..."
        echo "RELEASE_KEYSTORE_FILE=$RELEASE_KEYSTORE_FILE" >> ${GRADLE_PROPERTIES}
        echo "RELEASE_KEYSTORE_PASSWORD=$RELEASE_KEYSTORE_PASSWORD" >> ${GRADLE_PROPERTIES}
        echo "RELEASE_KEY_ALIAS=$RELEASE_KEY_ALIAS" >> ${GRADLE_PROPERTIES}
        echo "RELEASE_KEY_PASSWORD=$RELEASE_KEY_PASSWORD" >> ${GRADLE_PROPERTIES}
    fi
}



function fetchKeystore {
    sudo gpg --passphrase ${RELEASE_KEYSTORE_ENCRYPTION_KEY} --pinentry-mode loopback -o "app/$RELEASE_KEYSTORE_FILE" -d  "upload-binary/$RELEASE_KEYSTORE_FILE.gpg"
}