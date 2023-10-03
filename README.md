# meta-qbee
Yocto Project meta layer for the qbee-agent client https://qbee.io

This branch holds layer compatible for yocto codenames:
* mickledore
* langdale
* kirkstone
* honister
* hardknott
* gatesgarth
* dunfell

# Include the meta-qbee layer in your build

```
git clone -b main https://github.com/qbee-io/meta-qbee layers/meta-qbee
bitbake-layers add-layer ../layers/meta-qbee/meta-qbee
```

# Add the following to local.conf

```
EXTRA_IMAGE_FEATURES += "ssh-server-openssh"
QBEE_BOOTSTRAP_KEY="<bootstrap_key>"
QBEE_DEVICE_NAME_TYPE="mac-address"
```
