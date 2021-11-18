package org.parallaxsecond.parsec.jce.provider;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.util.Collections;
import java.util.stream.Stream;

@Slf4j
public final class KeyStoreExtensions {

     static Stream<String> aliases(KeyStore keyStore) {
        try {
            return Collections.list(keyStore.aliases()).stream();
        } catch (KeyStoreException e) {
            log.warn("couldn't retrieve aliases in keystore {}", keyStore, e);
            return Stream.empty();
        }
    }

    static WithAlias<Certificate> getCertificate(KeyStore keyStore, String alias) {
        try {
            return new WithAlias<>(alias, keyStore.getCertificate(alias));
        } catch (KeyStoreException e) {
            log.warn("couldn't retrieve certificate for alias {} in keystore {}", alias, keyStore, e);
            return null;
        }
    }
    static KeyStore fromBuilder(KeyStore.Builder builder) {
        try {
            return builder.getKeyStore();
        } catch (KeyStoreException e) {
            log.warn("couldn't build keystore {}", builder, e);
            return null;
        }
    }

    @Value
    public static class WithAlias<T> {
         String alias;
         T object;
         public <U> WithAlias<U> cast(Class<U> clz) {
             return new WithAlias<U>(alias, clz.cast(this.getObject()));
         }
    }
}
