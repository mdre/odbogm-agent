/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.odbogm.agent;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.odbogm.agent.LogginProperties;
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
    
    private boolean isInstrumetable = false;
    private boolean isInstrumented = false;
    private boolean hasDefaultContructor = false;
    private String clazzName = null;
    
    public InstrumentableClassDetector(ClassVisitor cv) {
        super(Opcodes.ASM4, cv);
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
        LOGGER.log(Level.FINEST, "Annotation: >"+ann+"<");
        if (ann.startsWith("Lnet/odbogm/annotations/Entity")) {
            LOGGER.log(Level.FINER, clazzName + ": Annotation: >"+ann+"<");
            LOGGER.log(Level.FINER, ">>>>>>>>>>> marcar como instrumentable");
            this.isInstrumetable = true;
        }
        return super.visitAnnotation(ann, bln); 
    }

    @Override
    public synchronized MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv;
        LOGGER.log(Level.FINEST, "visitando método: " + name + " signature: "+signature);
        mv = cv.visitMethod(access, name, desc, signature, exceptions);
        if ((mv != null) && name.equals("<init>") && signature == null ) {
            hasDefaultContructor = true;
        }
        return mv;
    }
    
    public synchronized boolean isInstrumentable() {
        return this.isInstrumetable;
    }
    public synchronized boolean isInstrumented() {
        return this.isInstrumented;
    }
    public synchronized boolean hasDefaultContructor() {
        return this.hasDefaultContructor;
    }
}
