package net.odbogm.agent;

/**
 *
 * @author jbertinetti
 */
public class OdbogmAgentInitializationException extends RuntimeException {
    
    public OdbogmAgentInitializationException(Throwable cause) {
        super("Error initializing ODBOGM Agent", cause);
    }
    
}
