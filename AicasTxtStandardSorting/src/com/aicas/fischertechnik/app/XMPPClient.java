package com.aicas.fischertechnik.app;

import java.io.IOException;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

public class XMPPClient
{
    private static final String HOST = "192.168.0.10";
    private static final String DOMAIN = "es-0226.aicas.burg";
    private static final String USERNAME = "colorsortingguisender";
    private static final String PASSWORD = "password";
    
    public static AbstractXMPPConnection connect(String username, String password)
    {
      XMPPTCPConnectionConfiguration connectionConfiguration = XMPPTCPConnectionConfiguration.builder().
          setDebuggerEnabled(true).
          setHost(HOST).
          setSecurityMode(SecurityMode.disabled).
          setServiceName(DOMAIN).
          setUsernameAndPassword(username, password).
          build();

      AbstractXMPPConnection connection = new XMPPTCPConnection(connectionConfiguration);

      while (!connection.isConnected())
        {
          System.out.println("Trying to connect " + username + "@" + HOST);
          try
            {
              connection.connect().login();
              break;
            }
          catch (XMPPException e)
            {
              System.err.println(e.getMessage());
            }
          catch (SmackException e)
            {
              System.err.println(e.getMessage());
            }
          catch (IOException e)
            {
              System.err.println(e.getMessage());
            }
          try
            {
              Thread.sleep(5000);
            }
          catch (InterruptedException e)
            {
              System.err.println(e.getMessage());
            }
        }

      System.out.println("connected.");
      return connection;
    }
}
