package br.ufsc.labsec.server;

import javax.swing.JOptionPane;

public class AR {
	public boolean sendData(String dob, String doe, String docCode, String docNum, String issuingState, String nationality, String surname, String name, String gender) { //passar resto dos dados
		//envia pro sgci
		//retorna true/false
		JOptionPane.showMessageDialog(null, "Na AR: "+ nationality + " " + name);
		return true;
	}
}
