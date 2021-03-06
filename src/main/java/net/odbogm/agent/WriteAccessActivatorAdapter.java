package net.odbogm.agent;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 *
 * @author Marcelo D. Ré {@literal <marcelo.re@gmail.com>}
 */
public class WriteAccessActivatorAdapter extends MethodVisitor implements ITransparentDirtyDetectorDef {

    private final static Logger LOGGER = Logger.getLogger(WriteAccessActivatorAdapter.class.getName());
    private boolean activate = false;
    private String owner;

    static {
        if (LOGGER.getLevel() == null) {
            LOGGER.setLevel(LogginProperties.WriteAccessActivatorAdapter);
        }
    }
    
    public WriteAccessActivatorAdapter(MethodVisitor mv) {
        super(Opcodes.ASM7, mv);
    }

    /**
     * Add a call to setDirty in every method that has a PUTFIELD in its code.
     * @param opcode código a analizar
     */
    @Override
    public synchronized void visitInsn(int opcode) {
        LOGGER.log(Level.FINEST, "Activate: {0}", this.activate);
        if ((this.activate)&&((opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) || opcode == Opcodes.ATHROW)) {
            LOGGER.log(Level.FINEST, "Agregando llamada a setDirty...");
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitInsn(Opcodes.ICONST_1);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, owner, SETDIRTY, "(Z)V", false);
            //mv.visitFieldInsn(Opcodes.PUTFIELD, owner, "__ogm__dirtyMark", "Z");
        } 
        mv.visitInsn(opcode);
        LOGGER.log(Level.FINEST, "fin --------------------------------------------------");
    }

    @Override
    public synchronized void visitFieldInsn(int opcode, String owner, String name, String desc) {
        LOGGER.log(Level.FINEST, "owner: {0} - name: {1} - desc: {2}", new Object[]{owner, name, desc});
        if (opcode == Opcodes.PUTFIELD) {
            this.activate = true;
            this.owner = owner;
        } 
        mv.visitFieldInsn(opcode, owner, name, desc); 
        LOGGER.log(Level.FINEST, "fin --------------------------------------------------");
    }

    @Override
    public void visitEnd() {
        LOGGER.log(Level.FINEST, "fin MethodVisitor -------------------------------------");
        super.visitEnd();
    }
    
}

