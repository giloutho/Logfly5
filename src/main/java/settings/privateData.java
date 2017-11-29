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
    
    phpSupport ("url support php"),  // php script called for support request
    updateUrl ("url live update"),  // url live update    
    mailUrl  ("mail server"),      // mailserver
    mailUser ("mail user");    // mailserver username
    

    private String name = "";

    //Constructeur
    privateData(String name){
      this.name = name;
    }

    public String toString(){
      return name;
    }
    
}
