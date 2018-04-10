/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Security;

import org.jmrtd.protocol.CAResult;
import org.jmrtd.protocol.TAResult;

/**
 *
 * @author luca
 */
public class EACResult {
    
    CAResult CA;
    TAResult TA;
    
    public EACResult(CAResult cares, TAResult tares){
        CA = cares;
        TA = tares;
    }
    
    public String toString(){
        return CA.toString() + System.lineSeparator() + TA.toString();
    }
}
