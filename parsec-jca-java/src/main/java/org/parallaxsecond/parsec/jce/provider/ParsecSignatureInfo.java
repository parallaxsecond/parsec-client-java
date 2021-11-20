package org.parallaxsecond.parsec.jce.provider;

import org.parallaxsecond.parsec.protobuf.psa_algorithm.PsaAlgorithm;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public interface ParsecSignatureInfo {
    ParsecSignature create(ParsecClientAccessor parsecClientAccessor);
    String getAlgorithmName();
    String name();
    MessageDigestFactory getMessageDigestFactory();
    default MessageDigest createMessageDigest() throws NoSuchAlgorithmException {
        return getMessageDigestFactory().create();
    }
    PsaAlgorithm.Algorithm.AsymmetricSignature getParsecAlgorithm();

    interface MessageDigestFactory {
        MessageDigest create() throws NoSuchAlgorithmException;
    }
}
