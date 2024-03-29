package net.odbogm.agent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.CheckClassAdapter;

/**
 *
 * @author Marcelo D. Ré {@literal <marcelo.re@gmail.com>}
 */
public class TransparentDirtyDetectorInstrumentator
        implements ClassFileTransformer, ITransparentDirtyDetectorDef {

    private final static Logger LOGGER = Logger.getLogger(TransparentDirtyDetectorInstrumentator.class.getName());

    static {
        if (LOGGER.getLevel() == null) {
            LOGGER.setLevel(LogginProperties.TransparentDirtyDetectorInstrumentator);
        } else {
            System.out.println("TDDI: " + LOGGER.getLevel());
        }
    }

    /**
     * Instrumentador.
     */
    public TransparentDirtyDetectorInstrumentator() {
    }

    /**
     * Implementación del Agente.
     *
     * Si se establece el nivel FINER para esta clase, se realiza un volcado a disco de las clases intervenidas en la carpeta /tmp/asm.
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

        LOGGER.log(Level.FINEST, "\n\nanalizando clase: {0}...", className);

        if (className.startsWith("test")) {
            LOGGER.log(Level.FINER, "\n\n >>>>>>>>>>>>>>>>>>> analizando clase: {0}...", className);
        }

        ClassReader cr = new ClassReader(classfileBuffer);
        if (isInterface(cr)) {
            // No procesar las interfaces
            LOGGER.log(Level.FINEST, "Interface detectada {0}. NO PROCESAR!", className);
            return classfileBuffer;
        }

        // determinar si se trata de una inner class o una clase base
        if (!className.substring(className.lastIndexOf("/") + 1).contains("$")) {
            // si no contienen el signo $ significa que es una clase base
            // y la procesamos normalmente

            ClassWriter cw = new ClassWriter(cr, 0);
            InstrumentableClassDetector icd = new InstrumentableClassDetector(cw);
            cr.accept(icd, 0);

            LOGGER.log(Level.FINEST, "isInstrumentable: " + icd.isInstrumentable());
            if (icd.isInstrumentable() && !icd.isInstrumented()) {
                LOGGER.log(Level.FINER, ""
                        + "\n****************************************************************************"
                        + "\nRedefiniendo on-the-fly {0}..."
                        + "\n****************************************************************************",
                        className);
                ClassReader crRedefine = new ClassReader(classfileBuffer);

                cw = new ClassWriter(crRedefine, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS) {
                    // Asegurar que se usa el mismo CL para cargar las clases.
                    @Override
                    protected String getCommonSuperClass(String type1, String type2) {
                        LOGGER.log(Level.FINER, "type1: " + type1 + "   - type2: " + type2);
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
                    }
                };
                TransparentDirtyDetectorAdapter taa = new TransparentDirtyDetectorAdapter(cw, icd.getIgnoredFields());
                try {
                    crRedefine.accept(taa, ClassReader.SKIP_FRAMES);
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
                    mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
                    mv.visitCode();
                    mv.visitVarInsn(Opcodes.ALOAD, 0);
                    mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
                    mv.visitInsn(Opcodes.RETURN);
                    mv.visitMaxs(1, 1);
                    mv.visitEnd();
                } else {
                    LOGGER.log(Level.FINER, "Se ha encontrado el contructor por defecto. ");
                }

                if (LOGGER.getLevel() == Level.FINER) {
                    writeToFile(className, cw.toByteArray());
                }
                LOGGER.log(Level.FINER, "FIN instrumentación {0}"
                        + "\n^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^"
                        + "\n****************************************************************************",
                        className
                );
                return cw.toByteArray();
            }
        } else {
            // si contiene el $, significa que es una innerClass. 
            // debermos revisar si corresponde intrumentarla visitando la clase principal
            String outerClass = className.substring(0, className.lastIndexOf("$")).replace("/", ".");
            
            try {
                LOGGER.log(Level.FINER, "analizando innerClass: " + className);
                ClassReader ocr = new ClassReader(outerClass);
                ClassWriter ocw = new ClassWriter(ocr, 0);
                InstrumentableClassDetector oicd = new InstrumentableClassDetector(ocw);
                ocr.accept(oicd, 0);
                if (oicd.isInstrumentable() && !oicd.isInstrumented()) {
                    LOGGER.log(Level.FINER, "Se encontró la anotattion Entity en la clase contenedora. Instrumentar la innerClass");

                    // FIXME: esto debería sacarse a una clase aparte o a un método, para que quede mas prolijo. 
                    LOGGER.log(Level.FINER, ""
                            + "\n****************************************************************************"
                            + "\nRedefiniendo innerClass on-the-fly {0}..."
                            + "\n****************************************************************************",
                            className);
                    ClassReader crRedefine = new ClassReader(classfileBuffer);

                    ClassWriter cw = new ClassWriter(crRedefine, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS) {
                        // Asegurar que se usa el mismo CL para cargar las clases.
                        @Override
                        protected String getCommonSuperClass(String type1, String type2) {
                            LOGGER.log(Level.FINER, "type1: " + type1 + "   - type2: " + type2);
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
                        }
                    };
                    
                    // activar el chequeo de la clase
                    TransparentDirtyDetectorInnerClassAdapter taa = new TransparentDirtyDetectorInnerClassAdapter(new CheckClassAdapter(cw,true), className, oicd.getIgnoredFields());
                    try {
                        crRedefine.accept(taa, ClassReader.EXPAND_FRAMES);
                        
                        if (LOGGER.getLevel() == Level.FINER) {
                            writeToFile(className, cw.toByteArray());
                        }
                        LOGGER.log(Level.FINER, "FIN instrumentación {0}"
                                + "\n^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^"
                                + "\n****************************************************************************",
                                className
                        );
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "ERROR <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
                        e.printStackTrace();
                    }
                    // descomentar estas líneas junto con la de arriba para realizar un checkeo de la clase.
//                    System.out.println("****************************************************************************");
//                    System.out.println("****************************************************************************");
//                    System.out.println("****************************************************************************\n");
//                    StringWriter stringWriter = new StringWriter();
//                    PrintWriter printWriter = new PrintWriter(stringWriter);
//                    CheckClassAdapter.verify(new ClassReader(cw.toByteArray()), false, printWriter);
//                    System.out.println("\n****************************************************************************");
//                    System.out.println("****************************************************************************");
//                    System.out.println("****************************************************************************");
                    return cw.toByteArray();

                }
            } catch (IOException ex) {
                LOGGER.log(Level.FINER, "No se ha podido analizar la clase inner "+className);
            }
        }
        return classfileBuffer;
    }
    
    /**
     * Herramienta para realizar un volcado de la clase a disco.
     *
     * @param className nombre del archivo a graba
     * @param myByteArray datos de la clase.
     */
    private void writeToFile(String className, byte[] myByteArray) {
        LOGGER.log(Level.FINER, "Escribiendo archivo a disco en /tmp/asm");
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

    /**
     * Determina si se trata de una interface o una clase.
     *
     * @param cr clase a analizar
     * @return true si es una interface
     */
    public synchronized boolean isInterface(ClassReader cr) {
        return ((cr.getAccess() & 0x200) != 0);
    }
}
