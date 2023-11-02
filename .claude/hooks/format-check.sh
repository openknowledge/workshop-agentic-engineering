#!/bin/bash
# PostToolUse hook: runs Prettier on edited TypeScript/React/CSS files
# Hook input is JSON on stdin with tool_input.file_path

INPUT=$(cat)
FILE=$(printf '%s' "$INPUT" | node -e "let d='';process.stdin.on('data',c=>d+=c);process.stdin.on('end',()=>{try{console.log(JSON.parse(d).tool_input.file_path)}catch(e){}})")

if [ -z "$FILE" ]; then
  exit 0
fi

# Normalize Windows backslashes to forward slashes
FILE="${FILE//\\//}"

# Format TypeScript/React/CSS files with Prettier
case "$FILE" in
  */basisprojekt-client/src/*.ts|*/basisprojekt-client/src/*.tsx|*/basisprojekt-client/src/*.css)
    CLIENT_DIR="${FILE%%/basisprojekt-client/src/*}/basisprojekt-client"
    cd "$CLIENT_DIR" 2>/dev/null || exit 0
    npx prettier --write "$FILE" 2>/dev/null
    ;;
esac

exit 0
