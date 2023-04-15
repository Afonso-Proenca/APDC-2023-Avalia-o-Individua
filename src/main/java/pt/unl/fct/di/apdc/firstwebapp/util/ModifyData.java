package pt.unl.fct.di.apdc.firstwebapp.util;

import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData.Estado;
import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData.ProfileType;
import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData.Role;

public class ModifyData {
    public String username;
    public String password;
    public String targetUsername;
    
    // superusers
    
    public String email;
    public String displayName;
    public Estado estado;
    public Role role;
    
    // everyone
    
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

    public ModifyData() {
    }

    public ModifyData(String username, String password, String targetUsername) {
        this.username = username;
        this.password = password;
        this.targetUsername = targetUsername;
    }
}
