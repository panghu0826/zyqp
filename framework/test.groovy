	String pwd = new File('build.gradle').absolutePath
	pwd = pwd.replaceAll('\\\\', '/')
	pwd = pwd.replaceAll('framework/build.gradle', 'demo/ws')
	println pwd