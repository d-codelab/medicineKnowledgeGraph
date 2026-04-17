package org.zuel.medicineknowledge.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.zuel.medicineknowledge.constant.SecurityConstants;
import org.zuel.medicineknowledge.constant.UserConstant;

@Configuration
@EnableWebSecurity//springboot项目可省略，已经自动加载了
public class WebSecurityConfig extends WebSecurityConfigurerAdapter{



	/**
	 * 此方法配置的资源路径不会进入 Spring Security 机制进行验证
	 */
	@Override
	public void configure(WebSecurity web) {
		web.ignoring()
				.antMatchers(HttpMethod.OPTIONS, "/**")
				.antMatchers("/doc.html")
				.antMatchers("/app/**/*.{js,html}")
				.antMatchers("/v2/api-docs/**")
				.antMatchers("/i18n/**")
				.antMatchers("/test/**")
				.antMatchers("/h2")
				.antMatchers("/content/**")
				.antMatchers("/webjars/springfox-swagger-ui/**")
				.antMatchers("/swagger-resources/**")
				.antMatchers("/swagger-ui.html");
	}


	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder(){
		return new BCryptPasswordEncoder();
	}
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http	.cors(Customizer.withDefaults())
				.exceptionHandling()
				// 当用户无权访问资源时发送 401 响应
				.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
				.and()
				// 禁用 CSRF
				.csrf().disable()
				.headers().frameOptions().disable()
				.and()
				.authorizeRequests()
				// 指定路径下的资源需要进行验证后才能访问
				.antMatchers("/swagger-ui/**", "/swagger-resources/**", "/v2/api-docs", "/v3/api-docs", "/webjars/**").permitAll()
				.antMatchers(HttpMethod.POST, SecurityConstants.AUTH_LOGIN_URL).permitAll()
				.antMatchers(HttpMethod.POST,SecurityConstants.AUTH_REGISTER_URL).permitAll()
				.antMatchers(HttpMethod.GET,SecurityConstants.AUTH_GET_EMAIL_URL).permitAll()
				.antMatchers(HttpMethod.GET,SecurityConstants.AUTH_GET_CODE_URL).permitAll()
				.antMatchers(HttpMethod.POST,SecurityConstants.AUTH_FORGETPASSWORD_URL).permitAll()
				.antMatchers(HttpMethod.POST,SecurityConstants.AUTH_LIST_ARTICLES_URL).permitAll()
				.antMatchers(HttpMethod.POST,SecurityConstants.AUTH_LOGIN_ADMIN_URL).permitAll()
				.antMatchers(HttpMethod.GET,SecurityConstants.AUTH_GET_ARTICLES_URL).permitAll()
				.antMatchers(HttpMethod.GET,SecurityConstants.AUTH_LIS_CATEGORY_URL).permitAll()

				//配置管理员权限请求
				.antMatchers("/user/admin/**").hasAuthority(UserConstant.ADMIN_ROLE)
				.antMatchers("/articles/admin/**").hasAuthority(UserConstant.ADMIN_ROLE)
				.antMatchers("/category/admin/**").hasAuthority(UserConstant.ADMIN_ROLE)
				.antMatchers("/statistics/admin/**").hasAuthority(UserConstant.ADMIN_ROLE)
				// 其他请求需验证
				.anyRequest().authenticated()
				.and()
				// 不需要 session（不创建会话）
				.sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
				.and()
				.apply(securityConfigurationAdapter());
	}

//	@Override
//	protected void configure(HttpSecurity http) throws Exception {
//		http.authorizeHttpRequests(authorize -> authorize
//						.antMatchers("/swagger-ui/**", "/swagger-resources/**", "/v2/api-docs", "/v3/api-docs", "/webjars/**").permitAll()
//						.antMatchers("/user/login","/user/register","/articles/list/page","/articles/get","/doc.html").permitAll()
//						.antMatchers(HttpMethod.OPTIONS).permitAll()
//						.anyRequest().authenticated()
//
//				);
////		http.formLogin(Customizer.withDefaults());
//		HeaderWriterLogoutHandler clearSiteData = new HeaderWriterLogoutHandler(new ClearSiteDataHeaderWriter(ClearSiteDataHeaderWriter.Directive.COOKIES));
//		http.logout(logout ->logout.addLogoutHandler(clearSiteData).logoutSuccessHandler(new MyLogoutHandler()));
//		http.exceptionHandling(ex ->ex.authenticationEntryPoint(new MyAuthenticationEntryPoint()));
//		http.csrf(csrf->csrf.disable());
//		http.cors(Customizer.withDefaults());
//		http.sessionManagement(session -> session.maximumSessions(2).expiredSessionStrategy(new MySessionExpiredHandler()));
//		http.apply(securityConfigurationAdapter());
////		return http.build();
//	}

	private JwtConfigurer securityConfigurationAdapter() throws Exception{
		return new JwtConfigurer(new JwtAuthorizationFilter(authenticationManager()));
	}
}