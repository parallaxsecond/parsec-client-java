package org.parallaxsecond.parsec.jce.provider;

import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.SignatureSpi;
import java.util.Arrays;

import org.parallaxsecond.parsec.client.exceptions.ClientException;
import org.parallaxsecond.parsec.client.exceptions.ServiceException;
import org.parallaxsecond.parsec.protocol.operations.NativeResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@RequiredArgsConstructor
@Slf4j
public final class ParsecSignature extends SignatureSpi {
  private final ParsecSignatureInfo signatureInfo;
  private final ParsecClientAccessor parsecClientAccessor;
  private String keyName;
  private MessageDigest messageDigest;
  private Signature verifyerDelegate;

  @Override
  protected void engineInitSign(PrivateKey privateKey) throws InvalidKeyException {
    if (!(privateKey instanceof ParsecRsaPrivateKey)) {
      throw new InvalidKeyException(String.format("Invalid key, expected a key of type %s.",
          ParsecRsaPrivateKey.class.getName()));
    }
    this.keyName = ((ParsecRsaPrivateKey) privateKey).getParsecName();
    this.messageDigest = makeMessageDigest();
  }

  private MessageDigest makeMessageDigest() throws InvalidKeyException {
    try {
      return this.signatureInfo.getMessageDigestFactory().create();
    } catch (NoSuchAlgorithmException e) {
      String message =
          String.format("Error creating associated message digest, key: %s, signatureInfo: %s",
              keyName, this.signatureInfo);
      log.debug(message);
      throw new InvalidKeyException(message, e);
    }
  }

  @Override
  protected void engineUpdate(byte b) throws SignatureException {
    if (verifyerDelegate != null) {
      verifyerDelegate.update(b);
    } else {
      this.messageDigest.update(b);
    }
  }

  @Override
  protected void engineUpdate(byte[] b, int off, int len) throws SignatureException {
    if (verifyerDelegate != null) {
      verifyerDelegate.update(b, off, len);
    } else {
      this.messageDigest.update(b, off, len);
    }
  }

  @Override
  protected byte[] engineSign() throws SignatureException {
    byte[] digest = this.messageDigest.digest();
    try {
      NativeResult.PsaSignHashResult r = parsecClientAccessor.get().psaSignHash(keyName, digest,
          signatureInfo.getParsecAlgorithm());
      log.info(String.format("Signed with algorithm %s", signatureInfo.getAlgorithmName()));

      return r.getSignature();
    } catch (ServiceException | ClientException e) {
      throw new SignatureException("error signing value, signatureInfo: " + signatureInfo, e);
    }
  }

  @Override
  protected void engineInitVerify(PublicKey publicKey) throws InvalidKeyException {
    if (publicKey instanceof ParsecPublicKey) {
      this.keyName = ((ParsecPublicKey) publicKey).getParsecName();
      this.messageDigest = makeMessageDigest();
    } else {
      // TODO: this uses internal APIs and is replaced for now.
      // Make sure the replacing code below is correct!
      // Provider.Service service = Providers.getFullProviderList()
      // .getServices("Signature", signatureInfo.getAlgorithmName()).stream()
      // .filter(s -> !ParsecProvider.PROVIDER_NAME.equals(s.getProvider().getName())).findFirst()
      // .orElseThrow(() -> new InvalidKeyException("couldn't find a provider to delegate to"));

      Provider[] providers = Security.getProviders();
      Provider.Service service = Arrays.stream(providers).flatMap(p -> p.getServices().stream())
          .filter(s -> "Signature".equals(s.getType())
              && signatureInfo.getAlgorithmName().equals(s.getAlgorithm())
              && !ParsecProvider.PROVIDER_NAME.equals(s.getProvider().getName()))
          .findFirst()
          .orElseThrow(() -> new InvalidKeyException("couldn't find a provider to delegate to"));
      try {
        this.verifyerDelegate =
            Signature.getInstance(service.getAlgorithm(), service.getProvider());
        this.verifyerDelegate.initVerify(publicKey);
      } catch (NoSuchAlgorithmException e) {
        throw new InvalidKeyException(e);
      }
    }
  }

  @Override
  protected boolean engineVerify(byte[] sigBytes) throws SignatureException {
    if (verifyerDelegate != null) {
      boolean valid = verifyerDelegate.verify(sigBytes);
      log.info("signature valid: {}", valid);
      return valid;
    }
    byte[] digest = this.messageDigest.digest();
    try {
      parsecClientAccessor.get().psaVerifyHash(keyName, digest, signatureInfo.getParsecAlgorithm(),
          sigBytes);
      return true;
    } catch (ServiceException | ClientException e) {
      throw new SignatureException("error verifying value, signatureInfo: " + signatureInfo, e);
    }
  }

  @Override
  protected void engineSetParameter(String param, Object value) throws InvalidParameterException {}

  @Override
  protected Object engineGetParameter(String param) throws InvalidParameterException {
    return null;
  }
}
