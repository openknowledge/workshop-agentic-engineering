#!/bin/bash

# Prompt for username
read -p "Username: " username

# Prompt for password (hidden)
read -sp "Password: " password
echo  # Add newline after password prompt

# Generate the apr1 hash
hash=$(echo "$password" | openssl passwd -apr1 -stdin)

# Escape all $ characters by replacing them with $$
escaped_hash=$(echo "$hash" | sed 's/\$/\$\$/g')

# Output the result
echo "${username}:${escaped_hash}"
