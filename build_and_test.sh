#!/usr/bin/env bash
set -e

DIST_DIR="$(pwd)/dist"
mkdir -p "$DIST_DIR"

resolve_module() {
    case "$1" in
        1.18.2|forge-1.18.2) echo "forge-1.18.2" ;;
        1.19.2|forge-1.19.2) echo "forge-1.19.2" ;;
        1.19.4|forge-1.19.4) echo "forge-1.19.4" ;;
        1.20.1|forge-1.20.1) echo "forge-1.20.1" ;;
        1.20.4|neoforge-1.20.4) echo "neoforge-1.20.4" ;;
        1.21.1|neoforge-1.21.1) echo "neoforge-1.21.1" ;;
        *) echo "" ;;
    esac
}

build_module() {
    local MODULE_NAME="$1"
    local JAR_NAME="$2"
    local SOURCE_CHECK="$MODULE_NAME/src/main/java/com/skl/jammod/JamMod.java"

    echo ""
    echo ">>> Building $MODULE_NAME..."
    if [ -f "$SOURCE_CHECK" ]; then
        ./gradlew ":$MODULE_NAME:build" -x test --daemon
        find "$MODULE_NAME/build/libs/" -name "*.jar" ! -name "*-sources.jar" ! -name "*-dev.jar" -exec cp {} "$DIST_DIR/$JAR_NAME" \;
        echo "✓ Artifact generated: $DIST_DIR/$JAR_NAME"
    else
        echo "⚠ Skipping $MODULE_NAME (sources not found at $SOURCE_CHECK)"
    fi
}

run_client() {
    local TARGET_VER="$1"
    local MODULE_NAME
    MODULE_NAME="$(resolve_module "$TARGET_VER")"

    if [ -z "$MODULE_NAME" ]; then
        echo "Error: Unknown or missing version '$TARGET_VER'."
        echo "Available versions: 1.18.2 | 1.19.2 | 1.19.4 | 1.20.1 | 1.20.4 | 1.21.1"
        exit 1
    fi

    echo ""
    echo "========================================================"
    echo " Launching Minecraft Client for $MODULE_NAME"
    echo "========================================================"
    ./gradlew ":$MODULE_NAME:runClient"
}

# Command dispatching
CMD="${1:-all}"

case "$CMD" in
    run|runClient)
        TARGET_VER="${2:-1.20.1}"
        run_client "$TARGET_VER"
        exit 0
        ;;
    clean)
        echo "Cleaning all modules..."
        ./gradlew clean
        rm -rf "$DIST_DIR"/*
        echo "Clean completed."
        exit 0
        ;;
    all)
        echo "========================================================"
        echo "      JamMod Multi-Version Build Manager (All)          "
        echo "========================================================"
        build_module "forge-1.20.1" "JamMod-1.20.1-forge.jar"
        build_module "neoforge-1.21.1" "JamMod-1.21.1-neoforge.jar"
        build_module "neoforge-1.20.4" "JamMod-1.20.4-neoforge.jar"
        build_module "forge-1.19.2" "JamMod-1.19.2-forge.jar"
        build_module "forge-1.19.4" "JamMod-1.19.4-forge.jar"
        build_module "forge-1.18.2" "JamMod-1.18.2-forge.jar"
        ;;
    *)
        # Allow syntax like: ./build_and_test.sh 1.21.1 run
        if [ "$2" = "run" ] || [ "$2" = "runClient" ]; then
            run_client "$CMD"
            exit 0
        fi

        MODULE="$(resolve_module "$CMD")"
        if [ -n "$MODULE" ]; then
            echo "========================================================"
            echo "      JamMod Single-Target Build: $MODULE               "
            echo "========================================================"
            JAR_NAME="JamMod-${CMD}-$(echo "$MODULE" | cut -d'-' -f1).jar"
            build_module "$MODULE" "$JAR_NAME"
        else
            echo "Usage:"
            echo "  Build:  ./build_and_test.sh [1.18.2 | 1.19.2 | 1.19.4 | 1.20.1 | 1.20.4 | 1.21.1 | all | clean]"
            echo "  Run:    ./build_and_test.sh run <version>      (e.g., ./build_and_test.sh run 1.21.1)"
            echo "          ./build_and_test.sh <version> run      (e.g., ./build_and_test.sh 1.21.1 run)"
            exit 1
        fi
        ;;
esac

echo ""
echo "========================================================"
echo "Artifacts available in $DIST_DIR:"
ls -lh "$DIST_DIR"/*.jar 2>/dev/null || echo "No final jars copied."
echo "========================================================"