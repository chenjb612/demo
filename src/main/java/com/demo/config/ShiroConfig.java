package com.demo.config;

import at.pollux.thymeleaf.shiro.dialect.ShiroDialect;
import com.demo.bean.ShiroRealm;
import com.demo.matcher.RetryLimitHashedCredentialsMatcher;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.apache.shiro.mgt.SecurityManager;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class ShiroConfig {

    private static final Logger logger = LoggerFactory
            .getLogger(ShiroConfig.class);

    @Bean(name = "securityManager")
    public SecurityManager securityManager(@Qualifier("authRealm") ShiroRealm authRealm,
                                           @Qualifier("cookieRememberMeManager") CookieRememberMeManager cookieRememberMeManager) {
        logger.info("securityManager()");
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        // 设置realm.
        securityManager.setRealm(authRealm);

        // 设置rememberMe管理器
        securityManager.setRememberMeManager(cookieRememberMeManager);

        return securityManager;
    }
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(
            @Qualifier("securityManager") SecurityManager securityManager) {
        logger.info("authorizationAttributeSourceAdvisor()");
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager(securityManager);
        return authorizationAttributeSourceAdvisor;
    }

    /**
     * ShiroFilterFactoryBean 处理拦截资源文件过滤器
     *  </br>1,配置shiro安全管理器接口securityManage;
     *  </br>2,shiro 连接约束配置filterChainDefinitions;
     */
    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean(
            SecurityManager securityManager) {
        //shiroFilterFactoryBean对象
        ShiroFilterFactoryBean shiroFilterFactoryBean;
        shiroFilterFactoryBean = new ShiroFilterFactoryBean();

        // 配置shiro安全管理器 SecurityManager
        shiroFilterFactoryBean.setSecurityManager(securityManager);

        // 指定要求登录时的链接
        shiroFilterFactoryBean.setLoginUrl("/login");

        // 登录成功后要跳转的链接
        shiroFilterFactoryBean.setSuccessUrl("/index");
        // 未授权时跳转的界面;
        shiroFilterFactoryBean.setUnauthorizedUrl("/403");

        // filterChainDefinitions拦截器
        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<String, String>();
        // 配置不会被拦截的链接 从上向下顺序判断
        filterChainDefinitionMap.put("/static/**", "anon");
        filterChainDefinitionMap.put("/templates/**", "anon");
        filterChainDefinitionMap.put("classpath:/static/", "anon");
        // 配置退出过滤器,具体的退出代码Shiro已经替我们实现了
        filterChainDefinitionMap.put("/logout", "logout");
        //add操作，该用户必须有【addOperation】权限
        filterChainDefinitionMap.put("/add", "perms[addOperation]");

        // <!-- authc:所有url都必须认证通过才可以访问; anon:所有url都都可以匿名访问【放行】-->
        filterChainDefinitionMap.put("/user/**", "authc");

        shiroFilterFactoryBean
                .setFilterChainDefinitionMap(filterChainDefinitionMap);
        logger.debug("Shiro拦截器工厂类注入成功");
        return shiroFilterFactoryBean;
    }

    /**
     * shiro安全管理器设置realm认证
     * @return
     */
    @Bean
    public DefaultWebSecurityManager securityManager() {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        // 设置realm.
        securityManager.setRealm(shiroRealm());
        // //注入ehcache缓存管理器;
        securityManager.setCacheManager(ehCacheManager());
        return securityManager;
    }

    /**
     * 身份认证realm; (账号密码校验；权限等)
     *
     * @return
     */
    @Bean public ShiroRealm shiroRealm() {

        ShiroRealm shiroRealm = new ShiroRealm();
        shiroRealm.setCredentialsMatcher(hashedCredentialsMatcher());
        return shiroRealm;
    }

    /**
     * ehcache缓存管理器；shiro整合ehcache：
     * 通过安全管理器：securityManager
     * @return EhCacheManager
     */
    @Bean public EhCacheManager ehCacheManager() {
        logger.debug(
                "=====shiro整合ehcache缓存：ShiroConfiguration.getEhCacheManager()");
        EhCacheManager cacheManager = new EhCacheManager();
        cacheManager.setCacheManagerConfigFile("classpath:config/ehcache.xml");
        return cacheManager;
    }

    @Bean
    public SimpleCookie remeberMeCookie(){
        logger.debug("记住我，设置cookie过期时间！");
        //cookie名称;对应前端的checkbox的name = rememberMe
        SimpleCookie scookie=new SimpleCookie("rememberMe");
        //记住我cookie生效时间1小时 ,单位秒  [1小时]
        scookie.setMaxAge(3600);
        return scookie;
    }
    // 配置cookie记住我管理器
    @Bean
    public CookieRememberMeManager rememberMeManager(){
        logger.debug("配置cookie记住我管理器！");
        CookieRememberMeManager cookieRememberMeManager=new CookieRememberMeManager();
        cookieRememberMeManager.setCookie(remeberMeCookie());
        return cookieRememberMeManager;
    }


    @Bean
    public HashedCredentialsMatcher hashedCredentialsMatcher() {
        HashedCredentialsMatcher hashedCredentialsMatcher = new RetryLimitHashedCredentialsMatcher(ehCacheManager());
        //new HashedCredentialsMatcher();
        hashedCredentialsMatcher.setHashAlgorithmName("md5");// 散列算法:这里使用MD5算法;
        hashedCredentialsMatcher.setHashIterations(1);// 散列的次数，比如散列两次，相当于 // md5(md5(""));
        return hashedCredentialsMatcher;
    }

    @Bean
    public ShiroDialect shiroDialect(){
        return new ShiroDialect();
    }
}
