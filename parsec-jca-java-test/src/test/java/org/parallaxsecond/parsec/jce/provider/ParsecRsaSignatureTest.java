package org.parallaxsecond.parsec.jce.provider;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parallaxsecond.parsec.client.jna.Uid;
import org.parallaxsecond.testcontainers.ParsecContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Testcontainers
@Slf4j
class ParsecRsaSignatureTest {

    private String absFile(String f) {
        return new File(f).getAbsolutePath();
    }

    @Container
    ParsecContainer parsecContainer = ParsecContainer.withVersion("latest").withFileSystemBind(
            absFile("src/test/resources/mbed-crypto-config.toml"), "/etc/parsec/config.toml");

    @BeforeEach
    @SneakyThrows
    void setup() {
        // uid of the parsec user in docker
        Uid.IMPL.set(() -> 4000);
        Awaitility.await().until(parsecContainer::isRunning);

        // Create and install the Parsec provider at highest priority
        Provider provider = ParsecProvider.builder().socketUri(parsecContainer.getSocketUri())
                .parsecAppName("parsec-test").build();

        // Insert at position 0 for highest priority
        Security.insertProviderAt(provider, 0);
    }

    @Test
    void testMesageDigests() {
        Arrays.stream(ParsecRsaSignature.values()).forEach(e -> {
            try {
                e.createMessageDigest();
            } catch (NoSuchAlgorithmException ex) {
                log.error("message digest factory not working", ex);
                fail(e + " message digest factory not working " + e.getAlgorithmName());
            }
        });
    }
}
