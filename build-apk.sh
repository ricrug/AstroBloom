#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

BUILD_TYPE="debug"
BUMP="build"
CLEAN=false
INSTALL=false
NO_GIT_PUSH=false
COMMIT_MSG=""

while [[ "$#" -gt 0 ]]; do
    case $1 in
        --release|-r) BUILD_TYPE="release" ;;
        --debug|-d) BUILD_TYPE="debug" ;;
        --bump) BUMP="$2"; shift ;;
        --clean|-c) CLEAN=true ;;
        --install|-i) INSTALL=true ;;
        --no-git-push) NO_GIT_PUSH=true ;;
        --commit-msg|-m) COMMIT_MSG="$2"; shift ;;
        *) echo "Parametro sconosciuto: $1"; exit 1 ;;
    esac
    shift
done

VERSION_FILE="$SCRIPT_DIR/version.properties"
MAJOR=1
MINOR=0
PATCH=0
BUILD=1
CODE=1

if [ -f "$VERSION_FILE" ]; then
    while IFS='=' read -r key value || [ -n "$key" ]; do
        key=$(echo "$key" | tr -d '[:space:]')
        value=$(echo "$value" | tr -d '[:space:]')
        case "$key" in
            VERSION_MAJOR) MAJOR="$value" ;;
            VERSION_MINOR) MINOR="$value" ;;
            VERSION_PATCH) PATCH="$value" ;;
            VERSION_BUILD) BUILD="$value" ;;
            VERSION_CODE) CODE="$value" ;;
        esac
    done < "$VERSION_FILE"
fi

case "$BUMP" in
    build)
        BUILD=$((BUILD + 1))
        CODE=$((CODE + 1))
        ;;
    patch)
        PATCH=$((PATCH + 1))
        BUILD=$((BUILD + 1))
        CODE=$((CODE + 1))
        ;;
    minor)
        MINOR=$((MINOR + 1))
        PATCH=0
        BUILD=$((BUILD + 1))
        CODE=$((CODE + 1))
        ;;
    major)
        MAJOR=$((MAJOR + 1))
        MINOR=0
        PATCH=0
        BUILD=$((BUILD + 1))
        CODE=$((CODE + 1))
        ;;
esac

VERSION_NAME="$MAJOR.$MINOR.$PATCH"

cat <<EOF > "$VERSION_FILE"
# AstroViaggio Version Configuration
VERSION_MAJOR=$MAJOR
VERSION_MINOR=$MINOR
VERSION_PATCH=$PATCH
VERSION_BUILD=$BUILD
VERSION_CODE=$CODE
VERSION_NAME=$VERSION_NAME
EOF

echo "📋 Nuova versione: v$VERSION_NAME (Code: $CODE, Build: $BUILD)"
echo "🎯 Tipo build:     $BUILD_TYPE"

GRADLEW="./gradlew"
if [ ! -f "$GRADLEW" ]; then
    GRADLEW="gradle"
fi

CAP_TYPE="$(tr '[:lower:]' '[:upper:]' <<< "${BUILD_TYPE:0:1}")${BUILD_TYPE:1}"
GRADLE_TASKS=()
if [ "$CLEAN" = true ]; then
    GRADLE_TASKS+=("clean")
fi
GRADLE_TASKS+=("assemble$CAP_TYPE")

$GRADLEW "${GRADLE_TASKS[@]}"

APK_DIR="$SCRIPT_DIR/app/build/outputs/apk/$BUILD_TYPE"
SOURCE_APK=$(find "$APK_DIR" -maxdepth 1 -name "*.apk" ! -name "*-unaligned.apk" | head -n 1)

if [ -z "$SOURCE_APK" ]; then
    echo "❌ APK non trovato in $APK_DIR"
    exit 1
fi

DIST_DIR="$SCRIPT_DIR/dist"
mkdir -p "$DIST_DIR"
DEST_APK="$DIST_DIR/AstroViaggio-v${VERSION_NAME}-b${CODE}-${BUILD_TYPE}.apk"
cp "$SOURCE_APK" "$DEST_APK"
cp "$SOURCE_APK" "$DIST_DIR/AstroViaggio-${BUILD_TYPE}-latest.apk"

echo "=========================================================="
echo " 🎉 BUILD COMPLETATA CON SUCCESSO!"
echo " 📦 APK:      $DEST_APK"
echo " 🏷️  Versione: v$VERSION_NAME (Code: $CODE)"
echo "=========================================================="

if [ "$INSTALL" = true ]; then
    if command -v adb >/dev/null 2>&1; then
        adb install -r "$DEST_APK"
    else
        echo "⚠️ ADB non trovato, impossibile installare."
    fi
fi

# Aggiornamento automatico del repository GitHub ad ogni build
if [ "$NO_GIT_PUSH" != true ]; then
    echo "=========================================================="
    echo " 🐙 Sincronizzazione Repository GitHub"
    echo "=========================================================="
    if command -v git &> /dev/null; then
        if [ -n "$(git status --porcelain)" ]; then
            git add -A
            MSG="${COMMIT_MSG:-build: aggiornamento automatico v$VERSION_NAME (code $CODE, build $BUILD)}"
            git commit -m "$MSG"
        fi
        CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo "main")
        if [ "$CURRENT_BRANCH" = "HEAD" ]; then
            CURRENT_BRANCH="main"
        fi
        if git push origin "$CURRENT_BRANCH"; then
            echo "✅ Repository GitHub aggiornato con successo su origin/$CURRENT_BRANCH!"
        else
            echo "⚠️ Attenzione: push su GitHub non riuscito."
        fi
    fi
fi
