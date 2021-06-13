package com.gamakdragons.wheretruck.config;

import java.util.Arrays;

import com.gamakdragons.wheretruck.auth.filter.JwtAuthenticationFilter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebConfig {

	@Bean
	public FilterRegistrationBean<JwtAuthenticationFilter> getFilterRegistrationBean() {
		FilterRegistrationBean<JwtAuthenticationFilter> filterRegistrationBean = 
					new FilterRegistrationBean<>(new JwtAuthenticationFilter());

		filterRegistrationBean.setUrlPatterns(Arrays.asList("/api/*"));
		return filterRegistrationBean;
	}
}
