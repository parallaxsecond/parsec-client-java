ARG REGISTRY
FROM ${REGISTRY}/parsec-tool-builder:latest as parsec-tool
FROM ${REGISTRY}/parsec-rust-builder:latest as builder

ARG PARSEC_BRANCH=0.8.1
ARG PARSEC_FEATURES="cryptoauthlib-provider,mbed-crypto-provider,unix-peer-credentials-authenticator,direct-authenticator"
RUN set -e -x; \
    source $HOME/.cargo/env; \
    git clone https://github.com/parallaxsecond/parsec; \
    cd parsec; \
    git checkout ${PARSEC_BRANCH}; \
    cargo install --features "${PARSEC_FEATURES}" --path .

ARG PARSEC_UID=4000
ARG PARSEC_GID=4000
RUN set -e -x; \
    groupadd -g ${PARSEC_GID} parsec; \
    useradd -u ${PARSEC_UID} -g ${PARSEC_GID} -d /home/parsec parsec; \
    mkdir -p /var/lib/parsec /home/parsec /run/parsec /etc/parsec /usr/libexec/parsec; \
    chmod 700 /var/lib/parsec /etc/parsec /usr/libexec/parsec; \
    chmod 755 /run/parsec;

FROM amazonlinux:2 as run
ENV RUST_LOG=info
ARG PARSEC_UID=4000
ARG PARSEC_GID=4000

RUN yum install -y socat && \
    rm -rf /root/.cache && \
    yum clean all && \
    rm -rf /var/cache/yum && \
    rpm --rebuilddb

COPY --from=builder /etc/passwd /etc/passwd
COPY --from=builder /etc/group /etc/group
COPY --from=builder /root/.cargo/bin/parsec* /usr/bin/
COPY --from=parsec-tool /root/.cargo/bin/parsec* /usr/bin/
COPY parsec/config.toml /etc/parsec/
COPY --from=builder --chown=${PARSEC_UID}:${PARSEC_GID} /usr/libexec/parsec /usr/libexec/parsec
COPY --from=builder --chown=${PARSEC_UID}:${PARSEC_GID} /var/lib/parsec /var/lib/parsec
COPY --from=builder --chown=${PARSEC_UID}:${PARSEC_GID} /home/parsec /home/parsec
COPY --from=builder --chown=${PARSEC_UID}:${PARSEC_GID} /run/parsec /run/parsec
COPY --from=builder --chown=${PARSEC_UID}:${PARSEC_GID} /etc/parsec /etc/parsec

USER parsec:parsec
WORKDIR "/home/parsec"
CMD ["parsec", "-c", "/etc/parsec/config.toml"]