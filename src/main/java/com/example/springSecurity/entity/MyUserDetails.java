package com.example.springSecurity.entity;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.RequiredArgsConstructor;

// Spring Security가 로그인 POST 요청을 낚아채서(?) 로그인을 진행시킴
// 로컬 로그인 - UserDetails 구현
// 소셜 로그인 - OAuth2User 구현

@RequiredArgsConstructor
public class MyUserDetails implements UserDetails{
	private final SecurityUser securityUser;

	//  사용자의 권한을 리턴
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {	//GrantedAuthority 상속받는 누구나 
		Collection<GrantedAuthority> collect = new ArrayList<>();
		collect.add(new GrantedAuthority() {
			
			@Override
			public String getAuthority() {
				return securityUser.getRole();
			}
		});
		return collect;
	}

	@Override
	public String getPassword() {
		return securityUser.getPwd();
	}

	@Override
	public String getUsername() {
		return securityUser.getUid();
	}

	@Override
	public boolean isAccountNonExpired() {
		if (securityUser.getIsDeleted() == 0)
			return true;
		return false;
	}

	@Override
	public boolean isAccountNonLocked() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return true;
	}

}
