package ru.gagauz.utils.network;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import java.util.Hashtable;

public class MXLookup {

    public static Attribute doLookup(String hostName) throws NamingException {
        Hashtable env = new Hashtable();
        env.put("java.naming.factory.initial",
                "com.sun.jndi.dns.DnsContextFactory");
        DirContext ictx = new InitialDirContext(env);
        Attributes attrs = ictx.getAttributes(hostName, new String[] {"MX"});
        return attrs.get("MX");
    }

    public static void main(String args[]) {

        try {
            System.out.println(" has " +
                    doLookup("google.com") + " mail servers");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
