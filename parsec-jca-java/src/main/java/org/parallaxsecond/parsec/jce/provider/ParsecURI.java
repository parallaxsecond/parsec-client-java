package org.parallaxsecond.parsec.jce.provider;

import lombok.NonNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to interprete Parsec URI.
 */
public class ParsecURI {
  public static final String PARSEC_SCHEME = "parsec";
  private static final String LABEL_KEY = "object";
  private static final String TYPE_KEY = "type";

  private final URI uri;
  private final Map<String, String> attributeMap = new HashMap<>();

  /**
   * Constructor of Parsec URI.
   *
   * @param str String used to parse parsec attributes
   * @throws URISyntaxException if str is not valid URI
   */
  public ParsecURI(@NonNull String str) throws URISyntaxException {
    this(new URI(str));
  }

  /**
   * Constructor of Parsec URI.
   *
   * @param uri URI used to parse parsec attributes
   */
  public ParsecURI(URI uri) {
    this.uri = uri;
    if (!PARSEC_SCHEME.equalsIgnoreCase(this.uri.getScheme())) {
      throw new IllegalArgumentException(String.format("URI scheme is not %s: %s", PARSEC_SCHEME, uri));
    }
    parseAttributes(this.uri.getSchemeSpecificPart());
  }

  private void parseAttributes(String schemeSpecificPart) {
    String[] attributes = schemeSpecificPart.split(";");
    for (String attribute : attributes) {
      int i = attribute.indexOf('=');
      if (i != -1) {
        attributeMap.put(attribute.substring(0, i).trim(), attribute.substring(i + 1).trim());
      }
    }
  }

  public String getLabel() {
    return attributeMap.get(LABEL_KEY);
  }

  public String getType() {
    return attributeMap.get(TYPE_KEY);
  }

  @Override
  public String toString() {
    return this.uri.toString();
  }

}
