/*
 * VGTalkApp.java
 */
package com.sivakg2000.talk;

import org.apache.log4j.Logger;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

/**
 * The main class of the application.
 */
public class VTalkApp extends SingleFrameApplication {

    private Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * At startup create and show the main frame of the application.
     */
    @Override
    protected void startup() {
        show(new VTalkView(this));
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI builder,
     * so this additional configuration is not needed.
     */
    @Override
    protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * 
     * @return the instance of VGTalkApp
     */
    public static VTalkApp getApplication() {
        return Application.getInstance(VTalkApp.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(final String[] args) {

        /*
         * try {
         * 
         * Object laf =
         * Class.forName("org.jvnet.substance.skin.OfficeSilver2007Skin").newInstance();
         * 
         * if (laf instanceof LookAndFeel) { UIManager.setLookAndFeel((LookAndFeel)
         * laf); } else if (laf instanceof SubstanceSkin) {
         * SubstanceLookAndFeel.setSkin((SubstanceSkin) laf); } } catch (Exception e) {
         * System.out.println("Exception " + e.getMessage()); }
         * 
         */
        launch(VTalkApp.class, args);

    }
}
