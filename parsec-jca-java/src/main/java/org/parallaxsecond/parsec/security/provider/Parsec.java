package org.parallaxsecond.parsec.security.provider;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.parallaxsecond.parsec.client.core.BasicClient;
import org.parallaxsecond.parsec.client.core.ipc_handler.IpcHandler;

import java.io.IOException;
import java.net.URI;
import java.security.Provider;
import java.util.Properties;

/**
 * Parsec JCA Security Provider
 */
@Slf4j
public final class Parsec extends Provider {

  private static final String NAME;
  private static final double VERSION;

  @Getter
  private static BasicClient basicClient;


  static {
    final Properties parsecProperties = new Properties();
    try {
      parsecProperties.load(Parsec.class.getClassLoader().getResourceAsStream("parsec.properties"));
    } catch (IOException e) {
      log.error("Could not load parsec.properties, exiting.", e);
      System.exit(1);
    }
    String versionString = parsecProperties.getProperty("version");
    VERSION = Double.parseDouble(versionString.substring(0, 3));
    NAME = parsecProperties.getProperty("name");
  }

  /**
   * Constructs a provider with the specified name, version number,
   * and information.
   */
  protected Parsec() throws IOException {
    super(NAME, VERSION, NAME + " provider, version " + VERSION + ", implementing secure random number generation.");

    // TODO what do we provide here? see https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#SecureRandom
    Parsec.this.put("SecureRandom.NativePRNG", "org.parallaxsecond.parsec.security.provider.SecureRandomParsec");
    Parsec.this.put("SecureRandom.NativePRNG ImplementedIn", "Hardware");
    Parsec.this.put("SecureRandom.NativePRNGBlocking", "org.parallaxsecond.parsec.security.provider.SecureRandomParsec");
    Parsec.this.put("SecureRandom.NativePRNGBlocking ImplementedIn", "Hardware");
    Parsec.this.put("SecureRandom.NativePRNGNonBlocking", "org.parallaxsecond.parsec.security.provider.SecureRandomParsec");
    Parsec.this.put("SecureRandom.NativePRNGNonBlocking ImplementedIn", "Hardware");
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
