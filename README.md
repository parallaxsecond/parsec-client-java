# Parsec Java Client

To enabled Parsec support in the Java Ecosystem, this repository contains multiple implementaiton(s) of Java Client that consumes the API Providers by the Parsec Service. Using this library is possible in two ways, as a Basic Java Client and/or as a Native Java JCA Interface. 

## In this repository

The following are the 

- parsec-client-java : Basic Client
- parsec-interface-java: wrapper 
- parsec-jca-java: This is the native JCA (Java Cryptograpy Adapter..)
- parsec-protobuf-java: This is submodule of the PArsec Protobuf where java classes are generated 
- parsec-testcontainers: collection of Docker test containers to test the use of Java Client 

# How to use this library
Here is explain how to use the Parsec Java-Client library in your application

# How to develop the Parsec Java Client
If you'd like to get started in developing parsec further, we explain how to get started and the design behind the support for parsec in java

# Example Implementations
There are a number of example implementations of both the basic java client and JCA client along with a demo (seperate repository) 
Both the tests and workshop demo cover the basic functionality of Parsec that address the majority of use cases, the following

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


