package main;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.kohsuke.args4j.CmdLineParser;

import com.buding.test.Looper;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
public class PokerMain {
	public static void main(String[] args) throws Exception {
//		Looper l = new Looper("www.xskdqmj3d.cn", 5000, "jaime", "123456");
//		Looper l = new Looper("www.xskdqmj3d.cn", 5000, "jaime", "123456");
//		do {
//			System.out.println("===============================================");
//			l.run();
//			Thread.sleep(1000);
//		} while (true);
		
		CmdArgs a = new CmdArgs();
		CmdLineParser parser = new CmdLineParser(a);
		parser.parseArgument(args);
		
		if(a.multiUser == 1) {
			Looper l = new Looper(a.server, 5000, "a2", "123456");
			Looper l2 = new Looper(a.server, 5000, "a3", "123456");
			Looper l3 = new Looper(a.server, 5000, "a4", "123456");
			Looper l4 = new Looper(a.server, 5000, "a5", "123456");
			
			ScheduledExecutorService pool = Executors.newScheduledThreadPool(4);
			pool.scheduleAtFixedRate(l, 1, 1, TimeUnit.SECONDS);
			pool.scheduleAtFixedRate(l2, 1, 1, TimeUnit.SECONDS);
			pool.scheduleAtFixedRate(l3, 1, 1, TimeUnit.SECONDS);
			pool.scheduleAtFixedRate(l4, 1, 1, TimeUnit.SECONDS);
		} else {
			Looper l = new Looper(a.server, 5000, a.user, a.passwd);
			do {
//				System.out.println("===============================================");
				l.run();
				Thread.sleep(1000);
			} while (true);
		}
	}
}
