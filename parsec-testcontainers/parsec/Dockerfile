# Copyright 2023 Contributors to the Parsec project.
# SPDX-License-Identifier: Apache-2.0
# Liberally adapted from:
# https://github.com/parallaxsecond/parsec/blob/5b2c4904d96808a5fb98a599f5304acef8b26dda/quickstart/quickstart.Dockerfile  

# ---------------------------------------------
# Docker Stage: Base builder used for both parsec service and tools
FROM rust:latest AS base_builder

RUN apt update -y && \
    apt install -y llvm-dev libclang-dev clang cmake jq 

## Track various build environment things we may want to use throughout
WORKDIR /build-env
RUN echo "$(uname | awk '{print tolower($0)}')" > /build-env/os
RUN echo "$(arch)" > /build-env/arch
RUN echo "$(rustc --version)" > /build-env/rustc-version
RUN echo "$(cargo --version)" > /build-env/cargo-version

# ---------------------------------------------
# Docker Stage: Temporary stage to help dependency caching
FROM base_builder AS parsec_service_scratch

WORKDIR /
## Checkout the latest release
RUN git clone https://github.com/parallaxsecond/parsec;

WORKDIR /parsec
# Latest release 1.4.1 doesn't compile: https://github.com/parallaxsecond/parsec/issues/776 
# TODO: add the following line before `cargo fetch` once the fix is released
# git checkout $(git tag --sort=committerdate | grep -v rc | tail -1); \
RUN cargo fetch

# ---------------------------------------------
# Docker Stage: Executes the build of the Parsec Service
FROM parsec_service_scratch AS parsec_service_builder

## Run the actual build
ARG PARSEC_FEATURES="cryptoauthlib-provider,mbed-crypto-provider,unix-peer-credentials-authenticator,direct-authenticator"
RUN git describe --exact-match --tags; \
    echo 'Building Parsec with features:' && echo "${PARSEC_FEATURES}" && \
    cargo build --release --features "${PARSEC_FEATURES}"; 

# Save the current parsec version and dependencies as defined by cargo and the current git commit hash
RUN echo "$(cargo metadata --format-version=1 --no-deps --offline | jq -r '.packages[0].version')" > /build-env/parsec-version
RUN echo "$(cargo tree)" > /build-env/parsec-dependencies
RUN echo "$(git rev-parse HEAD)" > /build-env/parsec-commit

# ---------------------------------------------
# Docker Stage: Executes the build of the Parsec Tool
FROM base_builder AS parsec_tool_builder

RUN git clone https://github.com/parallaxsecond/parsec-tool /parsec-tool
WORKDIR /parsec-tool
RUN git checkout $(git tag --sort=committerdate | tail -1)
RUN cargo build --release

# Save the current parsec-tool version and dependencies as defined by cargo and the current git commit hash
RUN echo "$(cargo metadata --format-version=1 --no-deps --offline | jq -r '.packages[0].version')" > /build-env/parsec-tool-version
RUN echo "$(cargo tree)" > /build-env/parsec-tool-dependencies
RUN echo "$(git rev-parse HEAD)" > /build-env/parsec-tool-commit

# ---------------------------------------------
# Docker Stage: Extracts build results from previous stages and adds in quickstart configs
FROM base_builder AS layout

## Add the built binaries into the image
COPY --from=parsec_service_builder /parsec/target/release/parsec /parsec/bin/parsec
COPY --from=parsec_tool_builder /parsec-tool/target/release/parsec-tool /parsec/bin/parsec-tool

## Create and configure a starting directory for quickstart operations
WORKDIR /parsec/quickstart
COPY config.toml /parsec/quickstart/config.toml
COPY --from=parsec_tool_builder /parsec-tool/tests/parsec-cli-tests.sh /parsec/quickstart/parsec-cli-tests.sh

## Grab all the build-env values
COPY --from=parsec_service_builder /build-env/* /build-env/
COPY --from=parsec_tool_builder /build-env/* /build-env/

## Generate the build details file
COPY construct-build-details.sh /build-env/
RUN chmod +x /build-env/construct-build-details.sh && /build-env/construct-build-details.sh > /parsec/quickstart/build.txt

# ---------------------------------------------
# Docker Stage: Constructs a valid Docker image with Parsec Quickstart
FROM ubuntu:latest AS runnable_image

COPY --from=layout /parsec /parsec

ENV PATH=$PATH:/parsec/bin
ENV PARSEC_SERVICE_ENDPOINT=unix:/parsec/quickstart/parsec.sock

RUN apt update && apt install -y openssl socat


RUN useradd -ms /bin/bash qs
RUN chown -R qs:qs /parsec/quickstart 
USER qs

WORKDIR /parsec/quickstart

RUN chmod a+rwx /parsec/quickstart/

COPY config.toml /etc/parsec/

CMD ["parsec", "-c", "/etc/parsec/config.toml"]
