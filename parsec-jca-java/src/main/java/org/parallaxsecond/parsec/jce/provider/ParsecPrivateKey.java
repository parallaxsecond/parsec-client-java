package org.parallaxsecond.parsec.jce.provider;

import lombok.Value;

import java.security.PrivateKey;

@Value
public class ParsecPrivateKey implements PrivateKey {
    String parsecName;
    String algorithm;
    String format;

    @Override
    public byte[] getEncoded() {
        throw new IllegalStateException("can't export private key");
    }
}
