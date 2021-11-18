# Example how to use Parsec Java Library

## Provider in Greengrass

To STart Greengrass the following is required:
This gudile follows basically this
https://docs.aws.amazon.com/greengrass/v2/developerguide/manual-installation.html#run-greengrass-core-v2-installer-manual


- Java 8 or 11



# Deploying Componetns

Debug Console
 - https://docs.aws.amazon.com/greengrass/v2/developerguide/local-debug-console-component.html

# References

- Good Youtube presentation https://www.youtube.com/watch?v=fBNG8OglRZQ

## Example with PCKS11 for compaision 

Instructions for #2.

softhsm2-util --init-token --label <some label> --so-pin <some private pin> --pin <some user pin> --free

Look in the output of this command to get the slot number which must be configured in the Greengrass config

Convert private key to PKCS8 format
```openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in <private key>.pem -out pkcs8.key```

Import private key to HSM
```pkcs11-tool --module <path to softhsm library> -l -p <user pin> --write-object pkcs8.key --type privkey --id 0000 --label <some label>```

import certificate
```pkcs11-tool --module <path to softhsm library> -l -p <user pin> --write-object <certificate.pem> --type cert --id 0000 --label <some label>`````

# Java Crypto Notes


