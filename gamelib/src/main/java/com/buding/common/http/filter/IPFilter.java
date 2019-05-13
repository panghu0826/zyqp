package com.buding.common.http.filter;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class IPFilter implements Filter {
	Logger logger = LogManager.getLogger(getClass());

	protected FilterConfig filterConfig;
	
	protected boolean enable = true;

	protected Set<String> filterIPSet = new HashSet<String>(); // 要过滤的IP

	/* 初始化 */
	public void init(FilterConfig config) throws ServletException {

		this.filterConfig = config;
		String filterIP = config.getInitParameter("filterIP");// 从web.xml中获取初始化参数
		System.out.println("要过滤的IP:" + filterIP);
		if (filterIP != null) {
			for (String item : filterIP.split(",")) {
				filterIPSet.add(item);
			}
		}
		
		enable = "true".equalsIgnoreCase(config.getInitParameter("enable"));
	}

	/* 过滤器方法 */
	public void doFilter(ServletRequest reg, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		if(!enable) {
			chain.doFilter(reg, res);
			return;
		}
		
		String remoteIP = reg.getRemoteAddr();// 获取访问服务器的IP		
		if (filterIPSet.contains(remoteIP)) {
			chain.doFilter(reg, res);
		} else {
			logger.info("acessDenie,ip:" + remoteIP);
		}
	}

	/* 销毁方法 */
	public void destroy() {
		this.filterConfig = null;
	}
}