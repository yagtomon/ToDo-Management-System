// src/main/java/com/dmm/task/config/WebSecurityConfig.java
package com.dmm.task.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity; 
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.core.userdetails.UserDetailsService;
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

    // 💡 プレフィックスを空文字にする設定を必ず入れる！
    // これで、DBの 'ROLE_ADMIN' がそのままロールとして認識されるようになる。
    @Bean
    public GrantedAuthorityDefaults grantedAuthorityDefaults() {
        // デフォルトの 'ROLE_' プレフィックスを削除する (空文字にする)
        return new GrantedAuthorityDefaults(""); 
    }

        @Bean
        public PasswordEncoder passwordEncoder() {
            // 1. デフォルトで使用するBCryptPasswordEncoderを定義
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

            // 2. エンコーダーIDとその実装クラスをマップで定義
            Map<String, PasswordEncoder> encoders = new HashMap<>();
            encoders.put("bcrypt", bCryptPasswordEncoder); // "{bcrypt}"をBCryptPasswordEncoderにマッピング

            // 3. DelegatingPasswordEncoderを使って、ID付きのパスワードを処理できるようにする
            // デフォルトのエンコーダーとして"bcrypt"を指定する
            return new DelegatingPasswordEncoder("bcrypt", encoders);
        }

    @Override
    public void configure(WebSecurity web) throws Exception {
        // 画像、JavaScript、cssは認可の対象外とする
        web.debug(false).ignoring().antMatchers("/images/**", "/js/**", "/css/**");
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // AuthenticationManagerBuilderに、実装した UserDetailsServiceとPasswordEncoderを設定
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
        super.configure(auth);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 認可の設定
        http.exceptionHandling()
            .accessDeniedPage("/login") // アクセス拒否された時に遷移
            .and()
            .authorizeRequests()
            .antMatchers("/login").permitAll() // loginFormは、全ユーザからのアクセスを許可
            .antMatchers("/authenticate").permitAll() // 認証処理のパスも許可
            .anyRequest().authenticated(); // その他は認証を求める

        // ログイン設定
        http.formLogin()
            .loginPage("/login") // ログインフォームを表示 するパス
            .loginProcessingUrl("/authenticate") // フォーム認証処理のパス
            .usernameParameter("userName") // ユーザ名のリクエストパラメータ名
            .passwordParameter("password") // パスワードのリクエストパラメータ名
            .defaultSuccessUrl("/main") // 認証成功時に遷移するデフォルトのパスを要件の「カレンダー」に変更
            .failureUrl("/login?error=true"); // 認証失敗時に遷移するパス

        // ログアウト設定
        http.logout()
            .logoutSuccessUrl("/login") // ログアウト成功時に遷移するパス
            .permitAll(); // 全ユーザに対して許可
    }
}