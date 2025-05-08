# Parsec Java Client

This repository contains a Java Client and a [JCA provider](https://docs.oracle.com/javase/8/docs/technotes/guides/security/crypto/CryptoSpec.html) for Parsec. The client exposes the [PSA Crypto API](https://github.com/ARMmbed/mbed-crypto/blob/psa-crypto-api/docs/PSA_Cryptography_API_Specification.pdf) to Java Applications and the JCA Provider allows existing applications that employ JCA to switch to Parsec.

_Note_: this client is at an early stage of development and not yet ready for production use. We welcome contributions!

## In this repository

The repository contains the following packages:

- parsec-client-java : PSA Crypto API Client
- parsec-jca-java: JCA Provider
- parsec-interface-java: Private wrapper for protobuf classes and socket communication
- parsec-protobuf-java: Java Protobuf classes (generated)
- parsec-testcontainers: Collection of Docker test containers for development & testing

# How to use this library

To use the Parsec JCA provider in your Maven project, you need to:

1.  **Configure GitHub Packages Repository:**
    Add the following repository configuration to your project's `pom.xml`. This allows Maven to find and download Parsec Java Client artifacts from GitHub Packages.

    ```xml
    <project>
        ...
        <repositories>
            <repository>
                <id>github-parallaxsecond</id>
                <name>GitHub Parallax Second Apache Maven Packages</name>
                <url>https://maven.pkg.github.com/parallaxsecond/parsec-client-java</url>
            </repository>
        </repositories>
        ...
    </project>
    ```

2.  **Add the Dependency:**
    Add the `parsec-jca-java` artifact as a dependency in your `pom.xml`:

    ```xml
    <dependencies>
        ...
        <dependency>
            <groupId>org.parallaxsecond</groupId>
            <artifactId>parsec-jca-java</artifactId>
            <version>0.1.0</version> <!-- Replace with the desired version -->
        </dependency>
        ...
    </dependencies>
    ```

    This will also bring in the necessary transitive dependencies: `parsec-client-java`, `parsec-interface-java`, and `parsec-protobuf-java`. For other JVM build systems, please take the necessary coordinates (`groupId`, `artifactId`, `version`) and adapt to your dependency management syntax. You can find the latest available version on the [project's GitHub Packages page](https://github.com/parallaxsecond/parsec-client-java/packages).

    If you don't want to use the JCA, you can employ this library in a more parsec-idiomatic way by depending directly on `parsec-client-java`. _Note:_ this is not a recommendation either way.

# How to develop the Parsec Java Client

Check out this repo's submodules:

```sh
git submodule update --init --recursive
```

You can use `act` to run the github action locally. On OSX, you need to set the container architecture, and for testcontainers to work, you may need to set the env var `TESTCONTAINERS_HOST_OVERRIDE`.

Example CLI input:

```sh
act --container-architecture linux/amd64 --env TESTCONTAINERS_HOST_OVERRIDE=`ipconfig getifaddr en0`
```

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

_Copyright 2021 Contributors to the Parsec project._
