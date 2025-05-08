#!/usr/bin/env bash

# Script to manage a multi-step release process:
# 1. If current version is X.Y.Z-SNAPSHOT:
#    - Calculates release version X.Y.Z
#    - Sets version to X.Y.Z, commits, and pushes this commit.
# 2. Calculates suggested next snapshot (e.g., X.Y.(Z+1)-SNAPSHOT).
# 3. Prompts developer for the actual next snapshot version (with suggestion as default).
# 4. Validates the developer's input (must be higher semver, must end in -SNAPSHOT).
# 5. Sets version to the chosen snapshot, commits (DOES NOT PUSH this commit).
# 6. Instructs user to:
#    a) Create GitHub release/tag for X.Y.Z (pointing to the pushed release commit).
#    b) After GitHub release actions complete, manually push the second (snapshot) commit.

set -e # Exit immediately if a command exits with a non-zero status.

# --- Check for required commands ---
if ! command -v mvn &>/dev/null; then
    echo "Error: mvn (Maven) command not found. Please install Maven and ensure it's in your PATH."
    exit 1
fi

if ! command -v git &>/dev/null; then
    echo "Error: git command not found. Please install Git and ensure it's in your PATH."
    exit 1
fi

# --- Helper function to compare semver versions (simplified: A > B) ---
# Returns 0 if v1 > v2, 1 otherwise.
# Assumes versions are like X.Y.Z or X.Y.Z-SNAPSHOT. Ignores -SNAPSHOT for gt comparison.
version_gt() {
    local v1=${1%-SNAPSHOT} # remove -SNAPSHOT if present
    local v2=${2%-SNAPSHOT} # remove -SNAPSHOT if present

    IFS='.' read -r -a v1_parts <<<"$v1"
    IFS='.' read -r -a v2_parts <<<"$v2"

    # Pad with zeros if parts are missing (e.g. 0.1 vs 0.1.0)
    for i in 0 1 2; do
        v1_parts[i]=${v1_parts[i]:-0}
        v2_parts[i]=${v2_parts[i]:-0}
    done

    for i in 0 1 2; do
        if ((v1_parts[i] > v2_parts[i])); then
            return 0 # v1 > v2
        fi
        if ((v1_parts[i] < v2_parts[i])); then
            return 1 # v1 < v2
        fi
    done
    return 1 # v1 == v2, so not strictly greater
}

# --- Get Current State ---
CURRENT_VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
if [ -z "${CURRENT_VERSION}" ]; then
    echo "Error: Could not determine current project version using Maven."
    exit 1
fi
echo "Current project version: ${CURRENT_VERSION}"

# --- Validate Current Version and Arguments ---
if [[ "${CURRENT_VERSION}" != *"-SNAPSHOT" ]]; then
    echo "Error: Current version (${CURRENT_VERSION}) is not a SNAPSHOT version."
    echo "This script must be run on a SNAPSHOT version to prepare a release."
    echo "If a release was just made, you might need to push the latest (snapshot) commit manually."
    exit 1
fi

if [ ! -z "$1" ]; then
    echo "Error: This script does not accept arguments."
    echo "       The release version is calculated, and the next snapshot version will be prompted."
    exit 1
fi

RELEASE_VERSION=${CURRENT_VERSION%-SNAPSHOT} # Remove -SNAPSHOT
COMMIT_MSG_RELEASE="prepare release ${RELEASE_VERSION}"

echo "Calculated release version: ${RELEASE_VERSION}"

# --- Confirmation for Phase 1 (Prepare Release) ---
echo
echo "-------------------- ACTION SUMMARY (Phase 1/2) --------------------"
echo "Current version is SNAPSHOT (${CURRENT_VERSION})."
echo "1. Will set version to RELEASE: ${RELEASE_VERSION}"
echo "   Will commit changes with message: \"${COMMIT_MSG_RELEASE}\""
echo "2. Will create an annotated tag: '${RELEASE_VERSION}' for this commit."
echo "3. Will PUSH this commit AND the tag ('${RELEASE_VERSION}') to the remote repository."
echo "-------------------------------------------------------------------"
echo

read -p "Do you want to proceed with Phase 1 (Prepare, Tag, and Push Release)? (y/N): " confirmation
if [[ ! "$confirmation" =~ ^[Yy]$ ]]; then
    echo "Operation cancelled by user."
    exit 0
fi

# --- Execute Phase 1 Actions ---

# 1. Check for uncommitted changes
if ! git diff --quiet HEAD --; then
    echo "Error: Uncommitted changes detected. Please commit or stash them first."
    exit 1
fi

# 2. Set version to RELEASE_VERSION
echo "Setting POM versions to ${RELEASE_VERSION}... (This may take a moment)"
mvn versions:set -DnewVersion="${RELEASE_VERSION}" -DprocessAllModules=true -DgenerateBackupPoms=false

# 3. Commit the release version change
echo "Staging pom.xml files for release commit..."
find . -name pom.xml -type f -exec git add {} +
echo "Committing release version change: \"${COMMIT_MSG_RELEASE}\" ..."
git commit -m "${COMMIT_MSG_RELEASE}"
echo "Release version ${RELEASE_VERSION} committed."

# 4. Create an annotated tag for the release
echo "Creating annotated tag '${RELEASE_VERSION}'..."
git tag -a "${RELEASE_VERSION}" -m "Release ${RELEASE_VERSION}"
echo "Tag '${RELEASE_VERSION}' created."

# 5. Push the release commit and the tag
echo "Pushing release commit and tag ('${RELEASE_VERSION}') to remote..."
git push --follow-tags
echo "Release commit and tag pushed successfully."
echo "-------------------------------------------------------------------"
echo

# --- Phase 2: Prepare Next Development Snapshot ---

# Calculate SUGGESTED_NEXT_SNAPSHOT_VERSION (e.g., from 0.1.1 to 0.1.2-SNAPSHOT)
IFS='.' read -r -a VERSION_PARTS <<<"${RELEASE_VERSION}"
MAJOR_VERSION=${VERSION_PARTS[0]}
MINOR_VERSION=${VERSION_PARTS[1]}
PATCH_VERSION=${VERSION_PARTS[2]}

if [ -z "$PATCH_VERSION" ]; then # Should not happen if RELEASE_VERSION was valid
    echo "Error: Could not parse patch version from ${RELEASE_VERSION}."
    echo "Ensure version is in format X.Y.Z."
    exit 1
fi

NEXT_PATCH_VERSION=$((PATCH_VERSION + 1))
SUGGESTED_NEXT_SNAPSHOT_VERSION="${MAJOR_VERSION}.${MINOR_VERSION}.${NEXT_PATCH_VERSION}-SNAPSHOT"

# Prompt for next snapshot version
echo
echo "-------------------- ACTION SUMMARY (Phase 2/2) --------------------"
echo "The release version ${RELEASE_VERSION} has been committed and pushed."
echo
echo "Now, let's prepare for the next development iteration."
NEXT_SNAPSHOT_VERSION=""
while true; do
    printf "Enter the next snapshot version (e.g., %s): " "${SUGGESTED_NEXT_SNAPSHOT_VERSION}"
    read USER_NEXT_SNAPSHOT_VERSION

    if [ -z "${USER_NEXT_SNAPSHOT_VERSION}" ]; then
        USER_NEXT_SNAPSHOT_VERSION="${SUGGESTED_NEXT_SNAPSHOT_VERSION}"
        echo "No input provided, using suggested: ${USER_NEXT_SNAPSHOT_VERSION}"
    fi

    # Ensure it ends with -SNAPSHOT
    if [[ "${USER_NEXT_SNAPSHOT_VERSION}" != *"-SNAPSHOT" ]]; then
        echo "Warning: The version '${USER_NEXT_SNAPSHOT_VERSION}' does not end with -SNAPSHOT. Appending it."
        USER_NEXT_SNAPSHOT_VERSION="${USER_NEXT_SNAPSHOT_VERSION}-SNAPSHOT"
        echo "Updated to: ${USER_NEXT_SNAPSHOT_VERSION}"
    fi

    # Validate semver is greater
    if version_gt "${USER_NEXT_SNAPSHOT_VERSION}" "${RELEASE_VERSION}"; then
        NEXT_SNAPSHOT_VERSION=${USER_NEXT_SNAPSHOT_VERSION}
        break
    else
        echo "Error: Next snapshot version ('${USER_NEXT_SNAPSHOT_VERSION%-SNAPSHOT}') must be greater than the release version ('${RELEASE_VERSION}')."
        echo "Please provide a valid higher version."
        # Reset suggested for next loop iteration if user input was bad
        SUGGESTED_NEXT_SNAPSHOT_VERSION=${USER_NEXT_SNAPSHOT_VERSION}
    fi
done

COMMIT_MSG_NEXT_DEV="prepare for next development iteration (${NEXT_SNAPSHOT_VERSION})"

echo
echo "Will set version to NEXT SNAPSHOT: ${NEXT_SNAPSHOT_VERSION}"
echo "Will commit changes with message: \"${COMMIT_MSG_NEXT_DEV}\""
echo "IMPORTANT: This commit will NOT be pushed by the script."
echo "-------------------------------------------------------------------"
echo

read -p "Do you want to proceed with Phase 2 (Prepare Next Snapshot locally)? (y/N): " confirmation_phase2
if [[ ! "$confirmation_phase2" =~ ^[Yy]$ ]]; then
    echo "Operation cancelled by user before Phase 2."
    exit 0
fi

# Set version to NEXT_SNAPSHOT_VERSION
echo "Setting POM versions to ${NEXT_SNAPSHOT_VERSION}... (This may take a moment)"
mvn versions:set -DnewVersion="${NEXT_SNAPSHOT_VERSION}" -DprocessAllModules=true -DgenerateBackupPoms=false

# Commit the next snapshot version change
echo "Staging pom.xml files for next development iteration commit..."
find . -name pom.xml -type f -exec git add {} +
echo "Committing next snapshot version change: \"${COMMIT_MSG_NEXT_DEV}\" ..."
git commit -m "${COMMIT_MSG_NEXT_DEV}"
echo "Next snapshot version ${NEXT_SNAPSHOT_VERSION} committed locally."

# Output Final Instructions
echo
echo "----------------------------- ACTION REQUIRED ------------------------------"
echo "Local repository updated. Two commits were involved:"
echo "  1. \"${COMMIT_MSG_RELEASE}\" (version ${RELEASE_VERSION}) - This commit AND the tag '${RELEASE_VERSION}' WERE PUSHED."
echo "  2. \"${COMMIT_MSG_NEXT_DEV}\" (version ${NEXT_SNAPSHOT_VERSION}) - This commit IS LOCAL."
echo
echo "NEXT STEPS:"
echo "1. Go to GitHub. The tag '${RELEASE_VERSION}' has been pushed."
echo "   - A GitHub Release might have been automatically created from this tag, or you may need to"
echo "     draft and publish a new release using the '${RELEASE_VERSION}' tag."
echo "   - Add release notes as appropriate."
echo "   - Wait for any automated release build/deployment processes triggered by this tag/release to complete successfully."
echo
echo "2. AFTER the GitHub release is published and any associated CI/CD is successful, push the local snapshot commit:"
echo "   git push"
echo "   (This will push the commit \"${COMMIT_MSG_NEXT_DEV}\" for version ${NEXT_SNAPSHOT_VERSION})"
echo "---------------------------------------------------------------------------"
