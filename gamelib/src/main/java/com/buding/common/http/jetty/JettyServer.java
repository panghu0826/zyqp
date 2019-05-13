package com.buding.common.http.jetty;

import org.eclipse.jetty.server.Server;  
import org.eclipse.jetty.server.nio.SelectChannelConnector;  
import org.eclipse.jetty.webapp.WebAppContext;  
import org.springframework.beans.BeansException;  
import org.springframework.context.ApplicationContext;  
import org.springframework.context.ApplicationContextAware;  
import org.springframework.web.context.WebApplicationContext;  
import org.springframework.web.context.support.XmlWebApplicationContext; 

public class JettyServer implements ApplicationContextAware {
	private Server server;  
    private ApplicationContext applicationContext;
    private String appPath = "src/main/webapp";
    private String cxtPath = "/";
    private int port;
    private boolean enable = false;
    private String host;
  
    @Override  
    public void setApplicationContext(ApplicationContext applicationContext)  
            throws BeansException {  
        this.applicationContext = applicationContext;  
          
    }  
      
    public void start() throws Exception{
    	if(!enable) {
    		return;
    	}
    	
        server = new Server();  
  
        SelectChannelConnector connector = new SelectChannelConnector();  
        connector.setPort(port);
        if(host != null) {
        	connector.setHost(host);
        }
        server.addConnector(connector);  
          
        WebAppContext webAppContext = new WebAppContext();  
  
        webAppContext.setContextPath(cxtPath);  
        webAppContext.setDescriptor(appPath + "/WEB-INF/web.xml");  
        webAppContext.setResourceBase(appPath);  
        webAppContext.setConfigurationDiscovered(true);  
        webAppContext.setParentLoaderPriority(true);  
        server.setHandler(webAppContext);  
        
        // 以下代码是关键  
        webAppContext.setClassLoader(applicationContext.getClassLoader());  
          
        XmlWebApplicationContext xmlWebAppContext = new XmlWebApplicationContext();  
        xmlWebAppContext.setParent(applicationContext);  
        xmlWebAppContext.setConfigLocation("");  
        xmlWebAppContext.setServletContext(webAppContext.getServletContext());  
        xmlWebAppContext.refresh();  
          
        webAppContext.setAttribute(  
                WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE,  
                xmlWebAppContext);  
  
        server.start();  
        
       System.out.println("JettyServer Start On : " + port);
    }

	public String getAppPath() {
		return appPath;
	}

	public void setAppPath(String appPath) {
		this.appPath = appPath;
	}

	public String getCxtPath() {
		return cxtPath;
	}

	public void setCxtPath(String cxtPath) {
		this.cxtPath = cxtPath;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}	
}
