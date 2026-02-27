# meta-qbee
Yocto Project meta layer for the qbee-agent client https://qbee.io

This repository holds the layers for integrating the qbee-agent into a Yocto build

The repository holds to branches based on the minimum supported yocto version
* kirkstone - compatible with kirkstone and above
* dunfell - compatible with dunfell and above

Essentially, the difference is that the kirkstone branch will build the qbee-agent from source, while
the dunfell branch installs pre-compiled static binaries (arm 32/64 and intel 32/64 supported for now)

# Adding the meta-qbee layer

Kirkstone and later
```
git clone -b kirkstone https://github.com/qbee-io/meta-qbee layers/meta-qbee
bitbake-layers add-layer layers/meta-qbee/meta-qbee
```

Dunfell and later
```
git clone -b dunfell https://github.com/qbee-io/meta-qbee layers/meta-qbee
bitbake-layers add-layer layers/meta-qbee/meta-qbee
```

# Build configuration

At build time you can define several qbee related variables to customize how the qbee agent is integrated on your image.
At the bare minimum you would need to define a bootstrap key in your build configuration for the qbee-agent to use
on first boot to authenticate with the qbee backend. 

```
QBEE_BOOTSTRAP_KEY="<bootstrap_key>"
```

Other optional values are:
```
# QBEE_DEVICE_NAME_TYPE (default: "" (eg. using system hostname), valid options: <mac-address|machine-id>)
QBEE_DEVICE_NAME_TYPE=""
# QBEE_DISABLE_REMOTE_ACCESS (default: "" (eg. "false"), valid options are "<false|true>")
QBEE_DISABLE_REMOTE_ACCESS=""
# QBEE_CONF_DIR (default: "/data/qbee/etc")
QBEE_CONF_DIR=""
# QBEE_STATE_DIR (default: "/data/qbee/var")
QBEE_STATE_DIR=""
# QBEE_DEVICE_HUB_HOST (default: "device.app.qbee.io")
QBEE_DEVICE_HUB_HOST=""
# QBEE_CLEAN_SEEDING_INFO (default: "" (eg. false))
QBEE_CLEAN_SEEDING_INFO=""
# QBEE_TPM_DEVICE (default: "" (eg. do not use tpm for qbee secrets). Set path to TPM device if needed and available, eg. "/dev/tpm0")
QBEE_TPM_DEVICE=""
# QBEE_CA_CERT (default: "") for private environments. CA cert will need to be added to image by custom recipe.
QBEE_CA_CERT=""
# QBEE_SYSTEMD_AFTER_TARGET (default: "network.target"). Set variable if you want to control when qbee-agent is started.
QBEE_SYSTEMD_AFTER_TARGET="network.target"
# QBEE_SYSTEMD_WANTS_TARGET (default: "$QBEE_SYSTEMD_AFTER_TARGET"). Set variable if you want to control when qbee-agent is started.
QBEE_SYSTEMD_WANTS_TARGET=""
# the user to run qbee-agent as. If left empty, qbee-agent will run as root. User will be added automatically by the qbee-agent recipe.
QBEE_EXEC_USER ?= ""
# The elevation command to use for privileged operations if QBEE_EXEC_USER is set. Needs to be in json array format, e.g. '["/usr/bin/sudo", "-n"]'
QBEE_ELEVATION_COMMAND ?= ""
# Supplementary groups to add the execution user to for additional permissions. Needs to be in a space separated format, eg "docker tss"
QBEE_SUPPLEMENTARY_GROUPS ?= ""
```

# Note on systemd targets

The qbee-agent is designed to work without network connectivity. The default settings might therefore lead to some connection
error messages during startup. The qbee-agent will re-try the connection, so it will eventually come online once network is ready.
However, if you want to ovverride this behavior, you can use the `QBEE_SYSTEMD_AFTER_TARGET` and 
`QBEE_SYSTEMD_AFTER_TARGET` build time variables to make qbee-agent start after full connectivity has been establised.

Eg.
```
QBEE_SYSTEMD_AFTER_TARGET = "systemd-resolved.service network-online.target"
QBEE_SYSTEMD_WANTS_TARGET = "network-online.target"
```

# Note on unprivileged exec user

Versions 2026.11 and later of the qbee-agent supports running certain privileged operations using an elevation command (eg. `sudo`).
This allows setting up the qbee-agent systemd service to run as an unprivileged user, which might be required from a security perspective.
When running as a non-privileged user, the `QBEE_ELEVATION_COMMAND` will prefix any commands that needs privileged access which is 
executed natively by the code (eg. `rauc`). The default `sudo` configuration (under `recipes-extended`) can be overridden by custom layers.

If the `QBEE_EXEC_USER` is set to a non-empty value, a user with the value as username will be set up automatically and the service will be configured to
run under that user.

The `QBEE_SUPPLEMENTARY_GROUPS` will add supplementary groups to the systemd for additional permissions (eg. `QBEE_SUPPLEMENTARY_GROUPS="docker tss"`
if you want to use the `tpm` and `docker` features of the qbee-agent).

More information on the running qbee-agent as an unprivileged user can be found here: https://docs.qbee.io/agent-rootless.html 

# Note on qbee-agent versions < 2024.09

Versions of qbee-agent  < 2024.09 will also need the following:

```
EXTRA_IMAGE_FEATURES += "ssh-server-openssh"
```
For more information, please refer to our docs: https://docs.qbee.io/yocto-qbee-agent.html
