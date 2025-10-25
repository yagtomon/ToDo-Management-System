// src/main/java/com/dmm/task/config/WebSecurityConfig.java
package com.dmm.task.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import java.util.HashMap; 
import java.util.Map; 
import com.dmm.task.service.AccountUserDetailsService;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;




@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private AccountUserDetailsService userDetailsService;

    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        // ここでロール階層を定義できるが、今回はロール階層を使わずプレフィックスを空にする目的
        roleHierarchy.setHierarchy("ROLE_ADMIN > ROLE_USER"); // 階層が必要なければ空でも可
        return roleHierarchy;
    }

    @Bean
    public GrantedAuthorityDefaults grantedAuthorityDefaults() {
        // デフォルトの 'ROLE_' プレフィックスを削除する (空文字にする)
        return new GrantedAuthorityDefaults(""); 
    }

        @Bean
        public PasswordEncoder passwordEncoder() {
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            Map<String, PasswordEncoder> encoders = new HashMap<>();
            encoders.put("bcrypt", bCryptPasswordEncoder);
            return new DelegatingPasswordEncoder("bcrypt", encoders);
        }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.debug(false).ignoring().antMatchers("/images/**", "/js/**", "/css/**");
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
        super.configure(auth);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 認可の設定
        http.exceptionHandling()
            .accessDeniedPage("/login")
            .and()
            .authorizeRequests()
            .antMatchers("/login").permitAll() 
            .antMatchers("/authenticate").permitAll() 
            .anyRequest().authenticated(); 

        // ログイン設定
        http.formLogin()
            .loginPage("/login") 
            .loginProcessingUrl("/authenticate") 
            .usernameParameter("userName") 
            .passwordParameter("password") 
            .defaultSuccessUrl("/main") 
            .failureUrl("/login?error=true"); 

        // ログアウト設定
        http.logout()
            .logoutSuccessUrl("/login") 
            .permitAll();
    }
}