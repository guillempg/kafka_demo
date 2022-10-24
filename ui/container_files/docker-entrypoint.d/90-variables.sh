#!/bin/ash
# shellcheck shell=dash
# shellcheck disable=SC2169
# shellcheck disable=SC3010

set -eo pipefail

[[ -n "$BACKEND_URL" ]] && (
  sed -i -e "s!@BACKEND_URL@!${BACKEND_URL}!g" /app/build/index.html
  echo "BACKEND_URL: ${BACKEND_URL}"
)
[[ -n "$REACT_APP_VERSION" ]] && (
  sed -i -e "s!@REACT_APP_VERSION@!${REACT_APP_VERSION}!g" /app/build/index.html
  echo "REACT_APP_VERSION: ${REACT_APP_VERSION}"
)
[[ -n "$OIDC_CALLBACK_URL" ]] && (
  sed -i -e "s!@OIDC_CALLBACK_URL@!${OIDC_CALLBACK_URL}!g" /app/build/index.html
  echo "OIDC_CALLBACK_URL: ${OIDC_CALLBACK_URL}"
)
[[ -n "$OIDC_AUTHORITY" ]] && (
  sed -i -e "s!@OIDC_AUTHORITY@!${OIDC_AUTHORITY}!g" /app/build/index.html
  echo "OIDC_AUTHORITY: ${OIDC_AUTHORITY}"
)
[[ -n "$OIDC_CLIENT_ID" ]] && (
  sed -i -e "s!@OIDC_CLIENT_ID@!${OIDC_CLIENT_ID}!g" /app/build/index.html
  echo "OIDC_CLIENT_ID: ${OIDC_CLIENT_ID}"
)
[[ -n "$BUSINESS_VERSION" ]] && (
  sed -i -e "s!@BUSINESS_VERSION@!${BUSINESS_VERSION}!g" /app/build/index.html
  echo "BUSINESS_VERSION: ${BUSINESS_VERSION}"
)
[[ -n "$NET_DANIA_URL" ]] && (
  sed -i -e "s!@NET_DANIA_URL@!${NET_DANIA_URL}!g" /app/build/index.html
  echo "NET_DANIA_URL: ${NET_DANIA_URL}"
)
[[ -n "$CS_POLICY" ]] && (
  sed -i -e "s!@CS_POLICY@!${CS_POLICY}!g" /etc/nginx/conf.d/default.conf
  echo "CS_POLICY: ${CS_POLICY}"
)

exit 0
