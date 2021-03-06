/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Security;

import java.util.HashMap;

/**
 *
 * @author luca
 */
public class PAResult {
    
    private HashMap<Integer, Boolean> checks;
    private boolean ValidSOD;
    
    public PAResult(boolean SODValidity, HashMap<Integer,Boolean> checked){
        this.checks = checked;
        this.ValidSOD = SODValidity;
    }
    
    public boolean SOD(){
        return ValidSOD;
    }
    
    public HashMap<Integer, Boolean> DGS(){
        return checks;
    }
    
    public boolean veredict(){
        return ValidSOD;
    }
    
    public String toString(){
        
        String res = "SOD: " + (ValidSOD? "Valid" : "Invalid") + System.lineSeparator();
        
        for(Integer i = 0; i < 16; i++){
            if(checks.containsKey(i)){
                res += "DG"+ i + " " + checks.get(i) + System.lineSeparator();
            }
        }
        return res;
    }
}
