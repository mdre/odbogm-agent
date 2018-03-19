/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Test.asm;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Marcelo D. Ré {@literal <marcelo.re@gmail.com>}
 */
public class AbstExt extends AbstClass {
    private final static Logger LOGGER = Logger.getLogger(AbstExt.class .getName());
    static {
        LOGGER.setLevel(Level.INFO);
    }

    int i = 0;
    
    public AbstExt() {
    }
    
    public int getI() {
        return this.i;
    }
}
