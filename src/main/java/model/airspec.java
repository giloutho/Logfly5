/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

/**
 *
 * @author gil
 */
public class airspec {
    
    private int levelTree;
    private String name;
    private String category;   
    private boolean displayed;
      
    public airspec(int pLevelTree, String pName, String pCategory, boolean pDisplayed) {
        this.levelTree = pLevelTree;
        this.name = pName;
        this.category = pCategory;
        this.displayed = pDisplayed;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public boolean isDisplayed() {
        return displayed;
    }

    public void setDisplayed(boolean displayed) {
        this.displayed = displayed;
    }
        
    
}
