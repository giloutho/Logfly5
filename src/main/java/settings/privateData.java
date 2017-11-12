/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package settings;

/**
 *
 * @author gil
 */
public enum privateData {
    
    phpSupport ("http://alpidev.com/logfly_contact/support.php"),  // php script called for support request
    updateUrl ("http://www.logfly.org/download/logfly5"),  // url live update    
    mailUrl  ("mail.alpidev.com"),      // mailserver
    mailUser ("logfly@alpidev.com");    // mailserver username
    

    private String name = "";

    //Constructeur
    privateData(String name){
      this.name = name;
    }

    public String toString(){
      return name;
    }
    
}
