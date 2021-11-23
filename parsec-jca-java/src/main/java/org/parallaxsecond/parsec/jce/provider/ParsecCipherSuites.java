package org.parallaxsecond.parsec.jce.provider;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.parallaxsecond.parsec.protobuf.psa_algorithm.PsaAlgorithm;
import org.parallaxsecond.parsec.protobuf.psa_key_attributes.PsaKeyAttributes;

@RequiredArgsConstructor
public enum ParsecCipherSuites {

    RSA_WITH_PKCS1(
            PsaKeyAttributes.KeyAttributes.newBuilder()
                    .setKeyPolicy(
                            PsaKeyAttributes.KeyPolicy.newBuilder()

                                    .setKeyAlgorithm(

                                            PsaAlgorithm.Algorithm.newBuilder()

                                                    .setAsymmetricSignature(
                                                            PsaAlgorithm.Algorithm
                                                                    .AsymmetricSignature
                                                                    .newBuilder()
                                                                    .setRsaPkcs1V15Sign(
                                                                            PsaAlgorithm.Algorithm.AsymmetricSignature.RsaPkcs1v15Sign.newBuilder()
                                                                                    .setHashAlg(PsaAlgorithm.Algorithm.AsymmetricSignature.SignHash.newBuilder()
                                                                                            .setAny(PsaAlgorithm.Algorithm.AsymmetricSignature.SignHash.Any.newBuilder().build())
                                                                                            .build())
                                                                                    .build()
                                                                    )
                                                                    .build())
                                                    .build())
                                    .setKeyUsageFlags(
                                            PsaKeyAttributes.UsageFlags.newBuilder()
                                                    .setDecrypt(true)
                                                    .setEncrypt(true)
                                                    .setSignMessage(true)
                                                    .setVerifyMessage(true)
                                                    .setVerifyHash(true)
                                                    .setSignHash(true)
                                                    .build())
                                    .build())
                    .setKeyType(
                            PsaKeyAttributes.KeyType.newBuilder()
                                    .setRsaKeyPair(
                                            PsaKeyAttributes.KeyType.RsaKeyPair.newBuilder()
                                                    .build())
                                    .build())
                    .build());
    @Getter
    private final PsaKeyAttributes.KeyAttributes keyAttributes;

}
