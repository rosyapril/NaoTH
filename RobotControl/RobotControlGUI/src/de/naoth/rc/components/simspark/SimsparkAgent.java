package de.naoth.rc.components.simspark;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Basic Simspark agent.
 * Just an experiment and to show how to create an agent and communicate with
 * Simspark.
 * 
 * @author Philipp Strobel <philippstrobel@posteo.de>
 */
public class SimsparkAgent extends Simspark
{
    /**
     * Main method of the simspark agent thread.
     */
    public void run() {
        if (socket == null) {
            return;
        }
        // init simspark communication
        sendAgentMessage("(scene rsg/agent/nao/nao.rsg 0)(syn)");
        System.out.println(getServerMessage());
        sendAgentMessage("(init (unum 0)(teamname NaoTH))(syn)");
        System.out.println(getServerMessage());

        System.out.println("listening");

        while (isRunning) {
            try {
                sleep(1);
                // Simspark in "sync" mode ...
                sendAgentMessage("(syn)");
                String msg = getServerMessage();
                if (msg != null) {
                    // TODO: where should the received agent messages go?!
                    System.out.println(msg);
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(SimsparkAgent.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
} // end class SimsparkAgent