package org.parallaxsecond.parsec.jce.provider;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.BasicHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.parallaxsecond.parsec.client.jna.Uid;
import org.parallaxsecond.testcontainers.ParsecContainer;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/** */
@Testcontainers
@Slf4j
class HttpClientTlsTest {

    private static final char[] keystorePassword = "changeme".toCharArray();

    @Container
    GenericContainer<?> nginxContainer =
            new GenericContainer<>("parallaxsecond/nginx-test:latest")
                    .withExposedPorts(80, 443)
                    .waitingFor(new HttpWaitStrategy().forPort(80).forStatusCode(200))
                    .withFileSystemBind(
                            absFile("src/test/resources/nginx-client-auth/init.sh"), "/init.sh");

    @Container
    ParsecContainer parsecContainer =
            ParsecContainer.withVersion("0.8.1")
                    .withFileSystemBind(
                            absFile("src/test/resources/mbed-crypto-config.toml"),
                            "/etc/parsec/config.toml");

    String hostPort;
    Path clientKeyStore;
    Path serverTrustStore;
    private TrustManagerFactory tmf;

    @SneakyThrows
    static Stream<Arguments> testHttpClient() {
        return Stream.of(
                Arguments.of("No keys", 403, (KmfTestFactory) t -> null),
                Arguments.of(
                        "JCA/Parsec",
                        200,
                        (KmfTestFactory)
                                t -> KeyManagerFactory.getInstance("X509", new ParsecProvider())),
                Arguments.of(
                        "JCA/Default/FS",
                        200,
                        (KmfTestFactory)
                                t -> {
                                    KeyManagerFactory kmf =
                                            KeyManagerFactory.getInstance(
                                                    KeyManagerFactory.getDefaultAlgorithm());
                                    kmf.init(
                                            defaultKeystoreFromFile(t.clientKeyStore),
                                            keystorePassword);
                                    return kmf;
                                }));
    }

    @SneakyThrows
    private static KeyStore defaultKeystoreFromFile(Path file) {
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        try (InputStream in = new FileInputStream(file.toFile())) {
            keyStore.load(in, keystorePassword);
        }
        return keyStore;
    }

    private String absFile(String f) {
        return new File(f).getAbsolutePath();
    }

    @BeforeEach
    @SneakyThrows
    void setup() {
        Awaitility.await().until(nginxContainer::isRunning);
        hostPort = format("%s:%s", nginxContainer.getHost(), nginxContainer.getMappedPort(443));
        ExecResult r =
                nginxContainer.execInContainer(
                        "sh",
                        "-c",
                        format("/init.sh %s %s /", hostPort, new String(keystorePassword)));
        assertEquals(0, r.getExitCode(), r.getStderr() + r.getStdout());

        serverTrustStore = Files.createTempFile("serverChainCert", ".jks");
        nginxContainer.copyFileFromContainer(
                "/keys/server_chain.jks", serverTrustStore.toFile().getAbsolutePath());

        clientKeyStore = Files.createTempFile("clientKeyStore", ".jks");
        nginxContainer.copyFileFromContainer(
                "/keys/client.jks", clientKeyStore.toFile().getAbsolutePath());

        tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(defaultKeystoreFromFile(serverTrustStore));

        // uid of the parse user in docker
        Uid.IMPL.set(() -> 4000);
        Awaitility.await().until(parsecContainer::isRunning);
        URI socketUri = parsecContainer.getSocketUri();
        ParsecProvider.init(socketUri);
    }

    @MethodSource
    @ParameterizedTest
    @SneakyThrows
    void testHttpClient(String description, int expectedResponseCode, KmfTestFactory kmfFactory) {
        log.info("running test {}", description);
        KeyManagerFactory kmf = kmfFactory.get(this);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(
                ofNullable(kmf).map(KeyManagerFactory::getKeyManagers).orElse(null),
                tmf.getTrustManagers(),
                null);

        assertNotNull(sslContext.getProvider());

        SSLConnectionSocketFactory sslsf =
                new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
        Registry<ConnectionSocketFactory> socketFactoryRegistry =
                RegistryBuilder.<ConnectionSocketFactory>create().register("https", sslsf).build();
        BasicHttpClientConnectionManager connectionManager =
                new BasicHttpClientConnectionManager(socketFactoryRegistry);
        CloseableHttpClient httpClient =
                HttpClients.custom().setConnectionManager(connectionManager).build();
        CloseableHttpResponse r = httpClient.execute(new HttpGet("https://" + hostPort));
        assertEquals(expectedResponseCode, r.getCode());
    }

    interface KmfTestFactory {
        KeyManagerFactory get(HttpClientTlsTest t) throws Exception;
    }
}
