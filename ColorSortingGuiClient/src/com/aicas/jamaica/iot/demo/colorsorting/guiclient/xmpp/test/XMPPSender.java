package com.aicas.jamaica.iot.demo.colorsorting.guiclient.xmpp.test;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;

import com.aicas.jamaica.iot.demo.colorsorting.guiclient.xmpp.XMPPClient;

public class XMPPSender
{
  private boolean active = true;

  public static void main(String[] args)
  {
    new XMPPSender().start();
  }

  void start()
  {
    AbstractXMPPConnection connection = XMPPClient.connect("colorsortingguitest", "password");
    MultiUserChatManager multiUserChatManager = MultiUserChatManager.getInstanceFor(connection);
    MultiUserChat multiUserChat = multiUserChatManager.getMultiUserChat("muc@conference.es-0226.aicas.burg");

    try
      {
        multiUserChat.createOrJoin("sender");
      }
    catch (NoResponseException e)
      {
        System.err.println(e.getMessage());
      }
    catch (XMPPErrorException e)
      {
        System.err.println(e.getMessage());
      }
    catch (NotConnectedException e)
      {
        System.err.println(e.getMessage());
      }
    catch (SmackException e)
      {
        System.err.println(e.getMessage());
      }

    if (multiUserChat.isJoined())
      {
        multiUserChat.addMessageListener(new MessageListener()
        {
          @Override
          public void processMessage(Message message)
          {
            System.out.println(message.getBody());
          }
        });
      }int i = 0;
    while(active)
      {
        try
          {
            String message = "key:" + i++;
            System.out.println("Send '" + message + "'.");
            multiUserChat.sendMessage(message);
          }
        catch (NotConnectedException e)
          {
            System.err.println(e.getMessage());
          }
        try
          {
            Thread.sleep(2000);
          }
        catch (InterruptedException e)
          {
            break;
          }
      }
  }
}
