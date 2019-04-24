package com.demo.action;

import com.demo.bean.User;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;

public class BaseAction {
    /**
     * 获取用户信息
     * @return
     */
    public User getUser(){
        //获取到用户信息;
        Subject subject  = SecurityUtils.getSubject();
        Session newsSession = subject.getSession();
        //这里的User是登陆时放入session的
        User user = (User) newsSession.getAttribute("user");
        return user;
    }
}
