/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package org.logfly.test.trackgps;

import java.io.File;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.logfly.settings.configProg;
import org.logfly.trackgps.traceGPS;

import static org.junit.Assert.*;
import org.junit.Before;

/**
 *
 * @author gil
 */
public class traceGPSTest {
    
    public traceGPSTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testIsDecodage() {
         // Reading settings requested
        configProg myConfig = new configProg();
        myConfig.readSettings();         
                                  
        if (myConfig.isValidConfig()) {        
            //File fIGC = new File("/Users/gil/Documents/Logflya/Clem_Turq1.IGC");   // OK
            File fIGC = new File("/Users/gil/Documents/Logfly/17060201.IGC");   // unvalid
            //File fIGC = new File("/Users/gil/Documents/Logfly/flytec/unvalid.igc");  
            traceGPS instance = new traceGPS(fIGC, true, myConfig);                       
            if (instance.isDecodage())  {            
                System.out.println("H Deco : "+instance.getDT_Deco()+" h Att : "+instance.getDT_Attero()+" durée : "+instance.getDuree_Vol()+" "+instance.getColDureeVol());
            }            
            traceGPS instance2 = new traceGPS(fIGC,false, myConfig);           
            if (instance2.isDecodage())  {            
                System.out.println("H Deco : "+instance2.getDT_Deco()+" h Att : "+instance2.getDT_Attero()+" durée : "+instance2.getDuree_Vol()+" "+instance2.getColDureeVol());
            }            
        }
    }
    
}
