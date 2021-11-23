package org.parallaxsecond.parsec.jce.provider;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.parallaxsecond.parsec.protobuf.psa_algorithm.PsaAlgorithm;
import org.parallaxsecond.parsec.protobuf.psa_algorithm.PsaAlgorithm.Algorithm.AsymmetricSignature;

import java.nio.ByteBuffer;
import java.security.MessageDigest;

@RequiredArgsConstructor
public enum ParsecRsaSignature implements ParsecSignatureInfo {
    // keep this in order of priority
    SHA512_WITH_RSA("SHA512withRSA",
        pkcs1WithHash(PsaAlgorithm.Algorithm.Hash.SHA_512),
        () -> MessageDigest.getInstance("SHA-512")),

    SHA256_WITH_RSA("SHA256withRSA",
        pkcs1WithHash(PsaAlgorithm.Algorithm.Hash.SHA_256),
        () -> MessageDigest.getInstance("SHA-256")),

    SHA256_PRECALCULATED_WITH_RSA("SHA256PrecalculatedWithRSA",
        pkcs1WithHash(PsaAlgorithm.Algorithm.Hash.SHA_256),
        () -> MessageDigest.getInstance("None")),
    ;

    @Getter private final String algorithmName;
    @Getter private final AsymmetricSignature parsecAlgorithm;
    @Getter private final MessageDigestFactory messageDigestFactory;

    private static AsymmetricSignature pkcs1WithHash(PsaAlgorithm.Algorithm.Hash hash) {
        return AsymmetricSignature.newBuilder()
            .setRsaPkcs1V15Sign(AsymmetricSignature.RsaPkcs1v15Sign.newBuilder()
                .setHashAlg(AsymmetricSignature.SignHash.newBuilder()
                    .setSpecific(hash)
                    .build())
                .build())
            .build();
    }

    private static AsymmetricSignature pkcs1() {
        return AsymmetricSignature.newBuilder()
            .setRsaPkcs1V15Sign(AsymmetricSignature.RsaPkcs1v15Sign.newBuilder()
                .build())
            .build();
    }

    @Override
    public ParsecSignature create(ParsecClientAccessor parsecClientAccessor) {
        //AsymmetricSignature.newBuilder().setRsaPss(AsymmetricSignature.RsaPss.newBuilder()
        return new ParsecSignature(this, parsecClientAccessor);
    }


}
