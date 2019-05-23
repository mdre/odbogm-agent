package net.odbogm.agent;

import com.ea.agentloader.AgentLoader;
import java.lang.instrument.Instrumentation;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.URISyntaxException;

/**
 *
 * @author Marcelo D. Ré {@literal <marcelo.re@gmail.com>}
 */
public class TransparentDirtyDetectorAgent {

    private final static Logger LOGGER = Logger.getLogger(TransparentDirtyDetectorAgent.class.getName());
    static {
        if (LOGGER.getLevel() == null) {
            LOGGER.setLevel(LogginProperties.TransparentDirtyDetectorAgent);
        }
    }

    private static Instrumentation instrumentation;

    
    /**
     * Agente para manipulación de las clases.
     */
    public TransparentDirtyDetectorAgent() {
    }

    /**
     * JVM hook to statically load the javaagent at startup.
     *
     * After the Java Virtual Machine (JVM) has initialized, the premain method 
     * will be called. Then the real application main method will be called.
     *
     * @param args args
     * @param inst inst
     * throws Exception ex
     */
    public static void premain(String args, Instrumentation inst)  {
        LOGGER.log(Level.INFO, "");
        LOGGER.log(Level.INFO, "===============================================");
        LOGGER.log(Level.INFO, "Transparent Dirty Detector Agent is loading... ");
        LOGGER.log(Level.INFO, "===============================================");
        LOGGER.log(Level.FINER, "premain method invoked with args: {0} and inst: {1}", new Object[]{args, inst});
        LOGGER.log(Level.INFO, "");
        instrumentation = inst;
        instrumentation.addTransformer(new TransparentDirtyDetectorInstrumentator());
    }

    /**
     * JVM hook to dynamically load javaagent at runtime.
     *
     * The agent class may have an agentmain method for use when the agent is started after VM startup.
     *
     * @param args args
     * @param inst inst
     * throws Exception ex
     */
    public static void agentmain(String args, Instrumentation inst)  {
        LOGGER.log(Level.INFO, "");
        LOGGER.log(Level.INFO, "===============================================");
        LOGGER.log(Level.INFO, "Transparent Dirty Detector Agent is loading... ");
        LOGGER.log(Level.INFO, "===============================================");
        LOGGER.log(Level.FINER, "agentmain method invoked with args: {0} and inst: {1}", new Object[]{args, inst});
        LOGGER.log(Level.INFO, "");
        instrumentation = inst;
        instrumentation.addTransformer(new TransparentDirtyDetectorInstrumentator());
    }

    /**
     * Programmatic hook to dynamically load javaagent at runtime.
     * It could be load with JVM parameters.
     * Ej:
     * -javaagent:/path-to-glassfish/domains/domain1/lib/ext/odbogm-agent-x.x.x.jar
     */
    public static void initialize() {
        if (instrumentation == null) {
            LOGGER.log(Level.INFO, "Dynamically loading java agent...");
            try {
                String pathToAgent = TransparentDirtyDetectorAgent.class
                        .getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
                LOGGER.log(Level.INFO, "path: {0}", pathToAgent);
                if (pathToAgent.endsWith(".jar")) {
                    AgentLoader.loadAgent(pathToAgent, null);
                } else {
                    AgentLoader.loadAgentClass(TransparentDirtyDetectorAgent.class.getName(),
                            null, null, true, true, true);
                }
            } catch (URISyntaxException ex) {
                LOGGER.log(Level.SEVERE, "Couldn't load java agent.", ex);
            }
        }
    }

}
