package com.aicas.jamaica.iot.demo.colorsorting.guiclient.xmpp;

import java.io.IOException;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;

import com.aicas.jamaica.iot.demo.colorsorting.guiclient.model.impl.XMPPColorSortingGUIModelImpl;

public class XMPPClient
{
  private static final String HOST = "192.168.0.10";
  private static final String DOMAIN = "es-0226.aicas.burg";
  private static final String USERNAME = "colorsortingguiclient";
  private static final String PASSWORD = "password";

//  private static Object[] LOCK = new Object[0];
  private final XMPPModelParser modelParser;

  public XMPPClient(XMPPColorSortingGUIModelImpl model) {
    modelParser = new XMPPModelParser(model);
  }

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

  void listen(AbstractXMPPConnection connection)
  {
    MultiUserChatManager multiUserChatManager = MultiUserChatManager.getInstanceFor(connection);
    MultiUserChat multiUserChat = multiUserChatManager.getMultiUserChat("muc@conference.es-0226.aicas.burg");
    System.out.println(multiUserChat);
    try
      {
        multiUserChat.createOrJoin("client");
      }
    catch (NoResponseException e)
      {
        System.err.println(e + ": " + e.getMessage());
      }
    catch (XMPPErrorException e)
      {
        System.err.println(e + ": " + e.getMessage());
      }
    catch (NotConnectedException e)
      {
        System.err.println(e + ": " + e.getMessage());
      }
    catch (SmackException e)
      {
        System.err.println(e + ": " + e.getMessage());
	}

    boolean isJoined = multiUserChat.isJoined();
    System.out.println("isJoined = " + isJoined);
    if (isJoined)
      {
        multiUserChat.addMessageListener(new MessageListener()
        {
          @Override
          public void processMessage(Message message)
          {
            String body = message.getBody();
            System.out.println("received Message: " + body);
            modelParser.parse(body);
          }
        });
      }
  }

public void start()
  {
    listen(connect(USERNAME, PASSWORD));
//    synchronized(LOCK)
//    {
//      try
//        {
//          LOCK.wait();
//        }
//      catch (InterruptedException e)
//        {
//          //
//        }
//    }
  }
}
