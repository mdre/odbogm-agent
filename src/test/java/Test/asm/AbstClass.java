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
public class AbstClass {
    private final static Logger LOGGER = Logger.getLogger(AbstClass.class .getName());
    static {
        LOGGER.setLevel(Level.INFO);
    }
    
    String s;

    public AbstClass() {
    }
    
    public void setS(String ss) {
        this.s = ss;
    }
}
