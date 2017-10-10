package br.ufsc.labsec;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.sourceforge.scuba.data.Gender;

import org.jmrtd.Passport;

public class ParserData {
	Passport passport;
	private String dob;
	private String doe;
	private String docCode;
	private String docNum;
	private String issuingState;
	private String nationality;
	private String surname;
	private String name;
	private Gender gender;
	
	public ParserData(Passport passport) throws ParseException, IOException {
		this.passport = passport;
		parse();
	}

	public String formatDate(String strDate) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyMMdd");
        Date dateStr = formatter.parse(strDate);
        String formattedDate = formatter.format(dateStr);
        System.out.println("yyMMdd date is ==>"+formattedDate);
        Date date1 = formatter.parse(formattedDate);

        formatter = new SimpleDateFormat("ddMMyyyy");
        formattedDate = formatter.format(date1);
        System.out.println("ddMMyyyy date is ==>"+formattedDate);
        return formattedDate;
	}
	
	public String parseName() throws IOException {
		StringBuffer nameStr = new StringBuffer();
		String[] firstNames = passport.getLDS().getDG1File().getMRZInfo().getSecondaryIdentifierComponents();
		for (int i = 0; i < firstNames.length; i++) {
			nameStr.append(firstNames[i]);
			if (i < (firstNames.length - 1)) { nameStr.append(" "); }
		}
		
		return nameStr.toString();
	}
	
	public void parse() throws ParseException, IOException {
		dob = formatDate(passport.getLDS().getDG1File().getMRZInfo().getDateOfBirth());
		doe = passport.getLDS().getDG1File().getMRZInfo().getDateOfExpiry();
		docCode = passport.getLDS().getDG1File().getMRZInfo().getDocumentCode();
		docNum = passport.getLDS().getDG1File().getMRZInfo().getDocumentNumber();
		issuingState = passport.getLDS().getDG1File().getMRZInfo().getIssuingState();
		nationality = passport.getLDS().getDG1File().getMRZInfo().getNationality();
		surname = passport.getLDS().getDG1File().getMRZInfo().getPrimaryIdentifier();
		//String name = passport.getLDS().getDG1File().getMRZInfo().getSecondaryIdentifier().replaceAll("<","");
		name = parseName();
		gender = passport.getLDS().getDG1File().getMRZInfo().getGender();
	}
	//para outros usos. O nosso eh um server?
//	Object[] parametros = new Object[]{dob, doe, docCode, docNum, issuingState, nationality, surname, name, gender.toString()};
//	Object resultado = client.executar("sendData", parametros); 
//	JOptionPane.showMessageDialog(null, resultado.toString());
	
	public String getDob(){
		return dob;
	}
	public String getNationality(){
		return nationality;
	}
	public String getSurname(){
		return surname;
	}
	public String getName(){
		return name;
	}
}
