#!/bin/bash
# PostToolUse hook: runs ESLint on edited TypeScript/React files
# Exits non-zero if lint errors found, which tells Claude to fix them

INPUT=$(cat)
FILE=$(printf '%s' "$INPUT" | node -e "let d='';process.stdin.on('data',c=>d+=c);process.stdin.on('end',()=>{try{console.log(JSON.parse(d).tool_input.file_path)}catch(e){}})")

if [ -z "$FILE" ]; then
  exit 0
fi

# Normalize Windows backslashes to forward slashes
FILE="${FILE//\\//}"

# Lint TypeScript/React files with ESLint
case "$FILE" in
  */basisprojekt-client/src/*.ts|*/basisprojekt-client/src/*.tsx)
    CLIENT_DIR="${FILE%%/basisprojekt-client/src/*}/basisprojekt-client"
    cd "$CLIENT_DIR" 2>/dev/null || exit 0
    LINT_OUTPUT=$(npx eslint "$FILE" 2>&1)
    if [ $? -ne 0 ]; then
      echo "$LINT_OUTPUT"
      echo "ESLint errors found in $FILE. Fix before continuing."
      exit 1
    fi
    ;;
esac

exit 0
