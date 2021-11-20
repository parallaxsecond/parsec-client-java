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
    public ParsecProvider(URI socketUri) {
        super(
                PROVIDER_NAME,
                VERSION,
                String.format("%s provider, version %s.", PROVIDER_NAME, VERSION));
        // create a new client each time for now
        this.parsecClientAccessor =
                () ->
                        BasicClient.client(
                                "parsec-jca-provider", IpcHandler.connectFromUrl(socketUri));
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
                "org.parallaxsecond.parsec.jce.provider.KeyManagerFactoryImpl",
                KeyManagerFactoryImpl::new);

        ps(
                "Signature",
                "MD2withRSA",
                "org.parallaxsecond.parsec.jce.provider.ParsecRsaSignature$MD2withRSA",
                ParsecRsaSignature.MD2withRSA::new);
        ps(
                "Signature",
                "MD5withRSA",
                "org.parallaxsecond.parsec.jce.provider.ParsecRsaSignature$MD5withRSA",
                ParsecRsaSignature.MD5withRSA::new);

        ps(
                "Signature",
                "SHA1withRSA",
                "org.parallaxsecond.parsec.jce.provider.ParsecRsaSignature$SHA1withRSA",
                ParsecRsaSignature.SHA1withRSA::new);

        ps(
                "Signature",
                "SHA224withRSA",
                "org.parallaxsecond.parsec.jce.provider.ParsecRsaSignature$SHA224withRSA",
                ParsecRsaSignature.SHA224withRSA::new);

        ps(
                "Signature",
                "SHA256withRSA",
                "org.parallaxsecond.parsec.jce.provider.ParsecRsaSignature$SHA256withRSA",
                ParsecRsaSignature.SHA256withRSA::new);

        ps(
                "Signature",
                "SHA384withRSA",
                "org.parallaxsecond.parsec.jce.provider.ParsecRsaSignature$SHA384withRSA",
                ParsecRsaSignature.SHA384withRSA::new);

        ps(
                "Signature",
                "SHA512withRSA",
                "org.parallaxsecond.parsec.jce.provider.ParsecRsaSignature$SHA512withRSA",
                ParsecRsaSignature.SHA512withRSA::new);

        ps(
                "Signature",
                "SHA512/224withRSA",
                "org.parallaxsecond.parsec.jce.provider.ParsecRsaSignature$SHA512_224withRSA",
                ParsecRsaSignature.SHA512_224withRSA::new);

        ps(
                "Signature",
                "SHA512/256withRSA",
                "org.parallaxsecond.parsec.jce.provider.ParsecRsaSignature$SHA512_256withRSA",
                ParsecRsaSignature.SHA512_256withRSA::new);

        ps(
                "Signature",
                "RSASSA-PSS",
                "org.parallaxsecond.parsec.jce.provider.ParsecRsaSignature$RSAPSSSignature",
                ParsecRsaSignature.RSAPSSSignature::new);
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
