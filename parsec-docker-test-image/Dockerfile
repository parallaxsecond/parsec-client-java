FROM amazonlinux:2 as builder
ARG PARSEC_UID=4000
ARG PARSEC_GID=4000
ARG PARSEC_BRANCH=0.8.1
ARG PARSEC_FEATURES="mbed-crypto-provider, unix-peer-credentials-authenticator, direct-authenticator"
ARG PARSEC_TOOL_VERSION=0.4.0
RUN set -e -x; \
    yum install -y git gcc shadow-utils cmake3 make gcc-c++ pkgconfig clang-devel;\
    ln -s /usr/bin/cmake3 /usr/bin/cmake

RUN curl https://sh.rustup.rs -sSf | sh -s -- -y

# Workaround for incorrect aarch64 entry in cargo build
RUN ln -s /usr/bin/gcc /usr/bin/aarch64-linux-gnu-gcc

RUN set -e -x; \
    source $HOME/.cargo/env; \
    git clone https://github.com/parallaxsecond/parsec-tool; \
    cd parsec-tool; \
    git checkout ${PARSEC_TOOL_VERSION}; \
    cargo install --path .

RUN set -e -x; \
    source $HOME/.cargo/env; \
    git clone https://github.com/parallaxsecond/parsec; \
    cd parsec; \
    git checkout ${PARSEC_BRANCH}; \
    cargo install --features "${PARSEC_FEATURES}" --path .

RUN set -e -x; \
    groupadd -g ${PARSEC_GID} parsec; \
    useradd -u ${PARSEC_UID} -g ${PARSEC_GID} -d /home/parsec parsec; \
    mkdir -p /var/lib/parsec /home/parsec /run/parsec;

FROM amazonlinux:2 as run
ENV RUST_LOG=info
ARG PARSEC_UID=4000
ARG PARSEC_GID=4000

RUN yum install -y socat procps-ng && \
    rm -rf /root/.cache && \
    yum clean all && \
    rm -rf /var/cache/yum && \
    rpm --rebuilddb

COPY --from=builder /etc/passwd /etc/passwd
COPY --from=builder /etc/group /etc/group
COPY --from=builder /root/.cargo/bin/parsec* /usr/bin/
COPY --from=builder /parsec/config.toml /
COPY --from=builder --chown=${PARSEC_UID}:${PARSEC_GID} /var/lib/parsec /var/lib/parsec
COPY --from=builder --chown=${PARSEC_UID}:${PARSEC_GID} /home/parsec /home/parsec
COPY --from=builder --chown=${PARSEC_UID}:${PARSEC_GID} /run/parsec /run/parsec


USER parsec:parsec
CMD ["parsec"]