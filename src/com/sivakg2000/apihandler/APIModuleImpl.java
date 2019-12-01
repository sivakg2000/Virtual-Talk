/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sivakg2000.apihandler;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.log4j.Logger;

/**
 *
 * @author siva.k
 */
public class APIModuleImpl implements APIModule, Runnable {
    private Logger logger = Logger.getLogger(this.getClass().getName());
    int id;
    APIHandler h;
    String message;
    String response;
    String maildID;

    @Override
    public void startAPIModule(int id, APIHandler h, String maildID, String message) {
        this.id = id;
        this.h = h;
        this.message = message;
        this.maildID = maildID.split("/")[0];
        new Thread(this).start();
    }

    @Override
    public void run() {

        logger.info("Sending API...!");
        logger.info("MSG :" + message);
        boolean sStatus = false;
        String url = "http://projects.sivakg2000.com/qa/ecirecharge.in/recharge-gtalk-api.php?mode=" + message
                + "&useremail=" + maildID + "&app_key=admin@sivakg2000.com";
        // "http://mrcglobe.com/recharge-m-api.php?username="+Common.getUsername()+"&api_key="+Common.getApiKey()+"&mode="
        // + message;

        // url="https://ajax.googleapis.com/ajax/services/search/web?v=1.0&"
        // + "q="+message;
        logger.info("url :" + url);

        HttpURLConnection httpConn = null;

        InputStream is = null;

        try {
            URL urlConnection = new URL(url);
            // Open an HTTP Connection object
            httpConn = (HttpURLConnection) urlConnection.openConnection();

            // Setup HTTP Request
            httpConn.setRequestMethod("GET");
            httpConn.setRequestProperty("User-Agent", "Profile/MIDP-1.0 Confirguration/CLDC-1.0");

            // This function retrieves the information of this connection
            // getConnectionInformation(httpConn);

            /**
             * Initiate connection and check for the response code. If the response code is
             * HTTP_OK then get the content from the target
             **/
            int respCode = httpConn.getResponseCode();
            System.out.println("respCode : " + respCode);
            if (respCode == httpConn.HTTP_OK) {
                StringBuffer sb = new StringBuffer();
                // os = httpConn.getOutputStream();
                is = httpConn.getInputStream();
                int chr;
                while ((chr = is.read()) != -1) {
                    sb.append((char) chr);
                }
                sStatus = true;
                // Web Server just returns the birthday in mm/dd/yy format.
                // System.out.println( " Reply " + sb.toString());
                response = sb.toString().trim();
            } else {
                System.out.println("Error in opening HTTP Connection. Error#" + respCode);
            }

        } catch (IOException ex) {
            System.out.println("IOException" + ex);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            if (httpConn != null) {

                httpConn.disconnect();

            }
        }
        h.setAPIHandler(id, sStatus, response, maildID);
    }

    void getConnectionInformation(HttpURLConnection hc) {

        System.out.println("Request Method for this connection is " + hc.getRequestMethod());
        System.out.println("URL in this connection is " + hc.getURL());
        /*
         * System.out.println("Protocol for this connection is " + hc.getProtocol()); //
         * It better be HTTP:) System.out.println("This object is connected to " +
         * hc.getHost() + " host"); System.out.println("HTTP Port in use is " +
         * hc.getPort()); System.out.println("Query parameter in this request are  " +
         * hc.getQuery());
         */
    }
}
