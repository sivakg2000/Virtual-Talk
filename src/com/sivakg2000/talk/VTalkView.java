/*
 * IMateGTalkView.java
 */
package com.sivakg2000.talk;

import com.sivakg2000.talk.log.TextAreaAppender;
import com.sivakg2000.talk.impl.ChatClient;
import com.sivakg2000.talk.log.LabelAppender;
import java.awt.AWTException;
import java.awt.Frame;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Properties;
import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * The application's main frame.
 */
public class IMateTalkView extends FrameView {

    ChatClient objChat = new ChatClient();
    private Logger logger = Logger.getLogger(this.getClass().getName());
    private DefaultListModel userListModel = new DefaultListModel();
    private LogViewer logViewer;
    private Image appIcon;

    public DefaultListModel getUserListModel() {
        return userListModel;
    }

    public static IMateTalkView instance;
    public static JPanel mPanel;

    public IMateTalkView(SingleFrameApplication app) {
        super(app);

        /*
         * try {
         * 
         * Object laf =
         * Class.forName("org.jvnet.substance.skin.OfficeSilver2007Skin").newInstance();
         * 
         * if (laf instanceof LookAndFeel){ UIManager.setLookAndFeel((LookAndFeel) laf);
         * } else if (laf instanceof SubstanceSkin) {
         * SubstanceLookAndFeel.setSkin((SubstanceSkin) laf); } } catch (Exception e) {
         * System.out.println("Exception "+e.getMessage()); }
         * 
         * 
         */
        initComponents();

        // status bar initialization - message timeout, idle icon and busy animation,
        // etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {

            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String) (evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer) (evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });

        if (logViewer == null) {
            JFrame mainFrame = IMateTalkApp.getApplication().getMainFrame();
            logViewer = new LogViewer(mainFrame, false);
            logViewer.setLocationRelativeTo(mainFrame);
        }

        if (aboutBox == null) {
            JFrame mainFrame = IMateTalkApp.getApplication().getMainFrame();
            aboutBox = new IMateTalkAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }

        if (preferencesBox == null) {
            JFrame mainFrame = IMateTalkApp.getApplication().getMainFrame();
            preferencesBox = new Preferences(mainFrame, true);
            preferencesBox.setLocationRelativeTo(mainFrame);
        }

        lstUser.setModel(userListModel);
        getFrame().setResizable(false);

        try {

            appIcon = ImageIO.read(IMateTalkView.class.getResourceAsStream("/com/sivakg2000/talk/resources/logo.png"));
            getFrame().setIconImage(appIcon);
            loadSystemTray();

        } catch (AWTException ex) {
            System.out.println("System tray error");
        } catch (IOException e) {
            System.out.println("Error");
        }

        IMateTalkView.setupLog4JAppender(statusMessageLabel, LogViewer.txtLogView);
        instance = this;
        mPanel = mainPanel;

    }

    private void loadSystemTray() throws AWTException {

        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
            return;
        }
        SystemTray tray = SystemTray.getSystemTray();
        Toolkit toolkit = Toolkit.getDefaultToolkit();

        PopupMenu menu = new PopupMenu();

        MenuItem messageItemAbout = new MenuItem("About");
        messageItemAbout.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                IMateTalkApp.getApplication().show(aboutBox);
            }
        });
        menu.add(messageItemAbout);

        MenuItem messageItemShowLogviewer = new MenuItem("Show LogViewer");
        messageItemShowLogviewer.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                IMateTalkApp.getApplication().show(logViewer);
            }
        });
        menu.add(messageItemShowLogviewer);

        MenuItem messageItemShow = new MenuItem("Show");
        messageItemShow.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                /*
                 * IMateTalkApp.getApplication().getMainView().setComponent(mainPanel);
                 * IMateTalkApp.getApplication().getMainView().setMenuBar(menuMain);
                 * IMateTalkApp.getApplication().getMainView().setStatusBar(statusPanel);
                 * IMateTalkApp.getApplication().getMainFrame().show();
                 */
                IMateTalkApp.getApplication().getMainFrame().setExtendedState(Frame.ICONIFIED);

                // IMateTalkView.instance.
                // IMateTalkApp.
            }
        });
        menu.add(messageItemShow);

        MenuItem messageItemPreferences = new MenuItem("Preferences");
        messageItemPreferences.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                IMateTalkApp.getApplication().show(preferencesBox);
            }
        });
        menu.add(messageItemPreferences);

        MenuItem closeItem = new MenuItem("Exit");
        closeItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        menu.add(closeItem);
        TrayIcon icon = new TrayIcon(appIcon, "V Virtual Talk", menu);
        icon.setImageAutoSize(true);
        icon.setToolTip("V Virtual-Talk");
        tray.add(icon);
        icon.displayMessage("V Virtual-Talk", "Started...!", MessageType.INFO);
    }

    @Action
    public void showAboutBox() {

        IMateTalkApp.getApplication().show(aboutBox);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated
    // Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        lstUser = new javax.swing.JList();
        btnStart = new javax.swing.JButton();
        menuMain = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        menuOpenLog = new javax.swing.JMenuItem();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        menuView = new javax.swing.JMenu();
        menuItemLogViewer = new javax.swing.JMenuItem();
        menuSetting = new javax.swing.JMenu();
        menuItemPreferences = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        jMenuItem2 = new javax.swing.JMenuItem();
        javax.swing.JMenu menuHelp = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        fileChooserOpenLog = new javax.swing.JFileChooser();

        mainPanel.setName("mainPanel"); // NOI18N

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        lstUser.setName("lstUser"); // NOI18N
        jScrollPane2.setViewportView(lstUser);

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application
                .getInstance(com.sivakg2000.talk.IMateTalkApp.class).getContext().getResourceMap(IMateTalkView.class);
        btnStart.setText(resourceMap.getString("btnStart.text")); // NOI18N
        btnStart.setName("btnStart"); // NOI18N
        btnStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStartActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(mainPanelLayout
                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(mainPanelLayout.createSequentialGroup().addGroup(mainPanelLayout
                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(mainPanelLayout.createSequentialGroup().addContainerGap().addComponent(jScrollPane2,
                                javax.swing.GroupLayout.PREFERRED_SIZE, 285, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(mainPanelLayout.createSequentialGroup().addGap(122, 122, 122).addComponent(btnStart)))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        mainPanelLayout.setVerticalGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
                        mainPanelLayout.createSequentialGroup().addContainerGap()
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 565, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnStart).addGap(7, 7, 7)));

        menuMain.setName("menuMain"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        menuOpenLog.setAccelerator(
                javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        menuOpenLog.setText(resourceMap.getString("menuOpenLog.text")); // NOI18N
        menuOpenLog.setName("menuOpenLog"); // NOI18N
        menuOpenLog.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuOpenLogActionPerformed(evt);
            }
        });
        fileMenu.add(menuOpenLog);

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application
                .getInstance(com.sivakg2000.talk.IMateTalkApp.class).getContext()
                .getActionMap(IMateTalkView.class, this);
        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuMain.add(fileMenu);

        menuView.setText(resourceMap.getString("menuView.text")); // NOI18N
        menuView.setName("menuView"); // NOI18N

        menuItemLogViewer.setAccelerator(
                javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.CTRL_MASK));
        menuItemLogViewer.setText(resourceMap.getString("menuItemLogViewer.text")); // NOI18N
        menuItemLogViewer.setName("menuItemLogViewer"); // NOI18N
        menuItemLogViewer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemLogViewerActionPerformed(evt);
            }
        });
        menuView.add(menuItemLogViewer);

        menuMain.add(menuView);

        menuSetting.setText(resourceMap.getString("menuSetting.text")); // NOI18N
        menuSetting.setName("menuSetting"); // NOI18N

        menuItemPreferences.setAccelerator(
                javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.CTRL_MASK));
        menuItemPreferences.setText(resourceMap.getString("menuItemPreferences.text")); // NOI18N
        menuItemPreferences.setName("menuItemPreferences"); // NOI18N
        menuItemPreferences.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemPreferencesActionPerformed(evt);
            }
        });
        menuSetting.add(menuItemPreferences);

        jSeparator1.setName("jSeparator1"); // NOI18N
        menuSetting.add(jSeparator1);

        jMenuItem2.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER,
                java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem2.setText(resourceMap.getString("jMenuItem2.text")); // NOI18N
        jMenuItem2.setName("jMenuItem2"); // NOI18N
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        menuSetting.add(jMenuItem2);

        menuMain.add(menuSetting);

        menuHelp.setText(resourceMap.getString("menuHelp.text")); // NOI18N
        menuHelp.setName("menuHelp"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        menuHelp.add(aboutMenuItem);

        menuMain.add(menuHelp);

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(statusPanelLayout
                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 305, Short.MAX_VALUE)
                .addGroup(statusPanelLayout.createSequentialGroup().addContainerGap().addComponent(statusMessageLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 135, Short.MAX_VALUE)
                        .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(statusAnimationLabel).addContainerGap()));
        statusPanelLayout.setVerticalGroup(statusPanelLayout
                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(statusPanelLayout.createSequentialGroup()
                        .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2,
                                javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(statusMessageLabel).addComponent(statusAnimationLabel)
                                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(3, 3, 3)));

        fileChooserOpenLog.setDialogTitle(resourceMap.getString("fileChooserOpenLog.dialogTitle")); // NOI18N
        fileChooserOpenLog.setFileFilter(null);
        fileChooserOpenLog.setName("fileChooserOpenLog"); // NOI18N

        setComponent(mainPanel);
        setMenuBar(menuMain);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

    private void menuItemPreferencesActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_menuItemPreferencesActionPerformed
        // TODO add your handling code here:

        IMateTalkApp.getApplication().show(preferencesBox);

    }// GEN-LAST:event_menuItemPreferencesActionPerformed

    private void btnStartActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnStartActionPerformed

        if (btnStart.getText().equals("Start")) {
            logger.info("Connecting...!");
            objChat.startService();
            btnStart.setText("Stop");
        } else {
            logger.info("Disconnecting...!");
            objChat.disconnect();
            btnStart.setText("Start");
        }

    }// GEN-LAST:event_btnStartActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jMenuItem2ActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_jMenuItem2ActionPerformed

    private void menuOpenLogActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_menuOpenLogActionPerformed
        // TODO add your handling code here:
        JFrame mainFrame = IMateTalkApp.getApplication().getMainFrame();
        int returnVal = fileChooserOpenLog.showOpenDialog(mainFrame);
    }// GEN-LAST:event_menuOpenLogActionPerformed

    private void menuItemLogViewerActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_menuItemLogViewerActionPerformed
        // TODO add your handling code here:
        IMateTalkApp.getApplication().show(logViewer);

    }// GEN-LAST:event_menuItemLogViewerActionPerformed

    protected static void setupLog4JAppender(JLabel jLabel, JTextArea jTextArea) {
        // This code attaches the appender to the text area
        LabelAppender.setLabel(jLabel);
        TextAreaAppender.setTextArea(jTextArea);

        Properties logProperties = new Properties();
        logProperties.put("log4j.rootLogger", "INFO,TEXTAREA,LABEL,FILE");
        /*
         * logProperties.put("log4j.appender.CONSOLE",
         * "org.apache.log4j.ConsoleAppender");
         * logProperties.put("log4j.appender.CONSOLE.layout",
         * "org.apache.log4j.PatternLayout");
         * logProperties.put("log4j.appender.CONSOLE.layout.ConversionPattern",
         * "%d{yyyy-MM-dd HH:mm:ss} : %m%n");
         */

        logProperties.put("log4j.appender.FILE", "org.apache.log4j.FileAppender");
        logProperties.put("log4j.appender.FILE.File", "iMateTalk.log");
        logProperties.put("log4j.appender.FILE.layout", "org.apache.log4j.PatternLayout");
        logProperties.put("log4j.appender.FILE.layout.ConversionPattern", "%d{yyyy-MM-dd HH:mm:ss} : %m%n");

        logProperties.put("log4j.appender.TEXTAREA", "com.sivakg2000.talk.log.TextAreaAppender");
        logProperties.put("log4j.appender.TEXTAREA.layout", "org.apache.log4j.PatternLayout");
        logProperties.put("log4j.appender.TEXTAREA.layout.ConversionPattern", "%d{yyyy-MM-dd HH:mm:ss} : %m%n");

        logProperties.put("log4j.appender.LABEL", "com.sivakg2000.talk.log.LabelAppender");
        logProperties.put("log4j.appender.LABEL.layout", "org.apache.log4j.PatternLayout");
        logProperties.put("log4j.appender.LABEL.layout.ConversionPattern", "%m%n");

        PropertyConfigurator.configure(logProperties);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnStart;
    private javax.swing.JFileChooser fileChooserOpenLog;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JList lstUser;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuItem menuItemLogViewer;
    private javax.swing.JMenuItem menuItemPreferences;
    private javax.swing.JMenuBar menuMain;
    private javax.swing.JMenuItem menuOpenLog;
    private javax.swing.JMenu menuSetting;
    private javax.swing.JMenu menuView;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    // End of variables declaration//GEN-END:variables
    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;
    private JDialog aboutBox;
    private JDialog preferencesBox;
}
