package utils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

/**
 * Created by kerry on 2016/9/7.
 * get v4 ip
 */
public class Ip {

    private static List<InetAddress> getAllHostAddress() throws SocketException {
        Enumeration networkInterfaces = NetworkInterface.getNetworkInterfaces();
        List addresses = new ArrayList();

        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = (NetworkInterface) networkInterfaces.nextElement();
            Enumeration inetAddresses = networkInterface.getInetAddresses();

            while (inetAddresses.hasMoreElements()) {
                InetAddress inetAddress = (InetAddress) inetAddresses.nextElement();
                addresses.add(inetAddress);
            }
        }

        return addresses;
    }

    private static List<String> getAllIpv4NoLoopbackAddresses() throws SocketException {
        List<InetAddress> allInetAddresses = getAllHostAddress();
        Iterator i$ = allInetAddresses.iterator();

        List<String> noLoopbackAddresses = new ArrayList<String>();
        while (i$.hasNext()) {
            InetAddress address = (InetAddress) i$.next();
            if (!address.isLoopbackAddress() && address instanceof Inet4Address) {
                noLoopbackAddresses.add(address.getHostAddress());
            }
        }

        return noLoopbackAddresses;
    }

    private static String localIp;

    public static String getLocalIp() {
        if (localIp == null) {
            List<String> v4List = null;
            try {
                v4List = getAllIpv4NoLoopbackAddresses();
            } catch (SocketException e) {
            }
            localIp = v4List != null ? v4List.get(0) : null;
        }
        return localIp;
    }
}
