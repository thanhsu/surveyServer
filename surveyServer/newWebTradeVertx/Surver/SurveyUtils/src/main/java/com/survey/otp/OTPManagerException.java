package com.survey.otp;

public class OTPManagerException extends Exception {


  private static final long serialVersionUID = 1L;

  /**
   * Constructs an <B>OTPManagerException</B> with a specified detail message.
   * 
   * @param message the detail message
   */
  public OTPManagerException(String message) {
    super(message);

  }

  /**
   * Constructs an <B>OTPManagerException</B> with a detail message and the specified Throwable.
   * 
   * @param message the detail message
   * @param cause the undeclared checked exception that was thrown
   */
  public OTPManagerException(String message, Throwable cause) {
    super(message, cause);
  }

}
