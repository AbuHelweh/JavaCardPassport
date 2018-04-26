/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Security;

/**
 * This class wraps an exception that could be thrown during
 * the certificate verification process.
 * 
 * @author Svetlin Nakov
 */
public class CertificateValidationException extends Exception {
    private static final long serialVersionUID = 1L;
 
    public CertificateValidationException(String message, Throwable cause) {
        super(message, cause);
    }
 
    public CertificateValidationException(String message) {
        super(message);
    }
}