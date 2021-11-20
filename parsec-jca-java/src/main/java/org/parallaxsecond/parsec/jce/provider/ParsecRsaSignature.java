package org.parallaxsecond.parsec.jce.provider;

import lombok.RequiredArgsConstructor;

import java.security.*;

@RequiredArgsConstructor
public abstract class ParsecRsaSignature extends SignatureSpi {
    private final ParsecClientAccessor parsecClientAccessor;

    public static class MD2withRSA extends ParsecRsaSignature{
        public MD2withRSA(ParsecClientAccessor parsecClientAccessor) {
            super(parsecClientAccessor);
        }

    }

    public static class MD5withRSA  extends ParsecRsaSignature{
        public MD5withRSA(ParsecClientAccessor parsecClientAccessor) {
            super(parsecClientAccessor);
        }
    }

    public static class SHA1withRSA  extends ParsecRsaSignature{
        public SHA1withRSA(ParsecClientAccessor parsecClientAccessor) {
            super(parsecClientAccessor);
        }
    }

    public static class SHA224withRSA  extends ParsecRsaSignature{
        public SHA224withRSA(ParsecClientAccessor parsecClientAccessor) {
            super(parsecClientAccessor);
        }
    }

    public static class SHA256withRSA  extends ParsecRsaSignature{
        public SHA256withRSA(ParsecClientAccessor parsecClientAccessor) {
            super(parsecClientAccessor);
        }
    }

    public static class SHA384withRSA  extends ParsecRsaSignature{
        public SHA384withRSA(ParsecClientAccessor parsecClientAccessor) {
            super(parsecClientAccessor);
        }
    }

    public static class SHA512withRSA  extends ParsecRsaSignature{
        public SHA512withRSA(ParsecClientAccessor parsecClientAccessor) {
            super(parsecClientAccessor);
        }
    }

    public static class SHA512_224withRSA  extends ParsecRsaSignature{
        public SHA512_224withRSA(ParsecClientAccessor parsecClientAccessor) {
            super(parsecClientAccessor);
        }
    }

    public static class SHA512_256withRSA  extends ParsecRsaSignature{
        public SHA512_256withRSA(ParsecClientAccessor parsecClientAccessor) {
            super(parsecClientAccessor);
        }
    }

    public static class RSAPSSSignature  extends ParsecRsaSignature{
        public RSAPSSSignature(ParsecClientAccessor parsecClientAccessor) {
            super(parsecClientAccessor);
        }
    }


    @Override
    protected void engineInitVerify(PublicKey publicKey) throws InvalidKeyException {
        throw new IllegalStateException("not implemented");
    }

    @Override
    protected void engineInitSign(PrivateKey privateKey) throws InvalidKeyException {
        throw new IllegalStateException("not implemented");
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
