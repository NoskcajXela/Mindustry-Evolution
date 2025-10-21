#!/bin/bash

# Mindustry Controller API Deployment Script
# This script helps package and deploy the Controller API

set -e

echo "=== Mindustry Controller API Deployment ==="
echo

# Configuration
MINDUSTRY_DIR="/home/v841657/linux_home/Mindustry"
BUILD_DIR="$MINDUSTRY_DIR/build/controller-api"
PACKAGE_NAME="mindustry-controller-api"
VERSION="1.0.0"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check prerequisites
check_prerequisites() {
    log_info "Checking prerequisites..."
    
    if [ ! -d "$MINDUSTRY_DIR" ]; then
        log_error "Mindustry directory not found: $MINDUSTRY_DIR"
        exit 1
    fi
    
    if ! command -v java &> /dev/null; then
        log_error "Java not found. Please install Java 11 or higher."
        exit 1
    fi
    
    log_success "Prerequisites checked"
}

# Validate API files
validate_files() {
    log_info "Validating Controller API files..."
    
    local files=(
        "core/src/mindustry/api/ControllerAPI.java"
        "core/src/mindustry/api/impl/ControllerAPIImpl.java"
        "core/src/mindustry/api/HeadlessControllerLauncher.java"
        "server/src/mindustry/server/ControllerServerLauncher.java"
        "core/src/mindustry/examples/ControllerAPIExample.java"
        "CONTROLLER_API.md"
        "CONTROLLER_API_USAGE.md"
    )
    
    local missing=0
    for file in "${files[@]}"; do
        if [ ! -f "$MINDUSTRY_DIR/$file" ]; then
            log_error "Missing file: $file"
            missing=$((missing + 1))
        fi
    done
    
    if [ $missing -gt 0 ]; then
        log_error "$missing files are missing. Cannot proceed."
        exit 1
    fi
    
    log_success "All Controller API files validated"
}

# Create build directory
setup_build_dir() {
    log_info "Setting up build directory..."
    
    rm -rf "$BUILD_DIR"
    mkdir -p "$BUILD_DIR"
    mkdir -p "$BUILD_DIR/src"
    mkdir -p "$BUILD_DIR/docs"
    mkdir -p "$BUILD_DIR/examples"
    mkdir -p "$BUILD_DIR/scripts"
    
    log_success "Build directory created: $BUILD_DIR"
}

# Copy API files
copy_api_files() {
    log_info "Copying Controller API files..."
    
    # Copy source files
    cp -r "$MINDUSTRY_DIR/core/src/mindustry/api" "$BUILD_DIR/src/"
    
    # Copy server files
    mkdir -p "$BUILD_DIR/src/server"
    cp "$MINDUSTRY_DIR/server/src/mindustry/server/ControllerServerLauncher.java" "$BUILD_DIR/src/server/"
    cp "$MINDUSTRY_DIR/server/src/mindustry/server/ControllerServerControl.java" "$BUILD_DIR/src/server/"
    
    # Copy examples
    cp "$MINDUSTRY_DIR/core/src/mindustry/examples/ControllerAPIExample.java" "$BUILD_DIR/examples/"
    cp "$MINDUSTRY_DIR/core/src/mindustry/test/ControllerAPITest.java" "$BUILD_DIR/examples/"
    
    # Copy documentation
    cp "$MINDUSTRY_DIR/CONTROLLER_API.md" "$BUILD_DIR/docs/"
    cp "$MINDUSTRY_DIR/CONTROLLER_API_USAGE.md" "$BUILD_DIR/docs/"
    
    log_success "API files copied"
}

# Create package info
create_package_info() {
    log_info "Creating package information..."
    
    cat > "$BUILD_DIR/README.md" << EOF
# Mindustry Controller API v$VERSION

This package contains the Mindustry Controller API, which enables headless operation and programmatic control of Mindustry games.

## What's Included

### Core API
- \`src/api/\` - Main API interfaces and implementations
- \`src/server/\` - Enhanced server components with API integration

### Documentation
- \`docs/CONTROLLER_API.md\` - Complete API reference and architecture
- \`docs/CONTROLLER_API_USAGE.md\` - Usage guide with practical examples

### Examples
- \`examples/ControllerAPIExample.java\` - Comprehensive usage example
- \`examples/ControllerAPITest.java\` - Integration test

### Scripts
- \`scripts/run-headless.sh\` - Run headless server with API
- \`scripts/run-example.sh\` - Run the example program

## Quick Start

1. Copy the API files to your Mindustry installation
2. Run the headless server: \`./scripts/run-headless.sh\`
3. Or run the example: \`./scripts/run-example.sh\`

## Key Features

- **Headless Operation**: Run Mindustry without graphics
- **AI Player Control**: Create and manage AI players programmatically
- **World Manipulation**: Build structures and modify terrain via code
- **Event System**: React to game events with custom handlers
- **Server Integration**: Enhanced server with API commands
- **Thread Safety**: Safe for multi-threaded usage

## Requirements

- Java 11 or higher
- Mindustry game files
- Arc framework (included with Mindustry)

## Installation

1. Extract this package to your Mindustry directory
2. Copy the API files to appropriate locations:
   - \`src/api/\` → \`core/src/mindustry/api/\`
   - \`src/server/\` → \`server/src/mindustry/server/\`
3. Rebuild Mindustry or add to classpath

## Support

For issues and questions, refer to the documentation in the \`docs/\` directory.

Built on $(date)
EOF

    # Create package manifest
    cat > "$BUILD_DIR/MANIFEST.txt" << EOF
Mindustry Controller API Package
Version: $VERSION
Build Date: $(date)
Build System: $(uname -a)
Java Version: $(java -version 2>&1 | head -n 1)

Files included:
$(find "$BUILD_DIR" -type f | sed "s|$BUILD_DIR/||" | sort)
EOF

    log_success "Package information created"
}

# Create utility scripts
create_scripts() {
    log_info "Creating utility scripts..."
    
    # Headless runner script
    cat > "$BUILD_DIR/scripts/run-headless.sh" << 'EOF'
#!/bin/bash
# Run Mindustry headless server with Controller API

echo "Starting Mindustry Headless Server with Controller API..."

# Set Java options for headless operation
JAVA_OPTS="-Djava.awt.headless=true -Xmx2G -XX:+UseG1GC"

# Run the enhanced server launcher
java $JAVA_OPTS -cp ".:../core/build/libs/*:../server/build/libs/*" \
    mindustry.server.ControllerServerLauncher --enable-api "$@"
EOF

    # Example runner script
    cat > "$BUILD_DIR/scripts/run-example.sh" << 'EOF'
#!/bin/bash
# Run the Controller API example

echo "Running Controller API Example..."

# Compile and run the example
javac -cp ".:../core/build/libs/*" examples/ControllerAPIExample.java
java -cp ".:../core/build/libs/*:examples" ControllerAPIExample
EOF

    # Test runner script
    cat > "$BUILD_DIR/scripts/run-tests.sh" << 'EOF'
#!/bin/bash
# Run Controller API tests

echo "Running Controller API Tests..."

# Compile and run tests
javac -cp ".:../core/build/libs/*" examples/ControllerAPITest.java
java -cp ".:../core/build/libs/*:examples" mindustry.test.ControllerAPITest
EOF

    # Make scripts executable
    chmod +x "$BUILD_DIR/scripts/"*.sh
    
    log_success "Utility scripts created"
}

# Create archive
create_archive() {
    log_info "Creating deployment archive..."
    
    cd "$(dirname "$BUILD_DIR")"
    tar -czf "${PACKAGE_NAME}-v${VERSION}.tar.gz" "$(basename "$BUILD_DIR")"
    
    local archive_path="$(pwd)/${PACKAGE_NAME}-v${VERSION}.tar.gz"
    local archive_size=$(du -h "$archive_path" | cut -f1)
    
    log_success "Archive created: $archive_path ($archive_size)"
    echo "  Archive location: $archive_path"
    echo "  Archive size: $archive_size"
}

# Generate file checksums
generate_checksums() {
    log_info "Generating checksums..."
    
    cd "$(dirname "$BUILD_DIR")"
    local archive_name="${PACKAGE_NAME}-v${VERSION}.tar.gz"
    
    # Generate checksums
    md5sum "$archive_name" > "${archive_name}.md5"
    sha256sum "$archive_name" > "${archive_name}.sha256"
    
    log_success "Checksums generated:"
    echo "  MD5: $(cat "${archive_name}.md5")"
    echo "  SHA256: $(cat "${archive_name}.sha256")"
}

# Deployment summary
show_summary() {
    echo
    log_success "=== Deployment Complete ==="
    echo "Controller API v$VERSION has been packaged successfully!"
    echo
    echo "Package contents:"
    echo "  📁 Source code: $(find "$BUILD_DIR/src" -name "*.java" | wc -l) Java files"
    echo "  📄 Documentation: $(find "$BUILD_DIR/docs" -name "*.md" | wc -l) documents"
    echo "  🔧 Examples: $(find "$BUILD_DIR/examples" -name "*.java" | wc -l) example files"
    echo "  🚀 Scripts: $(find "$BUILD_DIR/scripts" -name "*.sh" | wc -l) utility scripts"
    echo
    echo "Next steps:"
    echo "  1. Extract the archive to your Mindustry installation"
    echo "  2. Follow the README.md for installation instructions"
    echo "  3. Run ./scripts/run-headless.sh to start the API server"
    echo "  4. See docs/CONTROLLER_API_USAGE.md for usage examples"
    echo
    echo "Archive: $(dirname "$BUILD_DIR")/${PACKAGE_NAME}-v${VERSION}.tar.gz"
}

# Main deployment process
main() {
    echo "Starting Controller API deployment process..."
    echo
    
    check_prerequisites
    validate_files
    setup_build_dir
    copy_api_files
    create_package_info
    create_scripts
    create_archive
    generate_checksums
    show_summary
    
    log_success "Deployment completed successfully!"
}

# Run if executed directly
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi
