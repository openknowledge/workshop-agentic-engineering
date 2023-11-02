#!/bin/bash
# PostToolUse hook: runs Spotless and Checkstyle on edited Java files
# Exits non-zero if either fails, which tells Claude to fix the issues

INPUT=$(cat)
FILE=$(printf '%s' "$INPUT" | node -e "let d='';process.stdin.on('data',c=>d+=c);process.stdin.on('end',()=>{try{console.log(JSON.parse(d).tool_input.file_path)}catch(e){}})")

if [ -z "$FILE" ]; then
  exit 0
fi

# Normalize Windows backslashes to forward slashes
FILE="${FILE//\\//}"

# Format and check Java files
case "$FILE" in
  */basisprojekt-server/src/*.java)
    # Derive basisprojekt-server directory from the file path
    SERVER_DIR="${FILE%%/basisprojekt-server/src/*}/basisprojekt-server"
    cd "$SERVER_DIR" 2>/dev/null || exit 0

    # Step 1: Auto-format with Spotless
    SPOTLESS_OUTPUT=$(./mvnw spotless:apply -q 2>&1)
    if [ $? -ne 0 ]; then
      echo "$SPOTLESS_OUTPUT"
      echo "Spotless formatting failed for $FILE. Fix before continuing."
      exit 1
    fi

    # Step 2: Check with Checkstyle (cannot auto-fix, must be fixed manually)
    CHECKSTYLE_OUTPUT=$(./mvnw checkstyle:check -Dcheckstyle.config.location=src/main/checkstyle/configuration.xml -q 2>&1)
    if [ $? -ne 0 ]; then
      echo "$CHECKSTYLE_OUTPUT"
      echo "Checkstyle errors found. Fix before continuing."
      exit 1
    fi
    ;;
esac

exit 0
