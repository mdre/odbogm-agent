package net.odbogm.agent;

import java.lang.reflect.Method;
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
import test.Outer;

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
        for (Class ic : cfc.getInterfaces()) {
            System.out.println("::"+ic.getName());
        }
        assertTrue((Object)fc instanceof ITransparentDirtyDetector);
    }
    
    
    @Test
    public void innerClass() throws Exception {
        Outer outer = new Outer("test");
        assertTrue(outer instanceof ITransparentDirtyDetector);
        assertFalse(((ITransparentDirtyDetector)outer).___ogm___isDirty());
        
        Outer.Inner inner = outer.new Inner();
        assertFalse(((ITransparentDirtyDetector)outer).___ogm___isDirty());
        
        inner.setOuterMember("modified");
        assertEquals("modified", outer.getMember());
        assertTrue(((ITransparentDirtyDetector)outer).___ogm___isDirty());
    }
    
    
    @Test
    public void anonClass() throws Exception {
        Outer outer = new Outer("test");
        assertTrue(outer instanceof ITransparentDirtyDetector);
        assertFalse(((ITransparentDirtyDetector)outer).___ogm___isDirty());
        
        outer.anon();
        assertEquals("run", outer.getMember());
        assertTrue(((ITransparentDirtyDetector)outer).___ogm___isDirty());
    }
    
    
    @Test
    public void lambda() throws Exception {
        Outer outer = new Outer("test");
        assertTrue(outer instanceof ITransparentDirtyDetector);
        assertFalse(((ITransparentDirtyDetector)outer).___ogm___isDirty());
        
        outer.lambda();
        assertEquals("touched", outer.getMember());
        assertTrue(((ITransparentDirtyDetector)outer).___ogm___isDirty());
    }
    
    
    @Test
    public void lambda2() throws Exception {
        Outer outer = new Outer("test");
        assertTrue(outer instanceof ITransparentDirtyDetector);
        assertFalse(((ITransparentDirtyDetector)outer).___ogm___isDirty());
        
        outer.lambda2();
        assertEquals("lambda2", outer.getMember());
        assertTrue(((ITransparentDirtyDetector)outer).___ogm___isDirty());
    }
    
    
    @Test
    public void otherThread() throws Exception {
        Outer outer = new Outer("test");
        assertTrue(outer instanceof ITransparentDirtyDetector);
        assertFalse(((ITransparentDirtyDetector)outer).___ogm___isDirty());
        
        outer.threaded();
        assertEquals("from thread", outer.getMember());
        assertTrue(((ITransparentDirtyDetector)outer).___ogm___isDirty());
    }
    
    
    @Test
    public void publicMembers() throws Exception {
        Outer outer = new Outer("test");
        assertTrue(outer instanceof ITransparentDirtyDetector);
        assertFalse(((ITransparentDirtyDetector)outer).___ogm___isDirty());
        
        //las modificaciones de este tipo no son detectadas por el agente
        outer.publicMember = "editeddd";
        System.out.println("Public member, dirty? " + 
                ((ITransparentDirtyDetector)outer).___ogm___isDirty());
    }
    
    
    @Test
    public void finalClass() throws Exception {
        FinalClass fc = new FinalClass();
        Object fco = (Object)fc;
        assertTrue(fco instanceof ITransparentDirtyDetector);
        assertFalse(((ITransparentDirtyDetector)fco).___ogm___isDirty());
        
        fc.setData("change");
        assertTrue(((ITransparentDirtyDetector)fco).___ogm___isDirty());
        assertEquals("change", fc.getData());
        assertFalse(Modifier.isFinal(FinalClass.class.getModifiers()));
    }
    
    
//    @Test
//    public void finalMethods() throws Exception {
//        Outer outer = new Outer("test");
//        assertTrue(outer instanceof ITransparentDirtyDetector);
//        assertFalse(((ITransparentDirtyDetector)outer).___ogm___isDirty());
//        
//        outer.finalMethod();
//        assertEquals("final", outer.getMember());
//        assertTrue(((ITransparentDirtyDetector)outer).___ogm___isDirty());
//        
//        Method m = outer.getClass().getDeclaredMethod("finalMethod");
//        assertNotNull(m);
//        assertFalse(Modifier.isFinal(m.getModifiers()));
//    }
    
}
