package com.aicas.jamaica.iot.smack;

import java.io.IOException;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class SmackActivator implements BundleActivator
{
    private static final String HOST = "192.168.0.10";
    private static final String DOMAIN = "es-0226.aicas.burg";
    private static final String USERNAME = "colorsortingguisender";
    private static final String PASSWORD = "password";

    public void start(BundleContext arg0) throws Exception
    {
        XMPPTCPConnectionConfiguration connectionConfiguration = XMPPTCPConnectionConfiguration.builder()
                .setDebuggerEnabled(true).setHost(HOST).setSecurityMode(SecurityMode.disabled).setServiceName(DOMAIN)
                .setUsernameAndPassword(USERNAME, PASSWORD).build();

        AbstractXMPPConnection connection = new XMPPTCPConnection(connectionConfiguration);

        while (!connection.isConnected())
        {
            System.out.println("Trying to connect " + USERNAME + "@" + HOST);
            try
            {
                connection.connect().login();
                break;
            } catch (XMPPException e)
            {
                System.err.println(e.getMessage());
            } catch (SmackException e)
            {
                System.err.println(e.getMessage());
            } catch (IOException e)
            {
                System.err.println(e.getMessage());
            }
            try
            {
                Thread.sleep(5000);
            } catch (InterruptedException e)
            {
                System.err.println(e.getMessage());
            }
        }

        System.out.println("connected.");

        MultiUserChatManager multiUserChatManager = MultiUserChatManager.getInstanceFor(connection);
        MultiUserChat multiUserChat = multiUserChatManager.getMultiUserChat("muc@conference.es-0226.aicas.burg");

        try
        {
            multiUserChat.createOrJoin("sender");
        } catch (NoResponseException e)
        {
            System.err.println(e.getMessage());
        } catch (XMPPErrorException e)
        {
            System.err.println(e.getMessage());
        } catch (NotConnectedException e)
        {
            System.err.println(e.getMessage());
        } catch (SmackException e)
        {
            System.err.println(e.getMessage());
        }

        if (multiUserChat.isJoined())
        {
            multiUserChat.addMessageListener(new MessageListener()
            {

                public void processMessage(Message message)
                {

                    System.out.println(message.getBody());

                }

            });
        }
        int i = 0;
        while (true)
        {
            try
            {
                String message = "key:" + i++;
                System.out.println("Send '" + message + "'.");
                multiUserChat.sendMessage(message);
            } catch (NotConnectedException e)
            {
                System.err.println(e.getMessage());
            }
            try
            {
                Thread.sleep(2000);
            } catch (InterruptedException e)
            {
                break;
            }
        }

    }

    public void stop(BundleContext arg0) throws Exception
    {
        // TODO Auto-generated method stub

    }

}
