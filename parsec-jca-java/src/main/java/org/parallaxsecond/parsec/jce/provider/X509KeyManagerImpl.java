package org.parallaxsecond.parsec.jce.provider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.parallaxsecond.parsec.jce.provider.KeyStoreExtensions.WithAlias;

import javax.net.ssl.X509KeyManager;
import javax.security.auth.x500.X500Principal;
import java.net.Socket;
import java.security.KeyStore;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
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
    private Stream<WithAlias<X509Certificate>> certificates(String keyType, Principal[] issuers)  {
        Set<X500Principal> issuerSet = Arrays.stream(issuers)
                .filter(i->i instanceof X500Principal)
                .map(i -> (X500Principal)i )
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return builders.stream()
                .flatMap(ksb -> certificates(KeyStoreExtensions.fromBuilder(ksb), keyType, issuerSet));
    }

    private static Stream<WithAlias<X509Certificate>> certificates(KeyStore keyStore, String keyType, Set<X500Principal> issuers) {
        return KeyStoreExtensions.aliases(keyStore)
                .map(a -> KeyStoreExtensions.getCertificate(keyStore, a))
                .filter(Objects::nonNull)
                .filter(c -> c.getObject() instanceof X509Certificate)
                .map(c -> c.cast(X509Certificate.class))
                .filter(c -> c.getObject().getPublicKey() != null)
                .filter(c -> c.getObject().getPublicKey().getAlgorithm() != null)
                .filter(c -> c.getObject().getIssuerX500Principal() != null)
                .filter(c -> c.getObject().getPublicKey().getAlgorithm().equals(keyType))
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
        throw new IllegalStateException("not implemented");
    }

    @Override
    public PrivateKey getPrivateKey(String alias) {
        throw new IllegalStateException("not implemented");
    }
}
