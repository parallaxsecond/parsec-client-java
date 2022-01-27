# Parsec Java Client

This repository contains a Java Client and a [JCA provider](https://docs.oracle.com/javase/8/docs/technotes/guides/security/crypto/CryptoSpec.html) for Parsec. The client exposes the [PSA Crypto API](https://github.com/ARMmbed/mbed-crypto/blob/psa-crypto-api/docs/PSA_Cryptography_API_Specification.pdf) to Java Applications and the JCA Provider allows existing applications that employ JCA to switch to Parsec.

*Note*: this client is at an early stage of development and not yet ready for production use. We welcome contributions!

## In this repository

The repository contains the following packages:

- parsec-client-java : PSA Crypto API Client
- parsec-jca-java: JCA Provider
- parsec-interface-java: Private wrapper for protobuf classes and socket communication
- parsec-protobuf-java: Java Protobuf classes (generated)
- parsec-testcontainers: Collection of Docker test containers for development & testing 

# How to use this library
TODO

# How to develop the Parsec Java Client
TODO

# Example Implementations
There are a number of example implementations of both the basic java client and JCA provider along with a demo (separate repository) 
Both the tests and workshop demo cover the basic functionality of the current implementation:

- Parsec JCA Tests [**Link**](/parsec-jca-test)
- Parsec Test Containers [**Link**](/parsec-testcontainers)
- Parsec Workshop Demos (External Repository) [**Link**](https://github.com/56kcloud/parsec-workshop)

## License

The software is provided under Apache-2.0. Contributions to this project are accepted under the same license.

## Contributing

We welcome contributing, both in the use of this client library and programming,extending of this library code base. 
Please check the [**Contribution Guidelines**](https://parallaxsecond.github.io/parsec-book/contributing/index.html)
to know more about the contribution process.

*Copyright 2021 Contributors to the Parsec project.*


