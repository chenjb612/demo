package com.demo.action;

import com.demo.bean.User;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Controller
public class IndexAction extends BaseAction{
    @RequestMapping("/index")
    public String index(Map<String,Object> map){
        System.out.println("HomeController.index()");
        //获取到用户信息;
        User user=getUser();
        map.put("userInfo",user);
        return "user/index";
    }
}

