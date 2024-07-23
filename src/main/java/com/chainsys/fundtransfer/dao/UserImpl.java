package com.chainsys.fundtransfer.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import com.chainsys.fundtransfer.mapper.BeneficiaryMapper;
import com.chainsys.fundtransfer.mapper.PaymentMapper;
import com.chainsys.fundtransfer.mapper.RequestMoneyMapper;
import com.chainsys.fundtransfer.mapper.UserAccountDetailsMapper;
import com.chainsys.fundtransfer.mapper.UserMapper;
import com.chainsys.fundtransfer.model.BankAccount;
import com.chainsys.fundtransfer.model.Beneficiary;
import com.chainsys.fundtransfer.model.Payment;
import com.chainsys.fundtransfer.model.RequestMoneyDetails;
import com.chainsys.fundtransfer.model.User;

@Repository
public class UserImpl implements UserDAO {
	@Autowired
	JdbcTemplate jdbctemplate;

	@Override
	public boolean register(User user) {
		String query = "SELECT * FROM Users WHERE user_name=? and email=?";
		Object[] params = { user.getUsername(), user.getEmail() };
		List<User> users = jdbctemplate.query(query, new UserMapper(), params);
		

		if (!users.isEmpty()) {
			return true;
		} else {
			String insertQuery = "INSERT INTO Users (user_name, email,user_Password) VALUES (?,?,?)";

			jdbctemplate.update(insertQuery, user.getUsername(), user.getEmail(), user.getPassword());
			
			return false;
		}
	}

	public User login(String email, String password) {

		String loginQuery = "select * from Users where email=? and user_Password=? ";
		Object[] params = { email, password };
		try {
			return jdbctemplate.queryForObject(loginQuery, new UserMapper(), params);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	public boolean createAccount(BankAccount account) {
		String query = "SELECT * FROM Accounts WHERE user_ID=?";
		Object[] params = { account.getUserId() };
		List<BankAccount> usersaccount = jdbctemplate.query(query, new UserAccountDetailsMapper(), params);
System.out.println(usersaccount);
		if (!usersaccount.isEmpty()) {
			System.out.println("user already exist");
			return true;
		
		} else {
			String insertQuery = "INSERT INTO Accounts(user_ID,first_name,Last_name,phonenumber,date_of_birth,aadhar_number,residential_address)  VALUES(?,?,?,?,?,?,?)";
			jdbctemplate.update(insertQuery, account.getUserId(), account.getFirstName(), account.getLastName(),
					account.getPhoneNumber(), account.getDate(), account.getAadharNumber(), account.getAddress());
			System.out.println("Inserted Successfully ");
			return false;
		}
	}

	public List<BankAccount> read() {
		String readusers = "Select * from Accounts where is_approval=false";
		return jdbctemplate.query(readusers, new UserAccountDetailsMapper());

	}

	public List<BankAccount> approvedUsers() {
		String readusers = "Select * from Accounts where is_approval=true";
		return jdbctemplate.query(readusers, new UserAccountDetailsMapper());

	}

	public void userApproval(BankAccount account) {
		String query = "UPDATE Accounts SET account_ID=?, IFSC=?, account_Balance=?, is_approval=true WHERE user_ID=?";
		Object[] params = { account.getAccountId(), account.getIfscCode(), 2000, account.getUserId() };
		int rows = jdbctemplate.update(query, params);
		System.out.println(rows);
	}

	public BankAccount getUserDetails(int userId) {
		String readusers = "Select * from Accounts where user_ID=?";
		return jdbctemplate.queryForObject(readusers, new UserAccountDetailsMapper(), userId);

	}

	public void updatePhoneNumber(int userId, String phoneNumber) {
		String query = "update Accounts set phonenumber=? where user_ID=? ";
		Object[] params = { phoneNumber, userId };
		int rows = jdbctemplate.update(query, params);

	}

	public void updateAddress(int userId, String address) {
		String query = "update Accounts set residential_address=? where user_ID=? ";
		Object[] params = { address, userId };
		int rows = jdbctemplate.update(query, params);

	}

	public void addBeneficiary(Beneficiary beneficiary) {
		String insertQuery = "INSERT INTO beneficiary(user_ID,beneficiary_name,beneficiary_accountID,ifsccode,account_balance)  VALUES(?,?,?,?,?)";
		jdbctemplate.update(insertQuery, beneficiary.getUserId(), beneficiary.getBeneficiaryName(),
				beneficiary.getBeneficiaryAccountId(), beneficiary.getIfsccode(), 1000);
	}

	public List<Beneficiary> viewBeneficiary(int id) {
		String readbeneficiary = "Select beneficiary_ID,beneficiary_name,beneficiary_accountID,ifsccode from beneficiary where is_deleted=false and  user_ID=?";
		return jdbctemplate.query(readbeneficiary, new BeneficiaryMapper(), id);

	}

	public void deleteBeneficiary(int id) {
		System.out.println(id);
		String query = "update beneficiary  set is_deleted=1 where beneficiary_ID=?";
		jdbctemplate.update(query, id);
	}

	public void updateBeneficiaryDetails(Beneficiary beneficiary) {
		String query = "update beneficiary set beneficiary_name=?,beneficiary_accountID=?,ifsccode=? where beneficiary_ID=? ";
		Object[] params = { beneficiary.getBeneficiaryName(), beneficiary.getBeneficiaryAccountId(),
				beneficiary.getIfsccode(), beneficiary.getBeneficiaryId() };
		int rows = jdbctemplate.update(query, params);
		System.out.println(rows);
	}

	public String getAccountId(int userId) {
		String query = "select account_ID from Accounts where user_ID=?";

		return jdbctemplate.queryForObject(query, String.class, userId);
	}

	public void payment(Payment payment) {
		LocalDateTime currentDate = LocalDateTime.now();
		String insertQuery = "INSERT INTO Transfers(user_ID,sender_Account_ID,Recipient_ID,IFSC_code,transfer_Type,transfer_Date,transfer_Amount)  VALUES(?,?,?,?,?,?,?)";
		jdbctemplate.update(insertQuery, payment.getUserId(), payment.getSendAccountNo(),
				payment.getRecepientAccountNo(), payment.getiFSC(), payment.getTransfertype(), currentDate,
				payment.getAmount());
	}

	public Payment paymentDetails() {
		String paymentQuery = "SELECT * FROM Transfers ORDER BY transfer_Date DESC LIMIT 1";

		return (Payment) jdbctemplate.queryForObject(paymentQuery, new PaymentMapper());
	}

	public List<Payment> transactionHistory(int id) {
		String readusers = "Select * from Transfers where user_ID=? ";
		return jdbctemplate.query(readusers, new PaymentMapper(), id);
	}

	public List<Beneficiary> getBeneficiaryDetails(int userId) {
		String query = "SELECT beneficiary_ID,beneficiary_name, beneficiary_accountID, ifsccode FROM beneficiary WHERE is_deleted=0 and user_ID=?";
		return jdbctemplate.query(query, (rs, rowNum) -> {
			Beneficiary beneficiary = new Beneficiary();
			beneficiary.setBeneficiaryId(rs.getInt("beneficiary_ID"));
			beneficiary.setBeneficiaryName(rs.getString("beneficiary_name"));
			beneficiary.setBeneficiaryAccountId(rs.getString("beneficiary_accountID"));
			beneficiary.setIfsccode(rs.getString("ifsccode"));
			return beneficiary;
		}, userId);
	}

	public int getUserAccountBalance(int id) {
		String accountBalance = "SELECT account_Balance from Accounts where user_ID=?";
		return jdbctemplate.queryForObject(accountBalance, int.class, id);
	}

	public int getBeneficiaryAccountBalance(int id) {
		String accountBalance = "SELECT account_Balance from beneficiary where beneficiary_ID=?";
		return jdbctemplate.queryForObject(accountBalance, int.class, id);

	}

	public void updateSenderAccountBalance(int id, int balance) {
		String query = "update Accounts set account_Balance=? where user_ID=? ";
		Object[] params = { balance, id };
		int rows = jdbctemplate.update(query, params);

	}

	public void updateBeneficiaryAccountBalance(int id, int balance) {
		String query = "update beneficiary set account_balance=? where beneficiary_ID=? ";
		Object[] params = { balance, id };
		int rows = jdbctemplate.update(query, params);

	}
	public void updateReceiverAccountBalance(String id, int balance) {
		String query = "update Accounts set account_balance=? where account_ID=? ";
		Object[] params = { balance, id };
		int rows = jdbctemplate.update(query, params);

	}
	public void requestMoney(RequestMoneyDetails requestMoneyDetails) {
		LocalDateTime currentDate = LocalDateTime.now();
		String insertQuery = "INSERT INTO money_requests(user_ID,requester_id,approver_id,amount,request_date)  VALUES(?,?,?,?,?)";
		jdbctemplate.update(insertQuery,requestMoneyDetails.getUserId(),requestMoneyDetails.getRequesterId() ,requestMoneyDetails.getApproverId() ,
				requestMoneyDetails.getAmount(),currentDate);
	}
	public List<RequestMoneyDetails> readRequestMoney(String id) {
		String readusers = "Select * from money_requests where approver_id=? and status='PENDING'";
		return jdbctemplate.query(readusers, new RequestMoneyMapper(), id);
	}
	public String getEmail(int id) {
		String accountBalance = "SELECT email from Users where user_ID=?";
		return jdbctemplate.queryForObject(accountBalance, String.class, id);
	}
	public void moneyRequestStatus(int requestId,String status)
	{
		LocalDateTime currentDate = LocalDateTime.now();
		String query = "update money_requests set status=?,approval_date=? where request_id=? ";
		Object[] params = {status,currentDate,  requestId };
		jdbctemplate.update(query, params);
	}
	public int countMoneyRequest(String accountId)
	{
	  String count=	"SELECT count(*) FROM money_requests where approver_id=? and status='PENDING'";
	  return jdbctemplate.queryForObject(count, Integer.class, accountId);
	}
	public void updateCreditPoints(double creditpoints,int id)
	{
		String query = "update Accounts set credit_points=? where user_ID=? ";
		Object[] params = { creditpoints, id };
		int rows = jdbctemplate.update(query, params);
		
	}
	public int getCreditPoints(int userId)
	{
		String points = "SELECT credit_points from Accounts where user_ID=?";
		return jdbctemplate.queryForObject(points, int.class, userId);
		
	}
	  public boolean checkUserAlreadyExists(String AccountId) {
	        String selectQuery = "SELECT account_ID FROM Accounts WHERE account_ID = ?";
	        List<String> existingUsers = jdbctemplate.query(selectQuery, (rs, rowNum) -> rs.getString("account_ID"), AccountId);
	        return !existingUsers.isEmpty();
	    }
	  public int getReceiverAccountBalance(String accountId)
	  {
		  String accountBalance = "SELECT account_Balance from Accounts where account_ID=?";
			return jdbctemplate.queryForObject(accountBalance, int.class, accountId);
		  
	  }
	  public int countApprovedUsers()
		{
		  String count=	"SELECT count(*) FROM Accounts where is_approval=true ";
		  return jdbctemplate.queryForObject(count, Integer.class);
		}
	  public int countUnApprovedUsers()
		{
		  String count=	"SELECT count(*) FROM Accounts where is_approval=false";
		  return jdbctemplate.queryForObject(count, Integer.class);
		}
		public int countTransactionHistory()
		{
			 String count=	"SELECT count(*) FROM Transfers";
			  return jdbctemplate.queryForObject(count, Integer.class);
		}
		public List<String> getExistingAccountIds() {
		    return jdbctemplate.queryForList("SELECT account_ID FROM Accounts", String.class);
		}

		public List<String> getExistingIfscs() {
		    return jdbctemplate.queryForList("SELECT IFSC FROM Accounts", String.class);
		}
		public String getAadhar(int id) {
		    String accountBalance = "SELECT aadhar_number FROM Accounts WHERE user_ID=?";
		    try {
		        return jdbctemplate.queryForObject(accountBalance, String.class, id);
		    } catch (EmptyResultDataAccessException e) {
		        return null; 
		    }
		}
	}
	
	

	

