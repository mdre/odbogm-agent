package net.odbogm.agent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 *
 * @author Marcelo D. Ré {@literal <marcelo.re@gmail.com>}
 */
public class TransparentDirtyDetectorAdapter extends ClassVisitor implements ITransparentDirtyDetectorDef {

    private final static Logger LOGGER = Logger.getLogger(TransparentDirtyDetectorAdapter.class.getName());
    static {
        if (LOGGER.getLevel() == null) {
            LOGGER.setLevel(LogginProperties.TransparentDirtyDetectorAdapter);
        }
    }
    
    private boolean isFieldPresent = false;
    private List<String> ignoredFields = new ArrayList();

    public TransparentDirtyDetectorAdapter(ClassVisitor cv, List<String> ignoredFields) {
        super(Opcodes.ASM9, cv);
        this.ignoredFields = ignoredFields;
    }

    @Override
    public void visit(int version, int access, String name, String signature,
            String superName, String[] interfaces) {
        String[] addInterfaces = Arrays.copyOf(interfaces, interfaces.length + 1); //create new array from old array and allocate one more element
        addInterfaces[addInterfaces.length - 1] = ITransparentDirtyDetector.class.getName().replace(".", "/");
        LOGGER.log(Level.FINER, "visitando clase: {0} super: {1} y agregando la interface.",
                new Object[]{name, superName});
        // se elimina la propiedad FINAL de todas las clases visitadas para que 
        // CGLIB pueda extenderlas.
        LOGGER.log(Level.FINEST, ((access & Opcodes.ACC_FINAL) > 0)?"Clase FINAL detectada":"");

        cv.visit(version, access & (~Opcodes.ACC_FINAL) , name, signature, superName, addInterfaces);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        if (name.equals(DIRTYMARK)) {
            isFieldPresent = true;
            LOGGER.log(Level.FINER, "El campo ya existe!!!! WARNING!!! Esto no deberia ocurrir!!! ************************");
        }
        
        return cv.visitField(access, name, desc, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv;
        LOGGER.log(Level.FINEST, "visitando método: {0} signature: {1}", new Object[]{name, signature});
        //se quitan todos los FINAL de los métodos
        mv = cv.visitMethod(access & (~Opcodes.ACC_FINAL), name, desc, signature, exceptions);
        if ((mv != null) && !name.equals("<init>") && !name.equals("<clinit>")) {
            LOGGER.log(Level.FINER, ">>>>>>>>>>> Instrumentando método: {0}", name);
            mv = new WriteAccessActivatorAdapter(mv, ignoredFields);
            LOGGER.log(Level.FINEST, "fin instrumentación ---------------------------------------------------");
        } else {
            LOGGER.log(Level.FINEST, "mv = NULL !!!! <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        }
        return mv;
    }

    @Override
    public void visitEnd() {
        if (!isFieldPresent) {
            LOGGER.log(Level.FINER, "Agregando el campo");
            FieldVisitor fv = cv.visitField(Opcodes.ACC_PUBLIC, DIRTYMARK,
                    org.objectweb.asm.Type.BOOLEAN_TYPE.getDescriptor(), null, null);
            if (fv != null) {
                fv.visitEnd();
                LOGGER.log(Level.FINER, "fv.visitEnd..");
            }
        }
        cv.visitEnd();
    }

}
