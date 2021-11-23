package org.parallaxsecond.parsec.jce.provider;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.math.BigInteger;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

// FIXME verify if we can get rid off the RSAPrivateKey inheritance
@Builder
@Value
public class ParsecRsaPrivateKey implements RSAPrivateKey {
    @NonNull
    String parsecName;
    @NonNull
    String algorithm;
    @NonNull
    String format;
    // FIXME should get rid of this
    RSAPublicKey rsaPublicKey;

    @Override
    public BigInteger getPrivateExponent() {
        throw new IllegalStateException("cannot be called");
    }

    @Override
    public byte[] getEncoded() {
        throw new IllegalStateException("cannot be called");
    }

    @Override
    public BigInteger getModulus() {
        return rsaPublicKey.getModulus();
    }
}
