ARG REGISTRY
FROM ${REGISTRY}/parsec-rust-builder:latest as builder
ARG PARSEC_TOOL_VERSION=0.4.0
RUN set -e -x; \
    source $HOME/.cargo/env; \
    git clone https://github.com/parallaxsecond/parsec-tool; \
    cd parsec-tool; \
    git checkout ${PARSEC_TOOL_VERSION}; \
    cargo install --path .
