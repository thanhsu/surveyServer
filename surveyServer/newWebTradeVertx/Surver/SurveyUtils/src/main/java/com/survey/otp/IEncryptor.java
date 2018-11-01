package com.survey.otp;

public interface IEncryptor {


  /**
   * Returns the shared secret in the encrypted format.
   * 
   * @param sharedSecret shared secret to encrypt
   * @return encrypted shared secret
   * @throws OTPManagerException if a failure occur while encrypting the shared secret
   */
  String encrypt(String sharedSecret) throws OTPManagerException;

  /**
   * Returns the shared secret in decrypted format.
   * 
   * @param sharedSecret encrypted shared sectet
   * @return decrypted shared secret
   * @throws OTPManagerException if a failure occur while decrypting the shared secret
   */
  String decrypt(String sharedSecret) throws OTPManagerException;
}
