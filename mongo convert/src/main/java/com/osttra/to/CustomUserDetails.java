package com.osttra.to;

import java.util.Collection;
import java.util.HashSet;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.osttra.entity.User;

public class CustomUserDetails implements UserDetails{
	
	private User user;
	
	public CustomUserDetails(User user) {
		System.out.println("Inside CustomUserDetails constr");
		this.user = user;
		
		System.out.println(user);
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		
		System.out.println("inside grantedAuthority...");
		HashSet<SimpleGrantedAuthority> set = new HashSet<>();
		set.add(new SimpleGrantedAuthority(this.user.getRole()));
		
		return set;
	}

	@Override
	public String getPassword() {
		System.out.println("inside getPassword");
		return this.user.getPassword();
	}

	@Override
	public String getUsername() {
		System.out.println("inside getUsername");
		return this.user.getUsername();
	}

	@Override
	public boolean isAccountNonExpired() {
		System.out.println("isndie isNonExpired");
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		System.out.println("inside isNonlocked");
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		System.out.println("inside isCredentialsNonExpired");
		return true;
	}

	@Override
	public boolean isEnabled() {
		System.out.println("inside isEnabled");
		return true;
	}

}
