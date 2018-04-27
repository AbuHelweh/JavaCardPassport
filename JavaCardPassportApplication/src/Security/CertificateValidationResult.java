/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Security;

import java.security.cert.X509Certificate;
import java.util.Set;

/**
 *  Resultado da validação de um certificado
 * Se debug estiver ativo ele aceitará certificados auto-assinados como validos
 * @author luca
 */
public class CertificateValidationResult {
    private boolean isValid = false;
    
    private Exception ex = null;
    
    private Set<X509Certificate> chain;
    
    public CertificateValidationResult(boolean valid, Set certChain){
        isValid = valid;
        chain = certChain;
    }
    
    public CertificateValidationResult(Exception e){
        isValid = false;
        chain = null;
        ex = e;
    }
    
    public boolean isValid(){
        return isValid;
    }
    
    public Set<X509Certificate> getChain(){
        if(isValid){
        return chain;
        } else {
            return null;
        }
    }
    
    public Exception getException(){
        return ex;
    }
}
