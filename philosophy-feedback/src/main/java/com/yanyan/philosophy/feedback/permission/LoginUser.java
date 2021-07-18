package com.yanyan.philosophy.feedback.permission;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * @author niuyan
 * @date 2021/4/20 下午3:42
 * @version 1.0.0.1
 */
public class LoginUser extends User {

    public LoginUser(String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
    }
}