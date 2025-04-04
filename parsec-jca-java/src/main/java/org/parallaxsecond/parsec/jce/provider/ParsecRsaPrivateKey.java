package org.parallaxsecond.parsec.jce.provider;

import java.math.BigInteger;
import java.security.interfaces.RSAPrivateCrtKey;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * A private key implementation for RSA keys stored in a secure element through Parsec.
 * 
 * This class implements RSAPrivateCrtKey to provide all necessary key parameters to the TLS stack,
 * while ensuring private key material remains secure in the secure element.
 */
@Value
public class ParsecRsaPrivateKey implements RSAPrivateCrtKey {
    private static final long serialVersionUID = 1234567L;

    @NonNull
    String parsecName;
    @NonNull
    String algorithm;
    @NonNull
    String format;
    @NonNull
    BigInteger publicExponent;
    @NonNull
    BigInteger modulus;

    @Builder
    private ParsecRsaPrivateKey(String parsecName, String algorithm, String format,
            BigInteger publicExponent, BigInteger modulus) {
        this.parsecName = parsecName;
        this.algorithm = algorithm;
        this.format = format;
        this.publicExponent = publicExponent;
        this.modulus = modulus;
    }

    @Override
    public String getAlgorithm() {
        return algorithm;
    }

    @Override
    public String getFormat() {
        return format;
    }

    @Override
    public byte[] getEncoded() {
        return null; // Private key material cannot be exported from secure element
    }

    @Override
    public BigInteger getPrivateExponent() {
        return null; // Private key material cannot be exported from secure element
    }

    @Override
    public BigInteger getModulus() {
        return modulus;
    }

    @Override
    public BigInteger getPublicExponent() {
        return publicExponent;
    }

    @Override
    public BigInteger getPrimeP() {
        return null; // Private key material cannot be exported from secure element
    }

    @Override
    public BigInteger getPrimeQ() {
        return null; // Private key material cannot be exported from secure element
    }

    @Override
    public BigInteger getPrimeExponentP() {
        return null; // Private key material cannot be exported from secure element
    }

    @Override
    public BigInteger getPrimeExponentQ() {
        return null; // Private key material cannot be exported from secure element
    }

    @Override
    public BigInteger getCrtCoefficient() {
        return null; // Private key material cannot be exported from secure element
    }
}
