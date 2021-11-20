package org.parallaxsecond.parsec.jce.provider;

import java.security.*;

/**
 *
 */
public class RSASignature extends SignatureSpi {
  @Override
  protected void engineInitVerify(PublicKey publicKey) throws InvalidKeyException {
    throw new IllegalStateException("not implemented"); // FIXME
  }

  @Override
  protected void engineInitSign(PrivateKey privateKey) throws InvalidKeyException {
    throw new IllegalStateException("not implemented"); // FIXME
  }

  @Override
  protected void engineUpdate(byte b) throws SignatureException {
    throw new IllegalStateException("not implemented"); // FIXME
  }

  @Override
  protected void engineUpdate(byte[] b, int off, int len) throws SignatureException {
    throw new IllegalStateException("not implemented"); // FIXME
  }

  @Override
  protected byte[] engineSign() throws SignatureException {
    throw new IllegalStateException("not implemented"); // FIXME
    // return new byte[0];
  }

  @Override
  protected boolean engineVerify(byte[] sigBytes) throws SignatureException {
    throw new IllegalStateException("not implemented"); // FIXME
    // return false;
  }

  @Override
  protected void engineSetParameter(String param, Object value) throws InvalidParameterException {
    throw new UnsupportedOperationException("setParameter() not supported");
  }

  @Override
  protected Object engineGetParameter(String param) throws InvalidParameterException {
    throw new UnsupportedOperationException("getParameter() not supported");
  }
}
