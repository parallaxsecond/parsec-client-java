package org.parallaxsecond.parsec.jce.provider;

import lombok.Builder;
import lombok.Value;

import java.security.PublicKey;

public interface ParsecPublicKey extends PublicKey {
    String getParsecName();

    static ParsecPublicKey.ParsecPublicKeyImpl.ParsecPublicKeyImplBuilder builder() {
        return ParsecPublicKeyImpl.builder();
    }

    @Value
    @Builder
    class ParsecPublicKeyImpl implements ParsecPublicKey {
        String parsecName;
        String algorithm;
        String format;

        @Override
        public byte[] getEncoded() {
            throw new UnsupportedOperationException("Not implemented");
        }
    }
}
