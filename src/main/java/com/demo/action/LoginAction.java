package com.demo.action;

import com.demo.bean.BaseResult;
import com.demo.bean.User;
import com.demo.dao.UsersDao;
import com.demo.util.Md5Utils;
import com.demo.util.VerifyCodeUtils;
import com.sun.xml.internal.rngom.parse.host.Base;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

@Controller
public class LoginAction {

    @Autowired
    private UsersDao usersDao;

    @RequestMapping("/login")
    public String login(Map<String,Object> map, ModelMap modelMap){
        return "login/login";
    }

    @RequestMapping(value="/getImage",method= RequestMethod.GET)
    public void authImage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType("image/jpeg");
        // 生成随机字串
        String verifyCode = VerifyCodeUtils.generateVerifyCode(4);
        // 存入会话session
        HttpSession session = request.getSession(true);
        // 删除以前的
        session.removeAttribute("verCode");
        session.removeAttribute("codeTime");
        session.setAttribute("verCode", verifyCode.toLowerCase());
        session.setAttribute("codeTime", LocalDateTime.now());
        // 生成图片
        int w = 100, h = 30;
        OutputStream out = response.getOutputStream();
        VerifyCodeUtils.outputImage(w, h, out, verifyCode);
    }

    /**
     * 校验验证码
     * @param request
     * @param session
     * @return
     */
    @PostMapping(value="/dologin")
    @ResponseBody
    public BaseResult validImage(
            @RequestParam("account") String account,
            @RequestParam("password") String password,
            @RequestParam("code")String code,
            HttpServletRequest request, HttpSession session){
        BaseResult result = new BaseResult();
        User user=new User();

        Object verCode = session.getAttribute("verCode");
        if (null == verCode) {
            request.setAttribute("errmsg", "验证码已失效，请重新输入");
            result.setSuccess(false);
            result.setErrorMessage("验证码已失效，请重新输入");
            return result;
        }
        String verCodeStr = verCode.toString();
        LocalDateTime localDateTime = (LocalDateTime)session.getAttribute("codeTime");
        long past = localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long now = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        if(verCodeStr == null || code == null || code.isEmpty() || !verCodeStr.equalsIgnoreCase(code)){
            request.setAttribute("errmsg", "验证码错误");
            result.setSuccess(false);
            result.setErrorMessage("验证码错误");
        } else if((now-past)/1000/60>5){
            request.setAttribute("errmsg", "验证码已过期，重新获取");
            result.setSuccess(false);
            result.setErrorMessage("验证码已过期，重新获取");
        } else {
            session.removeAttribute("verCode");
            user.setAccount(account);
            user=usersDao.selectByRecord(user);
            if(user==null){
                result.setSuccess(false);
                result.setErrorMessage("会员不存在请重新注册");
            }else{
               String md5Password =Md5Utils.convertMD5(Md5Utils.convertMD5(password));
               if(StringUtils.pathEquals(md5Password,user.getPassword())){
                   result.setSuccess(false);
                   result.setErrorMessage("密码或账号不正确");
               }else{
                   Subject currentUser = SecurityUtils.getSubject();
                   Session newsSession = currentUser.getSession();
                   newsSession.setAttribute("user", user);
                   result.setSuccess(true);
                   result.setResult(user);
               }
            }
        }
        return  result;
    }

    @PostMapping("/checkUser")
    @ResponseBody
    public BaseResult checkUser( @RequestParam("account") String account){
        BaseResult baseResult=new BaseResult();
        User user=new User();
        user.setPhone(account);
        user=usersDao.selectByRecord(user);
        if(user!=null){
            baseResult.setSuccess(false);
            baseResult.setErrorMessage("用户已存在");
        }else{
            baseResult.setSuccess(true);
        }
        return baseResult;
    }

    @PostMapping("/doReg")
    @ResponseBody
    public BaseResult doReg(User user){
        BaseResult baseResult=new BaseResult();
        //加密
        String md5Password =Md5Utils.string2MD5(user.getPassword());
        user.setPassword(md5Password);
        int result=usersDao.insert(user);
        if(result>0){
            baseResult.setSuccess(true);
        }else{
            baseResult.setSuccess(false);
            baseResult.setErrorMessage("注册失败");
        }
        return baseResult;
    }


    @RequestMapping("/doLogin")
    public String doLogin(){
        return "";
    }

    @RequestMapping("/register")
    public String register(Map<String,Object> map, ModelMap modelMap){
        return "user/login";
    }


}
