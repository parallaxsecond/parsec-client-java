module parallax2nd_protobuf {
    requires com.google.protobuf;

    // FIXME don't export to parrallax2nd_parsec_client_java
    exports org.parallaxsecond.parsec.internal.protobuf.psa_algorithm to parallax2nd_interface_java,parrallax2nd_parsec_client_java;
    // FIXME don't export to parrallax2nd_parsec_client_java
    exports org.parallaxsecond.parsec.internal.protobuf.psa_raw_key_agreement to parallax2nd_interface_java,parrallax2nd_parsec_client_java;

    exports org.parallaxsecond.parsec.internal.protobuf.attest_key to parallax2nd_interface_java;
    exports org.parallaxsecond.parsec.internal.protobuf.can_do_crypto to parallax2nd_interface_java;
    exports org.parallaxsecond.parsec.internal.protobuf.delete_client to parallax2nd_interface_java;
    exports org.parallaxsecond.parsec.internal.protobuf.list_authenticators to parallax2nd_interface_java;
    exports org.parallaxsecond.parsec.internal.protobuf.list_clients to parallax2nd_interface_java;
    exports org.parallaxsecond.parsec.internal.protobuf.list_keys to parallax2nd_interface_java;
    exports org.parallaxsecond.parsec.internal.protobuf.list_opcodes to parallax2nd_interface_java;
    exports org.parallaxsecond.parsec.internal.protobuf.list_providers to parallax2nd_interface_java;
    exports org.parallaxsecond.parsec.internal.protobuf.ping to parallax2nd_interface_java;
    exports org.parallaxsecond.parsec.internal.protobuf.prepare_key_attestation to parallax2nd_interface_java;
    exports org.parallaxsecond.parsec.internal.protobuf.psa_aead_decrypt to parallax2nd_interface_java;
    exports org.parallaxsecond.parsec.internal.protobuf.psa_aead_encrypt to parallax2nd_interface_java;
    exports org.parallaxsecond.parsec.internal.protobuf.psa_asymmetric_decrypt to parallax2nd_interface_java;
    exports org.parallaxsecond.parsec.internal.protobuf.psa_asymmetric_encrypt to parallax2nd_interface_java;
    exports org.parallaxsecond.parsec.internal.protobuf.psa_cipher_decrypt to parallax2nd_interface_java;
    exports org.parallaxsecond.parsec.internal.protobuf.psa_cipher_encrypt to parallax2nd_interface_java;
    exports org.parallaxsecond.parsec.internal.protobuf.psa_destroy_key to parallax2nd_interface_java;
    exports org.parallaxsecond.parsec.internal.protobuf.psa_export_key to parallax2nd_interface_java;
    exports org.parallaxsecond.parsec.internal.protobuf.psa_export_public_key to parallax2nd_interface_java;
    exports org.parallaxsecond.parsec.internal.protobuf.psa_generate_key to parallax2nd_interface_java;
    exports org.parallaxsecond.parsec.internal.protobuf.psa_generate_random to parallax2nd_interface_java;
    exports org.parallaxsecond.parsec.internal.protobuf.psa_hash_compare to parallax2nd_interface_java;
    exports org.parallaxsecond.parsec.internal.protobuf.psa_hash_compute to parallax2nd_interface_java;
    exports org.parallaxsecond.parsec.internal.protobuf.psa_import_key to parallax2nd_interface_java;
    exports org.parallaxsecond.parsec.internal.protobuf.psa_key_attributes to parallax2nd_interface_java, parrallax2nd_parsec_client_java;
    exports org.parallaxsecond.parsec.internal.protobuf.psa_mac_compute to parallax2nd_interface_java;
    exports org.parallaxsecond.parsec.internal.protobuf.psa_mac_verify to parallax2nd_interface_java;
    exports org.parallaxsecond.parsec.internal.protobuf.psa_sign_hash to parallax2nd_interface_java;
    exports org.parallaxsecond.parsec.internal.protobuf.psa_sign_message to parallax2nd_interface_java;
    exports org.parallaxsecond.parsec.internal.protobuf.psa_verify_hash to parallax2nd_interface_java;
    exports org.parallaxsecond.parsec.internal.protobuf.psa_verify_message to parallax2nd_interface_java;


}