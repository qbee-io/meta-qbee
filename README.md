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
```

Versions of qbee-agent  < 2024.09 will also need the following:

```
EXTRA_IMAGE_FEATURES += "ssh-server-openssh"
```
For more information, please refer to our docs: https://qbee.io/docs/yocto-qbee-agent.html
