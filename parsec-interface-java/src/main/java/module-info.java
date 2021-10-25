
module parallax2nd_interface_java {
    requires static lombok;
    requires org.slf4j;
    requires parallax2nd_protobuf;
    requires com.google.protobuf;

    exports org.parallaxsecond.operations;
    exports org.parallaxsecond.operations_protobuf to parrallax2nd_parsec_client_java;
    exports org.parallaxsecond.requests.request to parrallax2nd_parsec_client_java;
    exports org.parallaxsecond.requests.response to parrallax2nd_parsec_client_java;
    exports org.parallaxsecond.requests to parrallax2nd_parsec_client_java;
}