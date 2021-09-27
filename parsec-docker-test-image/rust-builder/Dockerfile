FROM amazonlinux:2 as builder

RUN set -e -x; \
    yum install -y git gcc shadow-utils cmake3 make gcc-c++ pkgconfig clang-devel;\
    ln -s /usr/bin/cmake3 /usr/bin/cmake

RUN curl https://sh.rustup.rs -sSf | sh -s -- -y

# Workaround for incorrect aarch64 entry in cargo build
RUN ln -s /usr/bin/gcc /usr/bin/aarch64-linux-gnu-gcc