package com.chainsys.fundtransfer.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.chainsys.fundtransfer.dao.UserDAO;
import com.chainsys.fundtransfer.exceptionhandling.BeneficiaryException;
import com.chainsys.fundtransfer.model.Beneficiary;

import jakarta.servlet.http.HttpSession;

@Controller
public class BeneficiaryController {
	@Autowired
	Beneficiary beneficiary;
	@Autowired
	UserDAO userdao;

	@PostMapping("/addbeneficiary")
	public String addBeneficiary(@RequestParam("addBeneficiaryName") String beneficiaryName,
			@RequestParam("addBeneficiaryAccountId") String beneficiaryAccountId, @RequestParam("addBeneficiaryIfscCode") String ifscCode,
			@RequestParam("id") int id,HttpSession session, Model model) {
		beneficiary.setBeneficiaryName(beneficiaryName);
		beneficiary.setBeneficiaryAccountId(beneficiaryAccountId);
		beneficiary.setIfsccode(ifscCode);
		beneficiary.setUserId(id);
		try
		{
		userdao.addBeneficiary(beneficiary);
		}
		catch(DuplicateKeyException e)
		{
			throw new BeneficiaryException ("user already exist");
		}
		List<Beneficiary> beneficiary = userdao.getBeneficiaryDetails(id);
		session.setAttribute("beneficiarydetails", beneficiary);
		List<Beneficiary> beneficiaryDetails = userdao.viewBeneficiary(id);

		model.addAttribute("beneficiarydetails", beneficiaryDetails);
		return "beneficiary";

	}

	@GetMapping("/viewbeneficiary")
	public String viewBeneficiary(@RequestParam("userId") int id, Model model) {
		List<Beneficiary> beneficiaryDetails = userdao.viewBeneficiary(id);

		model.addAttribute("beneficiarydetails", beneficiaryDetails);
		return "beneficiary";

	}

	@PostMapping("/deletebeneficiary")
	public String deleteBeneficiary(@RequestParam("viewid") int id, @RequestParam("deleteid") int idToDelete,
			Model model,HttpSession session) {
		userdao.deleteBeneficiary(idToDelete);
		id=(int) session.getAttribute("id");
		List<Beneficiary> beneficiary = userdao.getBeneficiaryDetails(id);
		session.setAttribute("beneficiarydetails", beneficiary);
		List<Beneficiary> beneficiaryDetails = userdao.viewBeneficiary(id);
		model.addAttribute("beneficiarydetails", beneficiaryDetails);
		return "beneficiary";

	}

	@PostMapping("/updatebeneficiarydetails")
	public String update(@RequestParam("id") int id, @RequestParam("editBeneficiaryId") int editBeneficiaryId,
			@RequestParam("editBeneficiaryName") String beneficiaryName,
			@RequestParam("editBeneficiaryAccountId") String beneficiaryAccountId,
			@RequestParam("editBeneficiaryIfscCode") String beneficiaryIfsc, Model model) {
		beneficiary.setBeneficiaryId(editBeneficiaryId);
		beneficiary.setBeneficiaryName(beneficiaryName);
		beneficiary.setBeneficiaryAccountId(beneficiaryAccountId);
		beneficiary.setIfsccode(beneficiaryIfsc);
		userdao.updateBeneficiaryDetails(beneficiary);
		List<Beneficiary> beneficiaryDetails = userdao.viewBeneficiary(id);
		model.addAttribute("beneficiarydetails", beneficiaryDetails);
		return "beneficiary";

	}
}