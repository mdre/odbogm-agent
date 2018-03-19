package Test.asm;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.odbogm.agent.TransparentDirtyDetectorAgent;

/**
 *
 * @author Marcelo D. Ré {@literal <marcelo.re@gmail.com>}
 */
public class TestASM {

    private final static Logger LOGGER = Logger.getLogger(TestASM.class.getName());

    static {
        LOGGER.setLevel(Level.INFO);
    }

//    static {
//        TransparentDirtyDetectorAgent.initialize("Test.asm");
//    }
    public TestASM() {
        TransparentDirtyDetectorAgent.initialize();
        testConcurso();
    }

    private void testConcurso() {
        
    }

    private void testASM() {
        //            System.out.println("1");
//            ASMTarget asmti = new ASMTarget();
//            
//            System.out.println("2");
//            System.out.println("Dirty: "+((ITransparentDirtyDetector)asmti).___ogm___isDirty());
//            
//            System.out.println("3 - invocando ignore()");
//            asmti.ignore();
//            System.out.println("Dirty: "+((ITransparentDirtyDetector)asmti).___ogm___isDirty());
//            System.out.println("4 - invocando set()");
//            asmti.set();
//            System.out.println("Dirty: "+((ITransparentDirtyDetector)asmti).___ogm___isDirty());

//            System.out.println("5 - probando ASMTargetEx...");
//            ASMTargetEx asmte = new ASMTargetEx();
//            
//            System.out.println("6 - invocando ignore()");
//            asmte.ignore();
//            System.out.println("Dirty: "+((ITransparentDirtyDetector)asmte).___ogm___isDirty());
//            
//            System.out.println("7 - invocando set()");
//            asmte.set();
//            System.out.println("Dirty: "+((ITransparentDirtyDetector)asmte).___ogm___isDirty());
//            
//            System.out.println("8 - invocando init()");
//            asmte.init();
//            System.out.println("Dirty: "+((ITransparentDirtyDetector)asmte).___ogm___isDirty());
//            
//            System.out.println("9 - reseteando a false");
//            ((ITransparentDirtyDetector)asmte).___ogm___setDirty(false);
//            System.out.println("Ex Dirty: "+((ITransparentDirtyDetector)asmte).___ogm___isDirty());
//            System.out.println("orig Dirty: "+((ITransparentDirtyDetector)(ASMTarget)asmte).___ogm___isDirty());
        System.out.println("-------------------------------------------------");

//            System.out.println("asmti:"+asmti.getClass().getClassLoader()+" "+asmti.getClass().getCanonicalName());
//            System.out.println("o: "+o.getClass().getClassLoader()+" "+o.getClass().getCanonicalName());
//            ITransparentDirtyDetector itaAMSTi = (ITransparentDirtyDetector)asmti;
//            System.out.println("1. dirty: "+itaAMSTi.___getDirty());
//            asmti.ignore();
//            System.out.println("2. ignore - dirty: "+itaAMSTi.___getDirty());
//            asmti.set();
//            System.out.println("3. set - dirty: "+itaAMSTi.___getDirty());
    }

    public static void main(String[] args) {
        new TestASM();
    }
}
