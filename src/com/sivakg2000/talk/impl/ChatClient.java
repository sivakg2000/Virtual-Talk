/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sivakg2000.talk.impl;

import com.sivakg2000.apihandler.APIHandler;
import com.sivakg2000.apihandler.APIModuleImpl;
import com.sivakg2000.talk.VTalkView;
import com.sivakg2000.util.Common;
import java.util.Collection;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

/**
 *
 * @author Siva.K
 */
public class ChatClient implements MessageListener, APIHandler, Runnable {

    private Logger logger = Logger.getLogger(this.getClass().getName());
    XMPPConnection connection;
    Chat chat1;
    APIModuleImpl sendAPI = new APIModuleImpl();
    ChatClient cVal = null;

    public ChatClient() {
        cVal = this;
    }

    public void login(String userName, String password) throws XMPPException {
        ConnectionConfiguration config = new ConnectionConfiguration("talk.google.com", 5222, "gmail.com");
        config.setCompressionEnabled(true);
        config.setSASLAuthenticationEnabled(false);
        connection = new XMPPConnection(config);

        connection.connect();
        connection.login(userName, password);

        PacketListener pl = new PacketListener() {

            @Override
            public void processPacket(Packet p) {
                // Common.log(p.getFrom() + ": " + p.toString());
                if (p instanceof Message) {
                    Message msg = (Message) p;
                    if (msg.getBody() != null) {
                        logger.info(msg.getFrom() + ": " + msg.getBody());
                        new APIModuleImpl().startAPIModule(1, cVal, msg.getFrom(), msg.getBody());
                    }
                }
            }
        };

        connection.addPacketListener(pl, null);
    }

    public void sendMessage(String message, String to) throws XMPPException {
        Chat chat = connection.getChatManager().createChat(to, this);
        chat.sendMessage(message);
    }

    public void displayBuddyList() {

        Roster roster = connection.getRoster();
        roster.setSubscriptionMode(Roster.SubscriptionMode.accept_all);
        Collection<RosterEntry> entries = roster.getEntries();

        roster.addRosterListener(new RosterListener() {
            // Ignored events public void entriesAdded(Collection<String> addresses) {}

            public void entriesDeleted(Collection<String> addresses) {
                logger.info("entriesDeleted...!");
            }

            public void entriesUpdated(Collection<String> addresses) {
                logger.info("entriesUpdated...!");
            }

            public void presenceChanged(Presence presence) {

                // System.out.println("Presence changed: " + presence.getFrom() + " " +
                // presence);
                logger.info("presenceChanged...!" + presence.getFrom() + " " + presence);
            }

            public void entriesAdded(Collection<String> clctn) {
                logger.info("entriesAdded...!");
            }
        });

        for (RosterEntry r : entries) {
            // connection.getChatManager().createChat(r.getUser(), this);
            // logger.info(r.getUser());
            VTalkView.instance.getUserListModel().addElement(r.getUser());

        }
    }

    public void disconnect() {
        connection.disconnect();
        logger.info("Disconnected...!");
    }

    @Override
    public void processMessage(Chat chat, Message message) {
    }

    @Override
    public void setAPIHandler(int id, boolean sentStatus, String response, String emailID) {
        logger.info("API Response FOR :" + emailID);
        logger.info("API Response :" + response);
        try {
            this.sendMessage(response, emailID);
        } catch (Exception ex) {
            System.out.println("Error :" + ex);
        }

    }

    public void run() {
        XMPPConnection.DEBUG_ENABLED = false;
        try {

            String[] loginVal = Common.getLoginDetails();
            login(loginVal[0], loginVal[1]);
            logger.info("Connected...!");
            displayBuddyList();

        } catch (XMPPException ex) {
            logger.error("XMPPException", ex);
        }

    }

    public void startService() {
        new Thread(this).start();
    }
}
