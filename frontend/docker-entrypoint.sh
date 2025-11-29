#!/bin/sh

# Replace placeholder with actual env var
if [ -n "$VITE_API_URL" ]; then
  echo "Injecting VITE_API_URL: $VITE_API_URL"
  sed -i "s|VITE_API_URL_PLACEHOLDER|$VITE_API_URL|g" /usr/share/nginx/html/env-config.js
else
  echo "Warning: VITE_API_URL not set, using default"
fi

# Start nginx
exec nginx -g "daemon off;"
