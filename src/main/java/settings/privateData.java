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
    phpMail ("http://alpidev.com/logfly_contact/mailpj.php"),  // php script called for support request
    updateUrl ("http://www.logfly.org/download/logfly5"),  // url live update    
    mailSupport ("contact@logfly.org"),
    sitesUrl ("http://logfly.org/download/sites"), 
    elevationKey ("AIzaSyBIPOz7GUwpdrsPkrzjpq9V5kOBZSbgqCs");
    

    private String name = "";

    //Constructeur
    privateData(String name){
      this.name = name;
    }

    public String toString(){
      return name;
    }
    
}
