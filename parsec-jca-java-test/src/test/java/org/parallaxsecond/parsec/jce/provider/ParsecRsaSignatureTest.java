package org.parallaxsecond.parsec.jce.provider;

import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.fail;

class ParsecRsaSignatureTest {

    @Test
    void testMesageDigests() {
        Arrays.stream(ParsecRsaSignature.values())
                .forEach(
                        e -> {
                            try {
                                e.createMessageDigest();
                            } catch (NoSuchAlgorithmException ex) {
                                fail(
                                        e
                                                + " message digest factory not working "
                                                + e.getAlgorithmName());
                            }
                        });
    }
}
