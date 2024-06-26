package com.example.springSecurity.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.example.springSecurity.entity.SecurityUser;
import com.example.springSecurity.service.SecurityUserService;
import com.example.springSecurity.util.AsideUtil;
import com.example.springSecurity.util.ImageUtil;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;


@Slf4j
@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class SecurityUserController {
	private final SecurityUserService securityService;
	private final BCryptPasswordEncoder bCryptEncoder;
	private final ResourceLoader resourceLoader;
	private final ImageUtil imageUtil;
	private final AsideUtil asideUtil;
	@Value("${spring.servlet.multipart.location}") private String uploadDir;
	private String menu = "user";
	
	@GetMapping("/login")
	public String login() {
		return "user/login";
	}
	
	@GetMapping("/register")
	public String registerForm() {
		return "user/register";
	}
	
	@PostMapping("/register")
	public String registerProc(String uid, String pwd, String pwd2, String uname,
			String email, MultipartHttpServletRequest req, Model model) {
		String filename = null;
		MultipartFile filePart = req.getFile("picture");
		
		SecurityUser securityUser = securityService.getUserByUid(uid);
		if (securityUser != null) {
			model.addAttribute("msg", "사용자 ID가 중복되었습니다.");
			model.addAttribute("url", "/ss/user/register");
			return "common/alertMsg";
		}
		if (pwd == null || !pwd.equals(pwd2)) {
			model.addAttribute("msg", "암호 입력이 잘못되었습니다.");
			model.addAttribute("url", "/ss/user/register");
			return "common/alertMsg";
		}
		if (filePart.getContentType().contains("image")) {
			   filename = filePart.getOriginalFilename();
			   String path = uploadDir + "profile/" + filename;
			   try {
				    filePart.transferTo(new File(path));
			   	} catch (Exception e) {
				e.printStackTrace(); 
				}
			   filename = imageUtil.squareImage(uid, filename);
		   }
		String hashedPwd = bCryptEncoder.encode(pwd);
		securityUser = SecurityUser.builder()
				.uid(uid).pwd(hashedPwd).uname(uname).email(email)
				.picture("/ss/file/download/profile/" + filename).provider("world")
				.build();
		   securityService.insertSecurityUser(securityUser);
		   model.addAttribute("msg", "끝났다. 로그인");
		   model.addAttribute("url", "/ss/user/login");
		   return "common/alertMsg";
	}
	
	@GetMapping("/loginSuccess")
	public String loginSuccess(HttpSession session, Model model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		// 세션의 현재 사용자 아이디
		String uid = authentication.getName();
		
		// 세션에 필요한 값 설정
		SecurityUser securityUser = securityService.getUserByUid(uid);
		session.setAttribute("sessUid", uid);
		session.setAttribute("sessUname", securityUser.getUname());
		session.setAttribute("picture", securityUser.getPicture());
		session.setAttribute("email", securityUser.getEmail());
		
		// 상태 메시지
		Resource resource = resourceLoader.getResource("classpath:/static/data/todayQuote.txt");
		String quoteFile = null;
		try {
			quoteFile = resource.getURI().getPath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String stateMsg = asideUtil.getTodayQuote(quoteFile);
		session.setAttribute("stateMsg", stateMsg);
		
		// 환영 메세지
		log.info("Info Login: {}, {}", uid, securityUser.getUname());
		model.addAttribute("msg", securityUser.getUname()+"님 환영합니다.");
		model.addAttribute("url", "/ss/user/list");
		
		return "common/alertMsg";
	}
	
	@GetMapping({"/list/{page}", "/list"})
	public String list(@PathVariable(required=false) Integer page, HttpSession session, Model model) {
		page = (page == null) ? 1 : page;
		session.setAttribute("currentUserPage", page);
		List<SecurityUser> list = securityService.getSecurityUserList(page);
		model.addAttribute("userList", list);
		
		// for pagination
		int totalUsers = securityService.getSecurityUserCount();
		int totalPages = (int) Math.ceil(totalUsers * 1.0 / SecurityUserService.COUNT_PER_PAGE);
		List<Integer> pageList = new ArrayList<>();
		for (int i = 1; i <= totalPages; i++)
			pageList.add(i);
		model.addAttribute("pageList", pageList);
		model.addAttribute("menu", menu);
		
		return "user/list";
	}

	@ResponseBody
	@GetMapping("/detail/{uid}")
	public String detail(@PathVariable String uid) {
		SecurityUser securityUser = securityService.getUserByUid(uid);
		JSONObject jUser = new JSONObject();
		jUser.put("uid", uid);
		jUser.put("uid2", uid);
		jUser.put("hashedPwd", securityUser.getPwd());
		jUser.put("uname", securityUser.getUname());
		jUser.put("email", securityUser.getEmail());
		jUser.put("provider", securityUser.getProvider());
		jUser.put("role", securityUser.getRole());
		jUser.put("picture", securityUser.getPicture());
		return jUser.toString();
	}
	
	@PostMapping("/update")
	public String update(String uid, String pwd, String pwd2, String uname, String email,
						String hashedPwd, String picture, String provider, String role,
						MultipartHttpServletRequest req, HttpSession session, Model model) {
		System.out.println("hashedPwd=" + hashedPwd + ", provider=" + provider);
		String filename = null;
		SecurityUser securityUser = null;
		int currentUserPage = (Integer) session.getAttribute("currentUserPage");
		MultipartFile filePart = req.getFile("newProfile");
		String sessUid = (String) session.getAttribute("sessUid");
		
		if (!sessUid.equals(uid)) {
			model.addAttribute("msg", "수정 권한이 없다.");
			model.addAttribute("url", "/ss/user/list/" + currentUserPage);
			return "common/alertMsg";
		}
		if (provider.equals("admin")) {		// Local 등록자
			if (pwd != null && pwd.length() > 1 && pwd.equals(pwd2))
				hashedPwd = bCryptEncoder.encode("pwd");
			if (filePart.getContentType().contains("image")) {
					// 기존 사진 지우기
					int idx = picture.lastIndexOf("/");
					String path = uploadDir + "profile/" + picture.substring(idx + 1);
					File file = new File(path);
					file.delete();
					
					filename = filePart.getOriginalFilename();
					path = uploadDir + "profile/" + filename;
					try {
						filePart.transferTo(new File(path));
					} catch (Exception e) {
						e.printStackTrace();
					}
					picture = "/ss/file/download/profile/" + imageUtil.squareImage(uid, filename);
				}
				securityUser = SecurityUser.builder()
								.uid(uid).pwd(hashedPwd).uname(uname).email(email).picture(picture)
								.provider(provider).role(role).build();
				session.setAttribute("sessUname", uname);
				session.setAttribute("picture", picture);
				session.setAttribute("email", email);
		}
		securityService.updateSecurityUser(securityUser);
		return "redirect:/user/list/" + currentUserPage;
	}
	
		
	
}