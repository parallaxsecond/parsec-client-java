package org.parallaxsecond.parsec.jce.provider;

import lombok.RequiredArgsConstructor;

import java.security.*;

@RequiredArgsConstructor
public abstract class ParsecSignature extends SignatureSpi {
    private final ParsecClientAccessor parsecClientAccessor;
    private ParsecRsaPrivateKey privateKey;

    @Override
    protected void engineInitVerify(PublicKey publicKey) throws InvalidKeyException {
        throw new IllegalStateException("not implemented");
    }

    @Override
    protected void engineInitSign(PrivateKey privateKey) throws InvalidKeyException {
        if (!(privateKey instanceof ParsecRsaPrivateKey)) {
            throw new InvalidKeyException(
                    String.format(
                            "Invalid key, expected a key of type %s.",
                            ParsecRsaPrivateKey.class.getName()));
        }
        this.privateKey = (ParsecRsaPrivateKey) privateKey;
    }


    @Override
    protected void engineUpdate(byte b) throws SignatureException {
        throw new IllegalStateException("not implemented");
    }

    @Override
    protected void engineUpdate(byte[] b, int off, int len) throws SignatureException {
        throw new IllegalStateException("not implemented");
    }

    @Override
    protected byte[] engineSign() throws SignatureException {
        throw new IllegalStateException("not implemented");
    }

    @Override
    protected boolean engineVerify(byte[] sigBytes) throws SignatureException {
        throw new IllegalStateException("not implemented");
    }

    @Override
    protected void engineSetParameter(String param, Object value) throws InvalidParameterException {
        throw new IllegalStateException("not implemented");
    }

    @Override
    protected Object engineGetParameter(String param) throws InvalidParameterException {
        throw new IllegalStateException("not implemented");
    }
}
