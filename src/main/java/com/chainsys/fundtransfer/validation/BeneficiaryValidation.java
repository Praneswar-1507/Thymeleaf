package com.chainsys.fundtransfer.validation;

public class BeneficiaryValidation {
	public boolean validateUsername(String firstName) {
	       return firstName.matches("[A-Za-z]+");
	    }
	public boolean validateLastname(String lastName) {
	       return lastName.matches("[A-Za-z]+");
	    }
}
