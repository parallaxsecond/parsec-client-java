package org.parallaxsecond.parsec.jce.provider;

import java.io.File;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.parallaxsecond.parsec.client.core.BasicClient;
import org.parallaxsecond.parsec.client.core.ipc_handler.IpcHandler;
import static org.parallaxsecond.parsec.jce.provider.ParsecCipherSuites.RSA_WITH_PKCS1;
import org.parallaxsecond.testcontainers.ParsecContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Testcontainers
@Slf4j
class ParsecSignatureTest {
        @Container
        ParsecContainer parsecContainer = ParsecContainer.withVersion("latest").withFileSystemBind(
                        new File("src/test/resources/mbed-crypto-config.toml").getAbsolutePath(),
                        "/etc/parsec/config.toml");

        @SneakyThrows
        @Test
        void signVerify() {

                byte[] payload = new byte[1024];
                new Random().nextBytes(payload);

                String keyName = "testSignVerify";

                BasicClient client = BasicClient.client("parsec-tool",
                                IpcHandler.connectFromUrl(parsecContainer.getSocketUri()));
                ParsecClientAccessor parsecClient = () -> client;
                ParsecSignatureInfo signatureInfo = ParsecRsaSignature.SHA256_WITH_RSA;

                client.psaGenerateKey(keyName, RSA_WITH_PKCS1.getKeyAttributes());

                ParsecRsaPrivateKey privKey = ParsecRsaPrivateKey.builder()

                                .parsecName(keyName).algorithm("RSA").format("RAW").build();


                ParsecSignature signature = new ParsecSignature(signatureInfo, parsecClient);
                signature.engineInitSign(privKey);
                signature.engineUpdate(payload, 0, payload.length);
                byte[] signedBytes = signature.engineSign();


                ParsecPublicKey.ParsecPublicKeyImpl publicKey =
                                ParsecPublicKey.builder().parsecName(keyName).build();

                ParsecSignature verification = new ParsecSignature(signatureInfo, parsecClient);
                verification.engineInitVerify(publicKey);
                verification.engineUpdate(payload, 0, payload.length);
                assertTrue(verification.engineVerify(signedBytes));


        }

}
