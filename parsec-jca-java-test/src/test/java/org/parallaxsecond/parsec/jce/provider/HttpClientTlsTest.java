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
import org.parallaxsecond.parsec.client.core.BasicClient;
import org.parallaxsecond.parsec.client.core.ipc_handler.IpcHandler;
import org.parallaxsecond.parsec.client.jna.Uid;
import org.parallaxsecond.parsec.protobuf.psa_algorithm.PsaAlgorithm;
import org.parallaxsecond.parsec.protobuf.psa_key_attributes.PsaKeyAttributes;
import org.parallaxsecond.testcontainers.ParsecContainer;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.KeyStoreBuilderParameters;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;
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
    private TrustManagerFactory tmf;

    @SneakyThrows
    private static KeyStore defaultKeystoreFromFile(Path file) {
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        try (InputStream in = new FileInputStream(file.toFile())) {
            keyStore.load(in, keystorePassword);
        }
        return keyStore;
    }

    @SneakyThrows
    private KeyManagerFactory prepareParsecTest() {
        Path clientKeyDer = copyFromNginx("/keys/client.der");
        BasicClient client =
                BasicClient.client(
                        "parsec-jca-provider",
                        IpcHandler.connectFromUrl(parsecContainer.getSocketUri()));
        client.psaImportKey(
                "client",
                Files.readAllBytes(clientKeyDer),
                PsaKeyAttributes.KeyAttributes.newBuilder()
                        .setKeyPolicy(
                                PsaKeyAttributes.KeyPolicy.newBuilder()
                                        .setKeyAlgorithm(
                                                PsaAlgorithm.Algorithm.newBuilder()
                                                        .setAsymmetricSignature(
                                                                PsaAlgorithm.Algorithm
                                                                        .AsymmetricSignature
                                                                        .newBuilder()
                                                                        .setRsaPkcs1V15SignRaw(
                                                                                PsaAlgorithm
                                                                                        .Algorithm
                                                                                        .AsymmetricSignature
                                                                                        .RsaPkcs1v15SignRaw
                                                                                        .newBuilder()
                                                                                        .build())
                                                                        .build())
                                                        .build())
                                        .setKeyUsageFlags(
                                                PsaKeyAttributes.UsageFlags.newBuilder()
                                                        .setSignHash(true)
                                                        .build())
                                        .build())
                        .setKeyType(
                                PsaKeyAttributes.KeyType.newBuilder()
                                        .setRsaKeyPair(
                                                PsaKeyAttributes.KeyType.RsaKeyPair.newBuilder()
                                                        .build())
                                        .build())
                        .build());
        URI socketUri = parsecContainer.getSocketUri();
        Provider parsec = ParsecProvider.builder().socketUri(socketUri).build();
        Security.insertProviderAt(parsec, 1);

        Path clientCertStoreFile = copyFromNginx("/keys/client_cert.jks");
        KeyStore clientCertStore = defaultKeystoreFromFile(clientCertStoreFile);

        // ParsecProvider.init(socketUri);
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("X509", "PARSEC");
        kmf.init(
                new KeyStoreBuilderParameters(
                        KeyStore.Builder.newInstance(
                                clientCertStore,
                                new KeyStore.PasswordProtection(keystorePassword))));
        return kmf;
    }

    @SneakyThrows
    private KeyManagerFactory onFileJksTest() {
        Path clientKeyStore = copyFromNginx("/keys/client.jks");
        KeyManagerFactory kmf =
                KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(defaultKeystoreFromFile(clientKeyStore), keystorePassword);
        return kmf;
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

        Path serverTrustStore = copyFromNginx("/keys/server_chain.jks");
        tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(defaultKeystoreFromFile(serverTrustStore));

        // uid of the parse user in docker
        Uid.IMPL.set(() -> 4000);
        Awaitility.await().until(parsecContainer::isRunning);
    }

    @SneakyThrows
    private Path copyFromNginx(String path) {
        Path tempFile = Files.createTempFile("file", ".temp");
        nginxContainer.copyFileFromContainer(path, tempFile.toFile().getAbsolutePath());
        return tempFile;
    }


    @SneakyThrows
    static Stream<Arguments> testHttpClient() {
        return Stream.of(
                Arguments.of("No keys", 403, (KmfTestFactory) t -> null),
                Arguments.of("JCA/Parsec", 200, (KmfTestFactory) HttpClientTlsTest::prepareParsecTest),
                Arguments.of("JCA/Default/FS", 200, (KmfTestFactory) HttpClientTlsTest::onFileJksTest));
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
