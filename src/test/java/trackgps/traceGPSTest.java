/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package trackgps;

import java.io.File;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import settings.configProg;

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
            File fIGC = new File("/Users/gil/Documents/Logfly/flytec/vol2.igc");   // unvalid
            //File fIGC = new File("/Users/gil/Documents/Logfly/flytec/unvalid.igc");  
            traceGPS instance = new traceGPS(fIGC, "IGC",true, myConfig);   
            assertFalse(instance.isDecodage());                      
            if (instance.isDecodage())  {            
                System.out.println("Valid track - Gross points : "+instance.Tb_Tot_Points.size()+" valid points : "+instance.Tb_Good_Points.size());
            } else {
                try {
                    if (instance.Tb_Tot_Points.size() > 0)  {
                        System.out.println("Unvalid track - Gross points : "+instance.Tb_Tot_Points.size()+" valid points : "+instance.Tb_Good_Points.size());
                    } else {
                        System.out.println("Unvalid track - No points in this file ");
                    }
                } catch (Exception e) {
                }
            }                
        }
    }
    
}
