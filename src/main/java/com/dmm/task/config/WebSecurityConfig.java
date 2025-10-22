package com.dmm.task.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// import org.springframework.security.core.userdetails.UserDetailsService; // 削除
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
// import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder; // 削除
// import org.springframework.beans.factory.annotation.Autowired; // 削除

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    // ★★★ 削除: フィールドとコンストラクタは不要 ★★★
    // private final UserDetailsService userDetailsService;
    // public WebSecurityConfig(UserDetailsService userDetailsService) { /* ... */ }

    // ★★★ 削除: configureGlobal メソッドも削除（循環参照の原因） ★★★
    // @Autowired
    // public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception { /* ... */ }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .authorizeRequests()
                .antMatchers("/login", "/css/**", "/images/**").permitAll()
                .anyRequest().authenticated()
                .and()
            .formLogin()
                .loginPage("/login")
                .defaultSuccessUrl("/main", true) 
                .failureUrl("/login?error")
                .usernameParameter("username")
                .passwordParameter("password")
                .and()
            .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login")
                .invalidateHttpSession(true);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}