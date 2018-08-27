/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.odbogm.agent;

import java.lang.reflect.Modifier;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import test.ColClass;
import test.ExAbsClass;
import test.FinalClass;

/**
 *
 * @author Marcelo D. Ré {@literal <marcelo.re@gmail.com>}
 */
public class TransparentDirtyDetectorTest {
    
    public TransparentDirtyDetectorTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        TransparentDirtyDetectorAgent.initialize();
    }
    
    @After
    public void tearDown() {
    }


    /**
     * Test of initialize method, of class TransparentDirtyDetectorAgent.
     */
    @Test
    public void testObjects() {
        System.out.println("testeando los objetos");
        
        ExAbsClass eac = new ExAbsClass();
        assertTrue(eac instanceof ITransparentDirtyDetector);
        
        System.out.println("validar clases con colecciones de interfaces");
        ColClass cc = new ColClass();
        assertTrue(cc instanceof ITransparentDirtyDetector);
        
        System.out.println("\n\n\n\ninstanciando un FINAL");
        FinalClass fc = new FinalClass();
        
        Class cfc = fc.getClass();
        System.out.println("Modifiers : "+(Modifier.isFinal(cfc.getModifiers())));
        System.out.println("Interfaces:");
        for (Class ic :  cfc.getInterfaces()) {
            System.out.println("::"+ic.getName());
        }
        assertTrue((Object)fc instanceof ITransparentDirtyDetector);
        
        
    }
    
}
