/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sivakg2000.util;

/**
 *
 * @author siva.k
 */
public class Util { 

    public static boolean validatePin(String pin, String mobileNo) {
        boolean rVal = false;
        String cPin = ""; 
        int p1 = Integer.parseInt(mobileNo.substring(0, 1));
        for(int i=0;i<4;i++){
             p1--;
            cPin+=mobileNo.substring(p1, p1+1);
           

        }


        //System.out.println("rPin " + rPin);
        System.out.println("cPin " + cPin);
        if (pin.equals(cPin)) {
            rVal = true;
        }

        return rVal;
    }

    public static boolean validatePin1(String pin, String mobileNo) {
        boolean rVal = false;
         
        int p1 = Integer.parseInt(pin.substring(0, 1));
        String rPin = pin.substring(1);
        String cPin = "";
        if (p1 < 5) {
            if (p1 % 2 == 0) {
                cPin = mobileNo.substring(0, 1) + mobileNo.substring(2, 3) + mobileNo.substring(4, 5);
            } else {
                cPin = mobileNo.substring(1, 2) + mobileNo.substring(3, 4) + mobileNo.substring(5, 6);
            }
        } else {
            if (p1 % 2 == 0) {
                cPin = mobileNo.substring(8, 9) + mobileNo.substring(6, 7) + mobileNo.substring(4, 5);
            } else {
                cPin = mobileNo.substring(9) + mobileNo.substring(7, 8) + mobileNo.substring(5, 6);
            }
        }
        //System.out.println("rPin " + rPin);
        //System.out.println("cPin " + cPin);
        if (rPin.equals(cPin)) {
            rVal = true;
        }

        return rVal;
    }
}

