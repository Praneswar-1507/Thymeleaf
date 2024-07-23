package com.chainsys.fundtransfer.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.chainsys.fundtransfer.dao.UserDAO;
import com.chainsys.fundtransfer.model.BankAccount;
import com.chainsys.fundtransfer.validation.AccountValidation;

import jakarta.servlet.http.HttpSession;

@Controller
public class BankAccountController {
	@Autowired
	BankAccount account;
	@Autowired
	UserDAO userdao;
	@Autowired
	AccountValidation accountvalidation;
	
	@GetMapping("/userprofilepage")
	public String userprofilepage(Model model,HttpSession session ) {
		BankAccount bankaccount = userdao.getUserDetails((int)session.getAttribute("id"));
		model.addAttribute("userAccount", bankaccount);
		System.out.println(bankaccount);
		return "userProfile";
	}
	@PostMapping("/bankAccount")
	public String createAccountPage()
	{
		return "bankAccount";
		
	}


	@PostMapping("/createbankaccount")
	public String CreateBankAccount(@RequestParam("firstname") String firstName,
			@RequestParam("lastname") String lastName, @RequestParam("phonenum") String PhoneNumber,
			@RequestParam("dob") String dob, @RequestParam("aadharnumber") String aadharNumber,
			@RequestParam("address") String address, @RequestParam("id") int id,Model model,HttpSession session) {
		if (!accountvalidation.validateFirstName(firstName)) {
            model.addAttribute("error", "Invalid firstname format");
            return "signup";
            
        }
        
        if (!accountvalidation.validateLastName(lastName)) {
            model.addAttribute("error", "Invalid lastname format");
            return "signup";
        }
       
        if (!accountvalidation.validateMobile(PhoneNumber)) {
            model.addAttribute("error", "phonenumber format wrong");
            return "signup";
        }
        if (!accountvalidation.validateAadhar(aadharNumber)) {
            model.addAttribute("error", "aadhar format wrong");
            return "signup";
        }
		account.setFirstName(firstName);
		account.setLastName(lastName);
		account.setPhoneNumber(PhoneNumber);
		account.setDate(dob);
		account.setAadharNumber(aadharNumber);
		account.setAddress(address);
		account.setUserId(id);
		if (userdao.createAccount(account)) {
			 model.addAttribute("status", "failure");
		        model.addAttribute("message", "Account already exists.");
		        return "BankAccount";
	    } else {
	        model.addAttribute("status", "success");
	        model.addAttribute("message", "Account created successfully!");
	      String aadharId=  userdao.getAadhar(id);
	        session.setAttribute("aadharid", aadharId);
	        return "BankAccount";
	    }
		
	    

	}

	@PostMapping("/approveUser")
	public String approveUser(@RequestParam("generateid") int userId, Model model) {
	    Random random = new Random();
	    String firstEightDigits = "12345678";
	    String accountNum;
	    String userIfsc;
	    
	    Set<String> existingAccountIds = new HashSet<>();
	    Set<String> existingIfscs = new HashSet<>();
	    List<String> existingAccountIdsList = userdao.getExistingAccountIds();
	    System.out.println(existingAccountIdsList);
	    List<String> existingIfscsList = userdao.getExistingIfscs();
	    System.out.println(existingIfscsList);
	    existingAccountIds.addAll(existingAccountIdsList);
	    existingIfscs.addAll(existingIfscsList);

	    while (true) {
	        StringBuilder accountNumber = new StringBuilder(firstEightDigits);
	        for (int i = 0; i < 4; i++) {
	            int randomDigit = random.nextInt(10);
	            accountNumber.append(randomDigit);
	        }
	        accountNum = accountNumber.toString();

	        StringBuilder ifsc = new StringBuilder();
	        String pattern = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	        for (int i = 0; i < 4; i++) {
	            int randomIndex = random.nextInt(pattern.length());
	            char randomChar = pattern.charAt(randomIndex);
	            ifsc.append(randomChar);
	        }
	        ifsc.append('0');
	        for (int i = 0; i < 6; i++) {
	            int randomDigit = random.nextInt(10);
	            ifsc.append(randomDigit);
	        }
	        userIfsc = ifsc.toString();

	        if (!existingAccountIds.contains(accountNum) && !existingIfscs.contains(userIfsc)) {
	            break;
	        }
	    }
	    account.setAccountId(accountNum);
	    account.setIfscCode(userIfsc);
	    account.setUserId(userId);

	    userdao.userApproval(account);
	    List<BankAccount> userData = userdao.approvedUsers();
	    model.addAttribute("approveduserslist", userData);
	    return "approvedUsers";
	}

	@PostMapping("/userprofile")
	public String getUserDetails(@RequestParam("id") int userId, Model model,HttpSession session) {
		String accountId = userdao.getAccountId(userId);
		BankAccount bankaccount = userdao.getUserDetails(userId);
		model.addAttribute("userAccount", bankaccount);
		int count=userdao.countMoneyRequest(accountId);
		model.addAttribute("count", count);
		int creditPoints=userdao.getCreditPoints(userId);
		session.setAttribute("creditpoints", creditPoints);
		return "userProfile";

	}

	@PostMapping("/updatephonenumber")
	public String updatePhoneNumber(@RequestParam("phonenumber") int userId,
			@RequestParam("phoneNumber") String phoneNumber, Model model) {
		System.out.println(userId);
		userdao.updatePhoneNumber(userId, phoneNumber);
		BankAccount bankaccount = userdao.getUserDetails(userId);
		model.addAttribute("userAccount", bankaccount);
		return "userProfile";

	}

	@PostMapping("/updateaddress")
	public String updateAddress(@RequestParam("address") int userId, @RequestParam("addressValue") String address,
			Model model) {
		userdao.updateAddress(userId, address);
		BankAccount bankaccount = userdao.getUserDetails(userId);
		model.addAttribute("userAccount", bankaccount);
		return "userProfile";

	}

}