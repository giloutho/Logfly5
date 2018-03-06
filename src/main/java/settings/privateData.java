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
    phpMail ("url mailing script"),  // php script called for support request
    updateUrl ("url live update"),  // url live update    
    mailSupport ("mail support adress"),
    sitesUrl ("download sites url"), 
    elevationKey ("Google elevation API key");   
    

    private String name = "";

    //Constructeur
    privateData(String name){
      this.name = name;
    }

    public String toString(){
      return name;
    }
    
}
