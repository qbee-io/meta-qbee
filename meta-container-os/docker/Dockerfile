FROM alpine:latest AS build
RUN apk add git py3-pip
# Standalone wic (same version as used by Yocto 6.0 / wrynose)
RUN pip wheel --no-deps -w /wheels git+https://git.yoctoproject.org/wic.git@v0.3.1


FROM alpine:latest
RUN apk add qemu-system-x86_64 bash parted python3 py3-pip e2fsprogs-extra mtools swtpm
COPY --from=build /wheels /wheels
RUN pip install --break-system-packages --no-cache-dir /wheels/*.whl && rm -rf /wheels
COPY /images /qbee-demo/images
COPY /entrypoint.sh /qbee-demo/entrypoint.sh
CMD ["/bin/bash", "/qbee-demo/entrypoint.sh" ]
