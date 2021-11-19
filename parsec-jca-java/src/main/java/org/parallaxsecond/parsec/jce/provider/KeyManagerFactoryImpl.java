package org.parallaxsecond.parsec.jce.provider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactorySpi;
import javax.net.ssl.KeyStoreBuilderParameters;
import javax.net.ssl.ManagerFactoryParameters;
import java.security.*;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public final class KeyManagerFactoryImpl extends KeyManagerFactorySpi {
    private final ParsecClientAccessor parsecClientAccessor;
    private KeyManager keyManager;
    private boolean isInitialized;

    @Override
    protected void engineInit(KeyStore ks, char[] password) throws KeyStoreException {
                    throw new KeyStoreException(ParsecProvider.PROVIDER_NAME
                            + " KeyManager doesn't use #init(Keystore ks, char[] password)");
    }

    @Override
    protected void engineInit(ManagerFactoryParameters params) throws InvalidAlgorithmParameterException {
        if (!(params instanceof KeyStoreBuilderParameters)) {
            throw new InvalidAlgorithmParameterException("Parameters must be instance of KeyStoreBuilderParameters");
        } else {
            List<KeyStore.Builder> builders = ((KeyStoreBuilderParameters) params).getParameters();
            this.keyManager = new X509KeyManagerImpl(parsecClientAccessor, builders);
            this.isInitialized = true;
        }
    }

    @Override
    protected KeyManager[] engineGetKeyManagers() {
        if (!isInitialized) {
            throw new IllegalStateException("engineInit(...) not called yet");
        }
        return new KeyManager[] {this.keyManager};
    }
}
