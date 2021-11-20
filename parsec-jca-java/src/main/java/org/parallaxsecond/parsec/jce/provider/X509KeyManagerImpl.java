package org.parallaxsecond.parsec.jce.provider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.parallaxsecond.parsec.jce.provider.KeyStoreExtensions.WithAlias;
import org.parallaxsecond.parsec.protobuf.psa_key_attributes.PsaKeyAttributes;

import javax.net.ssl.X509KeyManager;
import javax.security.auth.x500.X500Principal;
import java.net.Socket;
import java.security.KeyStore;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Slf4j
public final class X509KeyManagerImpl implements X509KeyManager {
    private final ParsecClientAccessor parsecClientFactory;
    private final List<KeyStore.Builder> builders;

    private Stream<WithAlias<X509Certificate>> certificates(String[] keyTypes, Principal[] issuers)  {
        return Stream.of(keyTypes)
                .flatMap(keyType -> certificates(keyType, issuers));
    }
    private X509Certificate bySubjectX500Principal(X500Principal cn) {
        return builders.stream()
                .flatMap(ksb -> KeyStoreExtensions.allCertificates(KeyStoreExtensions.fromBuilder(ksb)))
                .map(WithAlias::getObject)
                .filter(c->c.getSubjectX500Principal() != null)
                .filter(c->c.getSubjectX500Principal().equals(cn))
                .findFirst()
                .orElse(null);
    }

    private Stream<WithAlias<X509Certificate>> certificates(String keyType, Principal[] issuers)  {
        Set<X500Principal> issuerSet = Arrays.stream(issuers)
                .filter(i->i instanceof X500Principal)
                .map(i -> (X500Principal)i )
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return builders.stream()
                .flatMap(ksb -> certificates(KeyStoreExtensions.fromBuilder(ksb), keyType, issuerSet));
    }


    private Stream<X509Certificate> certificatesForAlias(String alias){
        return builders.stream()
                .map(ksb -> KeyStoreExtensions.getCertificate(KeyStoreExtensions.fromBuilder(ksb), alias))
                .filter(Objects::nonNull)
                .map(WithAlias::getObject)
                .filter(X509Certificate.class::isInstance)
                .map(X509Certificate.class::cast);
    }

    private static Stream<WithAlias<X509Certificate>> certificates(KeyStore keyStore, String keyType, Set<X500Principal> issuers) {
        return KeyStoreExtensions.allCertificates(keyStore)
                .filter(c -> c.getObject().getPublicKey() != null)
                .filter(c -> c.getObject().getPublicKey().getAlgorithm() != null)
                .filter(c -> c.getObject().getIssuerX500Principal() != null)
                .filter(c -> keyType == null || c.getObject().getPublicKey().getAlgorithm().equals(keyType))
                .filter(c -> issuers.contains(c.getObject().getIssuerX500Principal()));
    }

    @Override
    public String[] getClientAliases(String keyType, Principal[] issuers) {
        return certificates(keyType, issuers)
                .map(WithAlias::getAlias)
                .toArray(String[]::new);
    }

    @Override
    public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
        return certificates(keyType, issuers)
                .map(WithAlias::getAlias)
                .findFirst()
                .orElse(null);
    }

    @Override
    public String[] getServerAliases(String keyType, Principal[] issuers) {
        return certificates(keyType, issuers)
                .map(WithAlias::getAlias)
                .toArray(String[]::new);
    }

    @Override
    public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
        return certificates(keyType, issuers)
                .map(WithAlias::getAlias)
                .findFirst()
                .orElse(null);
    }

    @Override
    public X509Certificate[] getCertificateChain(String alias) {
        X509Certificate current = certificatesForAlias(alias)
                .findFirst()
                .orElse(null);
        if (current == null) {
            return null;
        }
        List<X509Certificate> chain = new ArrayList<>();
        while (current != null && !chain.contains(current)) {
            chain.add(current);
            current = bySubjectX500Principal(current.getIssuerX500Principal());
        }
        return chain.toArray(new X509Certificate[0]);
    }

    @Override
    public PrivateKey getPrivateKey(String alias) {
        X509Certificate certificate = certificatesForAlias(alias).findFirst().orElse(null);
        if (certificate == null) {
            return null;
        }

        return parsecClientFactory.get().listKeys().getKeys().stream()
                .filter(k -> k.getName().equals(alias))
                .filter(k-> ParsecKeyAttributesHelper.algorithm(k.getAttributes())!= null)
                .findFirst()

                .map(keyInfo -> {
                    String algorithm = ParsecKeyAttributesHelper.algorithm(keyInfo.getAttributes());
                    String format = ParsecKeyAttributesHelper.format(keyInfo.getAttributes());
                    switch (algorithm) {
                        case "RSA":
                            if (! (certificate.getPublicKey() instanceof RSAPublicKey)) {
                                return null;
                            }
                            return new ParsecRsaPrivateKey(
                                    keyInfo.getName(),
                                    algorithm,
                                    format,
                                    ((RSAPublicKey)certificate.getPublicKey()).getModulus());
                        default:
                            throw new IllegalStateException("unsupported key algorithm " + algorithm);
                    }

                })
                .orElse(null);
    }
}
