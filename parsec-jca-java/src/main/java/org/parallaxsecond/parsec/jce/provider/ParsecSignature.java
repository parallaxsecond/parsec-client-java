package org.parallaxsecond.parsec.jce.provider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.parallaxsecond.parsec.client.exceptions.ClientException;
import org.parallaxsecond.parsec.client.exceptions.ServiceException;
import org.parallaxsecond.parsec.protocol.operations.NativeResult;

import java.security.*;

@RequiredArgsConstructor
@Slf4j
public final class ParsecSignature extends SignatureSpi {
    private final ParsecSignatureInfo signatureInfo;
    private final ParsecClientAccessor parsecClientAccessor;
    private ParsecRsaPrivateKey privateKey;
    private MessageDigest messageDigest;

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
        try {
            this.messageDigest = this.signatureInfo.getMessageDigestFactory().create();
        } catch (NoSuchAlgorithmException e) {
            String message =
                    String.format(
                            "Error creating associated message digest, key: %s, signatureInfo: %s",
                            privateKey, this.signatureInfo);
            log.debug(message);
            throw new InvalidKeyException(message, e);
        }
    }

    @Override
    protected void engineUpdate(byte b) throws SignatureException {
        this.messageDigest.update(b);
    }

    @Override
    protected void engineUpdate(byte[] b, int off, int len) throws SignatureException {
        this.messageDigest.update(b, off, len);
    }

    @Override
    protected byte[] engineSign() throws SignatureException {
        byte[] digest = this.messageDigest.digest();
        try {
            NativeResult.PsaSignHashResult r = parsecClientAccessor.get()
                            .psaSignHash(privateKey.getParsecName(), digest, signatureInfo.getParsecAlgorithm());
            return r.getSignature();
        } catch (ServiceException | ClientException e) {
            throw new SignatureException("error signing value, signatureInfo: " + signatureInfo, e);
        }
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
