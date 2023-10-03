#!/bin/sh

SYSCONF_DIR="/etc/qbee"
CONF_DIR="/data/qbee/etc"

if [ -f "$CONF_DIR/qbee-agent.json" ]; then
  # Assume we have bootstrapped
  exit 0
fi

if [ ! -f "$SYSCONF_DIR/ppkeys/.bootstrap-env" ]; then
  # No bootstrap env
  exit 0 
fi

# shellcheck disable=SC1090
. "$SYSCONF_DIR/ppkeys/.bootstrap-env"

mkdir -p "$CONF_DIR/ppkeys"
chmod 700 "$CONF_DIR/ppkeys"
cp -a "$SYSCONF_DIR/ppkeys/ca.cert" "$CONF_DIR/ppkeys"
chmod 600 "$CONF_DIR/ppkeys/ca.cert"

if [ -n "$BOOTSTRAP_KEY" ]; then
  printf '{"bootstrap_key":"%s"' "$BOOTSTRAP_KEY" > "$CONF_DIR/qbee-agent.json"
fi

if [ "$DEVICE_NAME_TYPE" = "machine-id" ]; then
  device_name=$(cat /etc/machine-id)
  printf ',"device_name":"%s"' "$device_name" >> "$CONF_DIR/qbee-agent.json"
elif [ "$DEVICE_NAME_TYPE" = "mac-address" ]; then
  default_device=$(ip route | awk '/default/{print $5}')
  device_name=$(ip link show "$default_device" | awk '/ether/{print $2}')  
  printf ',"device_name":"%s"' "$device_name" >> "$CONF_DIR/qbee-agent.json" 
else
  echo "unsupported"
fi

if [ -n "$DEVICE_HUB_HOST" ]; then
  printf ',"server":"%s"' "$DEVICE_HUB_HOST" >> "$CONF_DIR/qbee-agent.json"
fi

if [ -n "$VPN_SERVER" ]; then
  printf ',"vpn_server":"%s"' "$VPN_SERVER" >> "$CONF_DIR/qbee-agent.json"
fi

printf "}" >> "$CONF_DIR/qbee-agent.json"

