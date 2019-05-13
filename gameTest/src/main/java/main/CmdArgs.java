package main;

import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
public class CmdArgs {
	@Option(name = "-s")
	public String server = "www.xskdqmj3d.cn";
	
	@Option(name = "-u")
	public String user = "a2";
	
	@Option(name = "-p")
	public String passwd = "123456";
	
	@Option(name = "-m")
	public int multiUser = 1; 

	@Argument
	public List<String> arguments = new ArrayList<String>();
}
