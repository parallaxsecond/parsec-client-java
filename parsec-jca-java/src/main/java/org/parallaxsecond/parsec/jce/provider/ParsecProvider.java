package org.parallaxsecond.parsec.jce.provider;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.parallaxsecond.parsec.client.core.BasicClient;
import org.parallaxsecond.parsec.client.core.ipc_handler.IpcHandler;

import java.io.IOException;
import java.net.URI;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.Provider;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

/**
 * Parsec JCA Security Provider
 */
@Slf4j
public final class ParsecProvider extends Provider {

  public static final String PROVIDER_NAME;
  private static final String VERSION;

  @Getter
  private static BasicClient basicClient;


  static {
    final Properties parsecProperties = new Properties();
    try {
      parsecProperties.load(ParsecProvider.class.getClassLoader().getResourceAsStream("parsec.properties"));
    } catch (IOException e) {
      log.error("Could not load parsec.properties, exiting.", e);
      System.exit(1);
    }
    String versionString = parsecProperties.getProperty("version");
    VERSION = versionString.substring(0, 3);
    PROVIDER_NAME = parsecProperties.getProperty("name");
  }

  /**
   * Constructs a provider with the specified name, version number,
   * and information.
   */
  protected ParsecProvider() throws IOException {
    super(PROVIDER_NAME, VERSION, PROVIDER_NAME + " provider, version " + VERSION + ", implementing secure random number generation.");
    registerAlgorithms();
  }

  private void doRegister() {
   ps("SecureRandom",
           "NativePRNG",
           "org.parallaxsecond.parsec.jce.provider.SecureRandomParsec", null, null);
   ps("SecureRandom",
           "NativePRNGBlocking",
           "org.parallaxsecond.parsec.jce.provider.SecureRandomParsec", null, null);
   ps("SecureRandom",
           "NativePRNGNonBlocking",
           "org.parallaxsecond.parsec.jce.provider.SecureRandomParsec", null, null);


   // sun stuff from here

    ps("KeyManagerFactory", "X509",
            "org.parallaxsecond.parsec.jce.provider.KeyManagerFactoryImpl", null, null);



    ps("Signature", "MD5andSHA1withRSA",
            "sun.security.ssl.RSASignature", null, null);



    ps("KeyManagerFactory", "NewSunX509",
            "sun.security.ssl.KeyManagerFactoryImpl$X509",
            List.of("PKIX"), null);

    ps("TrustManagerFactory", "SunX509",
            "sun.security.ssl.TrustManagerFactoryImpl$SimpleFactory",
            null, null);
    ps("TrustManagerFactory", "PKIX",
            "sun.security.ssl.TrustManagerFactoryImpl$PKIXFactory",
            List.of("SunPKIX", "X509", "X.509"), null);

    ps("SSLContext", "TLSv1",
            "sun.security.ssl.SSLContextImpl$TLS10Context",
            List.of("SSLv3"), null);
    ps("SSLContext", "TLSv1.1",
            "sun.security.ssl.SSLContextImpl$TLS11Context", null, null);
    ps("SSLContext", "TLSv1.2",
            "sun.security.ssl.SSLContextImpl$TLS12Context", null, null);
    ps("SSLContext", "TLSv1.3",
            "sun.security.ssl.SSLContextImpl$TLS13Context", null, null);
    ps("SSLContext", "TLS",
            "sun.security.ssl.SSLContextImpl$TLSContext",
            List.of("SSL"), null);

    ps("SSLContext", "DTLSv1.0",
            "sun.security.ssl.SSLContextImpl$DTLS10Context", null, null);
    ps("SSLContext", "DTLSv1.2",
            "sun.security.ssl.SSLContextImpl$DTLS12Context", null, null);
    ps("SSLContext", "DTLS",
            "sun.security.ssl.SSLContextImpl$DTLSContext", null, null);

    ps("SSLContext", "Default",
            "sun.security.ssl.SSLContextImpl$DefaultSSLContext", null, null);

    /*
     * KeyStore
     */
    ps("KeyStore", "PKCS12",
            "sun.security.pkcs12.PKCS12KeyStore", null, null);
  }


  private void registerAlgorithms() {
    AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
      doRegister();
      return null;
    });
  }

  private void ps(String type, String algo, String cn,
                  List<String> a, HashMap<String, String> attrs) {
    putService(new Provider.Service(this, type, algo, cn, a, attrs));
  }





  /**
   * Initialise Parsec JCA Provider
   *
   * @param socketURI the socket on which Parsec is listening.
   */
  protected static void init(URI socketURI) {
    basicClient = BasicClient.client("parsec-jca-provider", IpcHandler.connectFromUrl(socketURI));
  }

}
