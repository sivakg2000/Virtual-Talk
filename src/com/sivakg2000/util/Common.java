/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sivakg2000.util;

import com.sivakg2000.talk.VTalkView;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Vector;
import org.apache.log4j.Logger;

/**
 *
 * @author siva.k
 */
public class Common {

    private static Logger logger = Logger.getLogger(Common.class);
    private static Properties prop = new Properties();

    public static void saveLoginDetails(String uName, String uPassword) {

        try {
            prop.setProperty("username", uName);
            prop.setProperty("password", uPassword);
            prop.store(new FileOutputStream("config.properties"), null);

        } catch (IOException ex) {
            logger.error("Property ", ex);
        }
    }

    public static String[] getLoginDetails() {
        String[] rVal = new String[2];
        try {
            prop.load(new FileInputStream("config.properties"));
            rVal[0] = prop.getProperty("username");
            rVal[1] = prop.getProperty("password");

        } catch (IOException ex) {
            logger.error("Property ", ex);
        }
        return rVal;
    }
}
