/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Test.asm;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.odbogm.agent.ITransparentDirtyDetector;

/**
 *
 * @author Marcelo D. Ré {@literal <marcelo.re@gmail.com>}
 */
public class ASMTargetExInstrumented extends ASMTargetInstrumented implements ITransparentDirtyDetector {

    private final static Logger LOGGER = Logger.getLogger(ASMTargetExInstrumented.class.getName());

    static {
        LOGGER.setLevel(Level.INFO);
    }
    public boolean ___dirty = false;

    @Override
    public boolean ___ogm___isDirty() {
        return this.___dirty;
    }

    @Override
    public void ___ogm___setDirty(boolean b) {
        if (super.getClass().isAssignableFrom(ITransparentDirtyDetector.class)) {
            super.___ogm___setDirty(b);
        }
        ___dirty = b;
    }
}
