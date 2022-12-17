/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.odbogm.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 *
 * @author Marcelo D. Ré {@literal <marcelo.re@gmail.com>}
 */
public class InstrumentableClassDetector extends ClassVisitor  {

    private final static Logger LOGGER = Logger.getLogger(InstrumentableClassDetector.class.getName());
    static {
        if (LOGGER.getLevel() == null) {
            LOGGER.setLevel(LogginProperties.InstrumentableClassDetector);
        }
    }
    
    private boolean isInstrumentable = false;
    private boolean isInstrumented = false;
    private boolean hasDefaultContructor = false;
    private List<String> innerClasses = new ArrayList<>();
    private List<String> ignoredFields = new ArrayList();
    private String clazzName = null;
    
    public InstrumentableClassDetector(ClassVisitor cv) {
        super(Opcodes.ASM7, cv);
    }

    @Override
    public synchronized void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        String ddi = ITransparentDirtyDetector.class.getName().replace(".", "/");
        this.clazzName = name;
        for (String aInterface : interfaces) {
            if (aInterface.equals(ddi)) {
                // si tiene la interface implementada marcarla como instrumentada
                LOGGER.log(Level.FINER, "La clase "+name+" ya ha sido instrumentada. No hacer nada.");
                this.isInstrumented = true;
            }
        }
        cv.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public synchronized  AnnotationVisitor visitAnnotation(String ann, boolean bln) {
        LOGGER.log(Level.FINEST, "//=====================================================");
        LOGGER.log(Level.FINEST, "Annotations: ");
        LOGGER.log(Level.FINEST, "//=====================================================");
        LOGGER.log(Level.FINEST, "Annotation: >"+ann+"<");
        if (ann.startsWith("Lnet/odbogm/annotations/Entity")) {
            LOGGER.log(Level.FINER, clazzName + ": Annotation: >"+ann+"<");
            LOGGER.log(Level.FINER, ">>>>>>>>>>> marcar como instrumentable");
            this.isInstrumentable = true;
        }
        
        LOGGER.log(Level.FINEST, "//=====================================================");
        return super.visitAnnotation(ann, bln); 
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        // TODO Auto-generated method stub
        if (this.isInstrumentable && (access & Opcodes.ACC_TRANSIENT)!= 0) {
            this.ignoredFields.add(name);
        }
        return super.visitField(access, name, descriptor, signature, value);
    }

    @Override
    public synchronized MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv;
        LOGGER.log(Level.FINEST, "visitando método: " + name + " desc: " + desc + " signature: "+signature);
        mv = cv.visitMethod(access, name, desc, signature, exceptions);
        if ((mv != null) && name.equals("<init>") && desc.equals("()V") ) {
            hasDefaultContructor = true;
        }
        return mv;
    }

    public synchronized boolean isInstrumentable() {
        return this.isInstrumentable;
    }
    public synchronized boolean isInstrumented() {
        return this.isInstrumented;
    }
    public synchronized boolean hasDefaultContructor() {
        return this.hasDefaultContructor;
    }

    public List<String> getIgnoredFields() {
        return this.ignoredFields;
    }
}
