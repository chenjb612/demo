package com.demo.bean;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.util.DigestUtils;

import javax.naming.AuthenticationException;

public class ShiroRealm extends AuthorizingRealm {
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(
            PrincipalCollection principalCollection) {
        User user=new User();
        //TODO
        new SimpleAuthenticationInfo(user, DigestUtils.md5Digest(user.getPassword().getBytes()),
                getName());
        return null;
    }

    @Override
    protected org.apache.shiro.authc.AuthenticationInfo doGetAuthenticationInfo(
            AuthenticationToken authenticationToken) {
        //TODO
        return null;
    }
}