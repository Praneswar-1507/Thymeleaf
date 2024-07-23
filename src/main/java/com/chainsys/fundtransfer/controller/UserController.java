package com.chainsys.fundtransfer.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.chainsys.fundtransfer.dao.UserDAO;
import com.chainsys.fundtransfer.model.BankAccount;
import com.chainsys.fundtransfer.model.User;
import com.chainsys.fundtransfer.validation.LoginValidation;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class UserController {
	@Autowired
	User user;
	@Autowired
	UserDAO userdao;
	@Autowired
	LoginValidation validation;
	@RequestMapping("/home")
	public String homepage() {
		return "home";
	}
	@GetMapping("/loginpage")
	public String loginpage() {
		return "login";
	}
	@GetMapping("/signuppage")
	public String signuppage() {
		return "signup";
	}
	@GetMapping("/approveduserpage")
	public String approvedUserPage(Model model) {
		List<BankAccount> userData = userdao.approvedUsers();
		model.addAttribute("approveduserslist", userData);
		System.out.println(userData);
		return "approvedUsers";
	}
	@GetMapping("/unapprovedUserPage")
	public String unapprovedUserPage(Model model) {
		List<BankAccount> userData = userdao.read();
		model.addAttribute("usersData", userData);
		System.out.println(userData);
		return "unapproved";
	}
	@GetMapping("/adminpage")
	public String admin(Model model) {
		 model.addAttribute("approvedUsersCount",userdao.countApprovedUsers());
		  model.addAttribute("unapprovedUsersCount", userdao.countUnApprovedUsers());
		  model.addAttribute("totalTransactions",userdao.countTransactionHistory() );
		return "admin";
	}

	@PostMapping("/register")
	public String registeruser(@RequestParam("username") String username, @RequestParam("email") String email,
			@RequestParam("password") String password,Model model) {
		if (!validation.validateUsername(username)) {
            model.addAttribute("error", "Invalid username format");
            return "signup";
            
        }
        
        if (!validation.validateEmail(email)) {
            model.addAttribute("error", "Invalid email format");
            return "signup";
        }
       
        if (!validation.validatepassword(password)) {
            model.addAttribute("error", "Passwords format wrong");
            return "signup";
        }
		user.setUsername(username);

		user.setEmail(email);
		user.setPassword(password);

		if (userdao.register(user)) {
			 model.addAttribute("status", "failure");
		        model.addAttribute("message", "You already have an account please login.");
		        return "signup";
		} else {
			 model.addAttribute("status", "success");
		     model.addAttribute("message", "Registered successfully!");
		     return "signup";
		}
	}

	@PostMapping("/login")
	public String loginuser(@RequestParam("email") String email, @RequestParam("password") String password,
			HttpSession session, Model model) {
		User user = userdao.login(email, password);
		if (user != null) {
			session.setAttribute("email", user.getEmail());
			session.setAttribute("username", user.getUsername());
			session.setAttribute("id", user.getId());
			 String aadharId=  userdao.getAadhar(user.getId());
			 System.out.println(aadharId);
			 session.setAttribute("aadharid", aadharId);
			if (email.endsWith("admin@fastpay.com")) {
				  model.addAttribute("approvedUsersCount",userdao.countApprovedUsers());
				  model.addAttribute("unapprovedUsersCount", userdao.countUnApprovedUsers());
				  model.addAttribute("totalTransactions",userdao.countTransactionHistory() );
				return  "admin";
			} else {

				return "home";
			}
		} else {
			model.addAttribute("errorMessage", "you dont have an account please Register");    
			return "login";
		}
	}
	@GetMapping("/logout")
    public String logoutUser(HttpSession session,HttpServletRequest request) {
        session = request.getSession(false);
        if (session != null) {
               session.invalidate(); 
           }

        
       return "redirect:/home";
        
    }
	
	@GetMapping("/logout1")
    public String logoutAdmin(HttpSession session,HttpServletRequest request) {
        session = request.getSession(false);
        if (session != null) {
               session.invalidate(); 
           }

        
       return "redirect:/home";
        
    }

}