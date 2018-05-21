/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.odbogm.agent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.odbogm.agent.LogginProperties;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 *
 * @author Marcelo D. Ré {@literal <marcelo.re@gmail.com>}
 */
public class TransparentDirtyDetectorInstrumentator implements ClassFileTransformer, ITransparentDirtyDetectorDef {

    private final static Logger LOGGER = Logger.getLogger(TransparentDirtyDetectorInstrumentator.class.getName());

    static {
        if (LOGGER.getLevel() == null) {
            LOGGER.setLevel(LogginProperties.TransparentDirtyDetectorInstrumentator);
        } else {

            System.out.println("TDDI: " + LOGGER.getLevel());
        }
    }
//    public TransparentDirtyDetectorInstrumentator() {
//        this.pkgs = TransparentDirtyDetectorAgent.pkgs;
//    }

    /**
     * Instrumentador
     */
    public TransparentDirtyDetectorInstrumentator() {
    }

    /**
     * Implementación del Agente
     *
     * @param loader classloader
     * @param className nombre de la clase
     * @param classBeingRedefined clase
     * @param protectionDomain poterctionDomain
     * @param classfileBuffer buffer de datos con la clases a redefinir
     * @return byte[] con la clase redefinida
     * @throws IllegalClassFormatException ex
     */
    @Override
    public synchronized byte[] transform(ClassLoader loader, String className, Class classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer)
            throws IllegalClassFormatException {

        LOGGER.log(Level.FINEST, "analizando clase: {0}...", className);

//        if (isInstrumentable(className)) {
        // forzar la recarga
//            clazz.getName().replace(".", "/")
        ClassReader cr = new ClassReader(classfileBuffer);
        if (isInterface(cr)) {
            // No procesar las interfaces
            LOGGER.log(Level.FINEST, "Interface detectada {0}. NO PROCESAR!", className);
            return classfileBuffer;
        }
        ClassWriter cw = new ClassWriter(cr, 0);
        InstrumentableClassDetector icd = new InstrumentableClassDetector(cw);
        cr.accept(icd, 0);

        LOGGER.log(Level.FINEST, "isInstrumentable: "+icd.isInstrumentable());
        if (icd.isInstrumentable() && !icd.isInstrumented()) {
            LOGGER.log(Level.FINER, ""
                    + "\n****************************************************************************"
                    + "\nRedefiniendo on-the-fly {0}..."
                    + "\n****************************************************************************", className);
            ClassReader crRedefine = new ClassReader(classfileBuffer);

            cw = new ClassWriter(crRedefine, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS) {
                // Asegurar que se usa el mismo CL para cargar las clases.
                @Override
                protected String getCommonSuperClass(String type1, String type2) {
                    LOGGER.log(Level.INFO, "type1: " + type1 + "   - type2: " + type2);
                    Class<?> c, d;
                    try {
                        c = Class.forName(type1.replace('/', '.'), false, loader);
                        d = Class.forName(type2.replace('/', '.'), false, loader);
                    } catch (Exception e) {
                        throw new RuntimeException(e.toString());
                    }
                    if (c.isAssignableFrom(d)) {
                        return type1;
                    }
                    if (d.isAssignableFrom(c)) {
                        return type2;
                    }
                    if (c.isInterface() || d.isInterface()) {
                        return "java/lang/Object";
                    } else {
                        do {
                            c = c.getSuperclass();
                        } while (!c.isAssignableFrom(d));
                        return c.getName().replace('.', '/');
                    }

//                return super.getCommonSuperClass(type1, type2);
            }
                
        };
        TransparentDirtyDetectorAdapter taa = new TransparentDirtyDetectorAdapter(cw);
        try {
            crRedefine.accept(taa, ClassReader.EXPAND_FRAMES);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "ERROR <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
            e.printStackTrace();
        }
        // instrumentar el método ___getDirty()
        LOGGER.log(Level.FINER, "insertando el método ___isDirty() ...");
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, ISDIRTY, "()Z", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, className, DIRTYMARK, "Z");
        mv.visitInsn(Opcodes.IRETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();

        // instrumentar el método ___setDirty()
        LOGGER.log(Level.FINER, "insertando el método ___setDirty() ...");
        mv = cw.visitMethod(Opcodes.ACC_PUBLIC, SETDIRTY, "(Z)V", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ILOAD, 1);
        mv.visitFieldInsn(Opcodes.PUTFIELD, className, DIRTYMARK, "Z");
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
        
        // detectar si tiene el contructor por defecto y en caso de no tenerlo insertar uno.
        if (!icd.hasDefaultContructor()) {
            LOGGER.log(Level.FINER, "No se ha encontrado el contructor por defecto. Insertando uno...");
            mv=cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(1,1);
            mv.visitEnd();
        } else {
            LOGGER.log(Level.FINER, "Se ha encontrado el contructor por defecto. ");
        }
        
        if (LogginProperties.TransparentDirtyDetectorInstrumentator == Level.FINER) {
            writeToFile(className, cw.toByteArray());
        }
        LOGGER.log(Level.FINER, "FIN instrumentación {0}"
                + "\n^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^"
                + "\n****************************************************************************",
                className
        );
        return cw.toByteArray();
    }
    return classfileBuffer ;
}

/**
 * Herramienta para realizar un volcado de la clase a disco.
 *
 * @param className nombre del archivo a graba
 * @param myByteArray datos de la clase.
 */
private void writeToFile(String className, byte[] myByteArray) {
        try {
            File theDir = new File("/tmp/asm");
            if (!theDir.exists()) {
                theDir.mkdir();
            }

            FileOutputStream fos = new FileOutputStream("/tmp/asm/" + className.substring(className.lastIndexOf("/")) + ".class");
            fos.write(myByteArray);
            fos.close();
        

} catch (IOException ex) {
            Logger.getLogger(TransparentDirtyDetectorInstrumentator.class
.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public synchronized boolean isInterface(ClassReader cr) {
        return ((cr.getAccess() & 0x200) != 0);
    }
}
