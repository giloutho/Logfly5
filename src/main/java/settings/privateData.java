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
    
    phpSupport ("adress of php support script"),  // php script called for support request
    updateUrl ("adress of live update"),  // url live update    
    mailUrl  ("mail.serveur.com"),      // mailserver
    mailUser ("mailuser@serveur.com");    // mailserver username
    

    private String name = "";

    //Constructeur
    privateData(String name){
      this.name = name;
    }

    public String toString(){
      return name;
    }
    
}
