package com.dmm.task.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain; // ★追加するimport

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
    
    // ----------------------------------------------------------------------
    // ★追加・修正: ログイン/ログアウトとパスの許可を設定するメソッド
    // ----------------------------------------------------------------------
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                // 静的リソース (CSSなど) は認証なしで許可
                .requestMatchers("/css/**", "/images/**").permitAll()
                // 上記以外の全てのリクエストには認証が必要
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                // ログインフォームのパスを指定
                .loginPage("/login")
                // ログイン処理を実行するパス（Controllerの定義は不要。Spring Securityが処理する）
                .loginProcessingUrl("/login")
                // ログイン成功後の遷移先 (カレンダー画面)
                .defaultSuccessUrl("/", true)
                // 認証失敗時のリダイレクト先
                .failureUrl("/login?error")
                // ログインフォームのフィールド名を指定
                .usernameParameter("username")
                .passwordParameter("password")
                // ログインページ自体は認証なしでアクセス可能にする
                .permitAll()
            )
            .logout(logout -> logout
                // ログアウト処理を実行するパス
                .logoutUrl("/logout")
                // ログアウト成功後の遷移先
                .logoutSuccessUrl("/login")
                // ログアウト後のセッション破棄を有効化
                .invalidateHttpSession(true)
                // ログアウト機能自体は認証なしでアクセス可能にする
                .permitAll()
            );

        return http.build();
    }
    // ----------------------------------------------------------------------


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        // user01の定義
        UserDetails user01 = User.builder()
                .username("user01")
                .password(passwordEncoder.encode("password01"))
                .roles("USER")
                .build();
        
        // user02の定義
        UserDetails user02 = User.builder()
                .username("user02")
                .password(passwordEncoder.encode("password02"))
                .roles("USER")
                .build();

        // adminの定義
        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin"))
                .roles("ADMIN", "USER")
                .build();

        return new InMemoryUserDetailsManager(user01, user02, admin);
    }
}