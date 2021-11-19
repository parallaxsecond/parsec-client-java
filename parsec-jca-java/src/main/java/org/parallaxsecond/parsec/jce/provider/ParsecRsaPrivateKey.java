package org.parallaxsecond.parsec.jce.provider;

import lombok.Value;

import java.math.BigInteger;
import java.security.interfaces.RSAPrivateKey;

@Value
public class ParsecRsaPrivateKey implements RSAPrivateKey {
    String parsecName;
    String algorithm;
    String format;
    BigInteger modulus;

    @Override
    public BigInteger getPrivateExponent() {
        throw new IllegalStateException("cannot be called");
    }

    @Override
    public byte[] getEncoded() {
        throw new IllegalStateException("cannot be called");
    }
}
