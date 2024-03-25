package com.example.springSecurity.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.springSecurity.entity.Member;

@Controller
@RequestMapping("/member")
public class MemberController {
	
	@ResponseBody
	@GetMapping("/deatil/{mid}")
	public String detail(@PathVariable int mid) {
		Member member = new Member();
		return "";
	}
	
//	@GetMapping("/insert")
//	public String insert() {
//		Member m1 = new Member ();
//		m1.setName("james");m1.setEmail("lebron23@g.com");
//		
////		// Builder pattern
////		Member m2 = Member.builder()
////						.name("maria").email("mari@g.com");
////						.build();
////		return "";
//	}
//	
}
