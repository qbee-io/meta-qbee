FROM alpine:latest AS build
RUN apk add git
RUN git clone --depth=1 --branch=scarthgap https://git.yoctoproject.org/poky /src/poky


FROM alpine:latest
RUN apk add qemu-system-x86_64 bash parted python3 e2fsprogs-extra swtpm
COPY --from=build /src/poky/scripts /poky/scripts
COPY /images /qbee-demo/images
COPY /start.sh /qbee-demo/start.sh
CMD ["/bin/sh", "/qbee-demo/start.sh" ]

#/python3/wic cp /image/.bootstrap-env /image/core-minimal.wic:4/etc/qbee/yocto/
