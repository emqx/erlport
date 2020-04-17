
package io.emqx;

import erlport.terms.*;

public class Main {

    public static Object init() {
        System.err.printf("Initiate driver...\n");
        Tuple opts = new Tuple(2);
        opts.set(0, 0);

        // {0 | 1, [{HookName, CallModule, CallFunction, Opts}]}
        //
        EList actions = new EList();

        Tuple connectedAction = new Tuple(4);
        connectedAction.set(0, "on_client_connected");
        connectedAction.set(1, "main");
        connectedAction.set(2, "on_client_connected");
        connectedAction.set(3, new EList());

        actions.add(connectedAction);

        opts.set(1, actions);
        return opts;
    }

    public static void on_client_connected(Object clientInfo, Object state) {
        System.err.printf("on_client_connected: clientinfo: %s, state: %s\n", clientInfo, state);
    }
}
