package com.gamakdragons.wheretruck.config;

import java.util.Arrays;

import com.gamakdragons.wheretruck.auth.filter.JwtAuthenticationFilter;
import com.gamakdragons.wheretruck.auth.service.JwtProvider;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebConfig {

	private final JwtProvider jwtProvider;
	
	@Bean
	public FilterRegistrationBean<JwtAuthenticationFilter> getFilterRegistrationBean() {
		FilterRegistrationBean<JwtAuthenticationFilter> filterRegistrationBean = 
					new FilterRegistrationBean<>(new JwtAuthenticationFilter(jwtProvider));

		filterRegistrationBean.setUrlPatterns(Arrays.asList("/api/*"));
		return filterRegistrationBean;
	}
}
