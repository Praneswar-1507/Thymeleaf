package com.chainsys.fundtransfer.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.chainsys.fundtransfer.dao.UserDAO;
import com.chainsys.fundtransfer.model.BankAccount;
import com.chainsys.fundtransfer.model.Beneficiary;
import com.chainsys.fundtransfer.model.Payment;
import com.chainsys.fundtransfer.model.RequestMoneyDetails;
import com.chainsys.fundtransfer.util.Email;
import com.chainsys.fundtransfer.validation.FundTransferValidation;
import jakarta.servlet.http.HttpSession;
@Controller
public class PaymentController {
	@Autowired
	Payment payment;
	@Autowired
	UserDAO userdao;
	@Autowired
	BankAccount bankaccount;
	@Autowired
	RequestMoneyDetails requestMoneyDetails;
	@Autowired
	FundTransferValidation fundtransfervalidation;
	@Autowired
	Email email;

	@GetMapping("/selectedfundtransfer")
	public String getUserAccountId(@RequestParam("userId") int userId, Model model) {
		String accountId = userdao.getAccountId(userId);
		model.addAttribute("accountId", accountId);
		System.out.println(accountId);
		return "fundTransfer";

	}

	@GetMapping("/selectedbeneficiaryfundtransfer")
	public String getAccountId(@RequestParam("userId") int userId, Model model) {
		String accountId = userdao.getAccountId(userId);
		System.out.println(accountId);
		model.addAttribute("accountId", accountId);
		List<Beneficiary> beneficiary = userdao.getBeneficiaryDetails(userId);
		System.out.println(beneficiary);
		model.addAttribute("beneficiarydetails", beneficiary);
		return "beneficiaryFundTransfer";

	}
	@PostMapping("/deposit")
	public String Deposit(@RequestParam("accountId" )int userId,@RequestParam("amount" )int amount,Model model)
	{
		int balance = userdao.getUserAccountBalance(userId);
		int addamount=balance+amount;
		userdao.updateSenderAccountBalance(userId, addamount);
		BankAccount bankaccount = userdao.getUserDetails(userId);
		model.addAttribute("userAccount", bankaccount);
		return "userProfile";
		
	}
	@PostMapping("/fundtransfer")
	public String Payment(@RequestParam("senderAccount") String senderAccountId,
			@RequestParam("receiverAccount") String receiverAccountId, @RequestParam("ifsc") String ifsc,
			@RequestParam("transferType") String transferType, @RequestParam("amount") int amount,
			@RequestParam("fundId") int id, Model model) {
		boolean accountIdExist=userdao.checkUserAlreadyExists(receiverAccountId);
		 if(accountIdExist) {
		payment.setSendAccountNo(senderAccountId);
		payment.setRecepientAccountNo(receiverAccountId);
		payment.setiFSC(ifsc);
		payment.setAmount(amount);
		payment.setTransfertype(transferType);
		payment.setUserId(id);
		int receiverBalance=userdao.getReceiverAccountBalance(receiverAccountId);
		System.out.println("r balance"+receiverBalance);
		int balance = userdao.getUserAccountBalance(id);
		System.out.println("s balance"+balance);
		if (amount > balance) {
			String accountId = userdao.getAccountId(id);
			model.addAttribute("accountId", accountId);
			 model.addAttribute("error", "Insufficient balance");
		        return "fundTransfer";
		}
		else
		{
			userdao.payment(payment);
		int remainingAmount = balance - amount;
		int receiverAmount=receiverBalance+amount;
		System.out.println("senderbalance:" + remainingAmount);
		System.out.println("receiverAmount:" + receiverAmount);
		userdao.updateSenderAccountBalance(id, remainingAmount);
		userdao.updateReceiverAccountBalance(receiverAccountId, receiverAmount);
		double creditPoints=amount*0.10;
		userdao.updateCreditPoints(creditPoints, id);
		Payment payment = userdao.paymentDetails();
		model.addAttribute("Payment", payment);
		String mail = userdao.getEmail(id);
		System.out.println(mail);
        String subject=" payment successful";
        String body="Account balance"+ 
                
                "\r\n"
                + "Best regards, ";
        email.sendTransactionEmail(mail, subject, body);
		return "paymentSuccess";
		}
		 }
		else
		{
			String accountId = userdao.getAccountId(id);
			model.addAttribute("accountId", accountId);
			model.addAttribute("status", "failure");
            model.addAttribute("message", "Invalid AccountId");
            return "fundTransfer";    
		}
		
	}

	@GetMapping("/TransactionHistory")
	public String transactionHistory(@RequestParam("userId") int id, Model model) {
		List<Payment> payment = userdao.transactionHistory(id);
		model.addAttribute("payment", payment);
		return "transactionHistory";

	}

	@PostMapping("/beneficiaryfundtransfer")
	public String beneficiaryPayment(@RequestParam("senderAccount") String senderAccountId,
			@RequestParam("receiverAccount") String receiverAccountId, @RequestParam("ifsc") String ifsc,
			@RequestParam("transferType") String transferType, @RequestParam("amount") int amount,
			@RequestParam("fundId") int id, @RequestParam("beneficiaryId") int beneficiaryId, Model model) {
		payment.setSendAccountNo(senderAccountId);
		payment.setRecepientAccountNo(receiverAccountId);
		payment.setiFSC(ifsc);
		payment.setAmount(amount);
		payment.setTransfertype(transferType);
		payment.setUserId(id);
		int balance = userdao.getUserAccountBalance(id);
		System.out.println(beneficiaryId);
		int beneficiaryBalance = userdao.getBeneficiaryAccountBalance(beneficiaryId);
		if (amount > balance) {
			String accountId = userdao.getAccountId(id);
			model.addAttribute("accountId", accountId);
			 model.addAttribute("error", "Insufficient balance");
		        return "beneficiaryFundTransfer";
		}
		
		else
		{
			userdao.payment(payment);
		int remainingAmount = balance - amount;
		int beneficiaryRemainingAmount = beneficiaryBalance + amount;
		System.out.println("remainingbalance:" + beneficiaryRemainingAmount);
		userdao.updateSenderAccountBalance(id, remainingAmount);
		userdao.updateBeneficiaryAccountBalance(beneficiaryId, beneficiaryRemainingAmount);
		double creditPoints=amount*0.10;
		userdao.updateCreditPoints(creditPoints, id);
		Payment payment = userdao.paymentDetails();
		model.addAttribute("Payment", payment);
		String mail = userdao.getEmail(id);
		System.out.println(mail);
        String subject=" payment successful";
        String body="Account balance"+ 
                
                "\r\n"
                + "Best regards, ";
        email.sendTransactionEmail(mail, subject, body);
		return "paymentSuccess";
		}

	}
	@PostMapping("/requestMoney")
	public String requestMoney(@RequestParam("userId") int id,@RequestParam("approverId") String approverAccountId,@RequestParam("amount") int amount,Model model) {
		boolean accountIdExist=userdao.checkUserAlreadyExists(approverAccountId);
		 if(accountIdExist) {
		String accountId=userdao.getAccountId(id);
		System.out.println(approverAccountId);
		requestMoneyDetails.setRequesterId(accountId);
		requestMoneyDetails.setAmount(amount);
		requestMoneyDetails.setApproverId(approverAccountId);
		requestMoneyDetails.setUserId(id);
		userdao.requestMoney(requestMoneyDetails);
		model.addAttribute("status", "success");
        model.addAttribute("message", "Money Requested SuccessFully");
        return "moneyRequest";  
		 }
		 else
			{
				model.addAttribute("status", "failure");
	            model.addAttribute("message", "Invalid AccountId");
	            return "moneyRequest";    
			}
			
	}
	@GetMapping("viewmoneyrequest")
	public String viewMoneyRequest(@RequestParam("userId") int userId, Model model) {
		String accountId=userdao.getAccountId(userId);
		List<RequestMoneyDetails> money = userdao.readRequestMoney(accountId);
		model.addAttribute("money", money);
		System.out.println(money);
		return "moneyRequest";

	}
	@PostMapping("/rejectRequest")
	public String rejectMoneyRequest(@RequestParam("requestId") int requestId,@RequestParam("userId") int userId, Model model)
	{
		String accountId=userdao.getAccountId(userId);
		userdao.moneyRequestStatus(requestId,"DECLINED");
		List<RequestMoneyDetails> money = userdao.readRequestMoney(accountId);
		model.addAttribute("money", money);
		return "moneyRequest";
		
	}
	@PostMapping("/approveRequest")
	public String approveMoneyRequest(@RequestParam("requestId") int requestId,@RequestParam("userId") int userId,@RequestParam("amount") int amount, Model model)
	{
		String accountId=userdao.getAccountId(userId);
		userdao.moneyRequestStatus(requestId,"APPROVED");
		int balance = userdao.getUserAccountBalance(userId);
		System.out.println("Balance:"+balance);
		int remainingAmount = balance - amount;
		System.out.println("remainingamount:"+remainingAmount);
		if (amount > balance) {
			 List<RequestMoneyDetails> money = userdao.readRequestMoney(accountId);
		        model.addAttribute("money", money);
		        model.addAttribute("error", "Insufficient balance");
		        model.addAttribute("requestId", requestId);
		        return "moneyRequest";
		}
		else
		{
		userdao.updateSenderAccountBalance(userId, remainingAmount);
		List<RequestMoneyDetails> money = userdao.readRequestMoney(accountId);
		model.addAttribute("money", money);
		return "moneyRequest";
		}
		
	}
	@PostMapping("/redeempoints")
	public String redeemPoints(@RequestParam("id") int userId, @RequestParam("points") int points,Model model,HttpSession session)
	{
		int creditPoints=userdao.getCreditPoints(userId);
		System.out.println(creditPoints);
		int RemainingCreditPoints=creditPoints-points;
		int balance = userdao.getUserAccountBalance(userId);
		   int additionalBalance = (points / 1000) * 50;
		   System.out.println(additionalBalance);
		 int  remainingAmount=balance+additionalBalance;
		userdao.updateCreditPoints(RemainingCreditPoints, userId);
		userdao.updateSenderAccountBalance(userId, remainingAmount);
		BankAccount bankaccount = userdao.getUserDetails(userId);
		model.addAttribute("userAccount", bankaccount);
		String accountId = userdao.getAccountId(userId);
		int count=userdao.countMoneyRequest(accountId);
		model.addAttribute("count", count);
		session.setAttribute("creditpoints", RemainingCreditPoints);
		return "redirect:/userprofilepage";

	}
}