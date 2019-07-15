package net.odbogm.agent;

import java.util.logging.Level;

/**
 * Configuración de los loggers de cada clase.
 * @author Marcelo D. Ré {@literal <marcelo.re@gmail.com>}
 */
public class LogginProperties {

    public static Level TransparentDirtyDetectorAdapter            = Level.FINER;
    public static Level TransparentDirtyDetectorInstrumentator     = Level.FINER;
    public static Level TransparentDirtyDetectorAgent              = Level.FINER;
    public static Level InstrumentableClassDetector                = Level.FINER;
    public static Level WriteAccessActivatorAdapter                = Level.FINER;
    public static Level WriteAccessActivatorInnerClassAdapter      = Level.FINER;
    
}
