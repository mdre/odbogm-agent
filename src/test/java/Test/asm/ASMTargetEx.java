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
 * 
 */
public class ASMTargetEx extends ASMTarget implements IASMTest {

    private float f = 0.1f;
    private ASMTarget asmt;
    
    public ASMTargetEx() {
        super();
    }
    
    public void init() {
        this.f = 0.2f;
        this.asmt = new ASMTarget();
    }
    
    private void set2() {
        this.f = 2.0f;
    }
    
    private void set3() {
        this.f = 2.0f;
    }

    @Override
    public void noImplementar() {
        System.out.println("no implementar");
    }
}
