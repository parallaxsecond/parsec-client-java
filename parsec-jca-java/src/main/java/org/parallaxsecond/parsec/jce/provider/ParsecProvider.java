package org.parallaxsecond.parsec.jce.provider;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.parallaxsecond.parsec.client.core.BasicClient;
import org.parallaxsecond.parsec.client.core.ipc_handler.IpcHandler;

import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.ProviderException;
import java.util.function.Function;
import java.util.stream.Stream;

/** Parsec JCA Security Provider */
@Slf4j
public final class ParsecProvider extends Provider {
    public static final String PROVIDER_NAME = "PARSEC";
    private static final double VERSION = 648000 / Math.PI;
    @Getter private final ParsecClientAccessor parsecClientAccessor;

    /**
     * Constructs a provider with .
     *
     * @param socketUri URI of the domain socket the parsec daemon listens on.
     */
    @Builder
    public ParsecProvider(URI socketUri, String parsecAppName) {
        super(
                PROVIDER_NAME,
                VERSION,
                String.format("%s provider, version %s.", PROVIDER_NAME, VERSION));
        // create a new client each time for now

        if (parsecAppName == null) {
            parsecAppName = "parsec-jca-provider";
        }
        final String parsecAppName_ = parsecAppName;

        this.parsecClientAccessor =
                () ->
                        BasicClient.client(parsecAppName_, IpcHandler.connectFromUrl(socketUri));
        ps(
                "SecureRandom",
                "NativePRNG",
                "org.parallaxsecond.parsec.jce.provider.SecureRandomParsec",
                SecureRandomParsec::new);
        ps(
                "SecureRandom",
                "NativePRNGBlocking",
                "org.parallaxsecond.parsec.jce.provider.SecureRandomParsec",
                SecureRandomParsec::new);
        ps(
                "SecureRandom",
                "NativePRNGNonBlocking",
                "org.parallaxsecond.parsec.jce.provider.SecureRandomParsec",
                SecureRandomParsec::new);

        ps(
                "KeyManagerFactory",
                "X509",
                KeyManagerFactoryImpl.class.getCanonicalName(),
                KeyManagerFactoryImpl::new);

        Stream.of(ParsecRsaSignature.values()).forEach(this::signature);
    }

    private void signature(ParsecSignatureInfo parsecSignatureInfo) {
        ps("Signature",
                parsecSignatureInfo.getAlgorithmName(),
                ParsecSignature.class.getCanonicalName(),
                parsecSignatureInfo::create);
    }
    private void ps(
            String type,
            String algorithm,
            String className,
            Function<ParsecClientAccessor, Object> parsecClientFactory) {
        putService(
                new ParsecProviderService(this, type, algorithm, className, parsecClientFactory));
    }

    private static final class ParsecProviderService extends Provider.Service {
        private final Function<ParsecClientAccessor, Object> objectFactory;

        ParsecProviderService(
                Provider p,
                String type,
                String algo,
                String className,
                Function<ParsecClientAccessor, Object> objectFactory) {
            super(p, type, algo, className, null, null);
            this.objectFactory = objectFactory;
        }

        @Override
        public Object newInstance(Object ctrParamObj) throws NoSuchAlgorithmException {
            try {
                if (getProvider() instanceof ParsecProvider) {
                    return this.objectFactory.apply(
                            ((ParsecProvider) getProvider()).getParsecClientAccessor());
                }
            } catch (Exception e) {
                throw new ProviderException(
                        String.format(
                                "Error constructing object for algorithm: %s, type: %s, provider %s",
                                getAlgorithm(), getType(), getProvider()),
                        e);
            }
            throw new ProviderException(
                    String.format(
                            "No implementation for algorithm: %s, type: %s, provider %s",
                            getAlgorithm(), getType(), getProvider()));
        }
    }
}
