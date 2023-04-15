package pt.unl.fct.di.apdc.firstwebapp.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterData {

	public String displayName;
	public String username;
	public String password;
	public String email;
	public String confirmPwd;
	public Role role;
	public Estado estado;

	// Opcionais
	public ProfileType profileType;
	public String phone;
	public String mobilePhone;
	public String occupation;
	public String workplace;
	public String address;
	public String addressComplement;
	public String city;
	public String postalCode;
	public String nif;
	public String photo;

	public enum Role {
		SU, USER, GS, GBO;
	}

	public enum Estado {
		INATIVO, ATIVO;
	}

	public enum ProfileType {
		PUBLIC, PRIVATE
	}

	public RegisterData() {
	}

	public RegisterData(String displayName, String username, String email, String password, String confirmPwd) {

		this.displayName = displayName;
		this.username = username;
		this.password = password;
		this.email = email;
		this.confirmPwd = confirmPwd;
		this.role = Role.USER; // default role
		this.estado = Estado.INATIVO; // default estado

	}

	public boolean validRegistration() {
		boolean isValidEmail = validateEmail(email);
		boolean isPasswordConfirmed = password.equals(confirmPwd);

		return isValidEmail && isPasswordConfirmed;
	}

	private boolean validateEmail(String email) {
	
		String emailPattern = "^[\\w\\.-]+@[\\w\\.-]+\\.[A-Za-z]{2,}$";
		Pattern pattern = Pattern.compile(emailPattern);
		Matcher matcher = pattern.matcher(email);

		return matcher.matches();
	}



}
