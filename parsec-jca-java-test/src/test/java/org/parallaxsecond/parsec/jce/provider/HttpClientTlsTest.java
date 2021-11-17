package org.parallaxsecond.parsec.jce.provider;


import com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.ssl.SSLContexts;
import com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.ssl.TrustStrategy;
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
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parallaxsecond.parsec.client.jna.Uid;
import org.parallaxsecond.testcontainers.ParsecContainer;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import org.testcontainers.shaded.org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 */
@Testcontainers
@Slf4j
class HttpClientTlsTest {

    @Container
    GenericContainer<?> nginxContainer = new GenericContainer<>("nginx")
            .withExposedPorts(80, 443)
            .waitingFor(new HttpWaitStrategy().forPort(80).forStatusCode(200))
            .withFileSystemBind(absFile("src/test/resources/nginx-client-auth/init.sh"), "/init.sh");

    private String absFile(String f) {
        return new File(f).getAbsolutePath();
    }

    @Container
    ParsecContainer parsecContainer =
            ParsecContainer.withVersion("0.8.1")
                    .withFileSystemBind(
                            absFile("src/test/resources/mbed-crypto-config.toml"),
                            "/etc/parsec/config.toml");

    private final String eccKey = "eccKey";
    private final String rsaKey = "rsaKey";
    private final String[] algorithms = {"NativePRNG", "NativePRNGBlocking", "NativePRNGNonBlocking"};


    @BeforeEach
    @SneakyThrows
    void setup() throws IOException {
        Awaitility.await().until(nginxContainer::isRunning);

        ExecResult r = nginxContainer.execInContainer(
                "sh", "-c", format("/init.sh %s %s /", nginxContainer.getHost(),
                        nginxContainer.getMappedPort(443)));
        assertEquals(0, r.getExitCode(), r.getStderr() + r.getStdout());

        r = nginxContainer.execInContainer("nginx -s reload");
        assertEquals(0, r.getExitCode(), r.getStderr() + r.getStdout());


        // uid of the parse user in docker
        Uid.IMPL.set(() -> 4000);
        Awaitility.await().until(parsecContainer::isRunning);
        URI socketUri = parsecContainer.getSocketUri();
        ParsecProvider.init(socketUri);
//        parsecContainer.parsecTool("create-ecc-key", "--key-name", eccKey);
//        parsecContainer.parsecTool("create-rsa-key", "--key-name", rsaKey);
        //Security.insertProviderAt(parsec, 1);
//        Security.getProvider(parsec.getName());
    }

    @Test
    @SneakyThrows
    void testHttpClient() {
        TrustStrategy acceptingTrustStrategy = (cert, authType) -> true;

        KeyManagerFactory kmf=KeyManagerFactory.getInstance("X509", new ParsecProvider());

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), null, null);

        assertTrue(sslContext.getProvider() != null);


        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext,
                NoopHostnameVerifier.INSTANCE);

        Registry<ConnectionSocketFactory> socketFactoryRegistry =
                RegistryBuilder.<ConnectionSocketFactory> create()
                        .register("https", sslsf)
                        .build();

        BasicHttpClientConnectionManager connectionManager =
                new BasicHttpClientConnectionManager(socketFactoryRegistry);

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager).build();

        CloseableHttpResponse r = httpClient.execute(new HttpGet("https://google.com"));
        assertEquals(200, r.getCode());

    }

}

