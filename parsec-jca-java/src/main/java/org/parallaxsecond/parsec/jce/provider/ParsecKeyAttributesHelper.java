package org.parallaxsecond.parsec.jce.provider;

import org.parallaxsecond.parsec.protobuf.psa_key_attributes.PsaKeyAttributes;

public interface ParsecKeyAttributesHelper {

    static String algorithm(PsaKeyAttributes.KeyAttributes keyAttributes) {
        if (keyAttributes.getKeyType().hasRsaKeyPair()) {
            return "RSA";
        }
        return null;
    }
    static String format(PsaKeyAttributes.KeyAttributes keyAttributes) {
        return "RAW";
    }


}
