package com.mqttinsight.ui.log;

import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author ptma
 */
public class StdHook {

    private static final PrintStream STD_OUT = System.out;
    private static final PrintStream STD_ERR = System.err;
    private static final AtomicBoolean HOOKED = new AtomicBoolean(false);

    public static void hook(LogTab logTab) {
        PrintStream psOut = new PrintStream(new StdStream(logTab, System.out), true);
        System.setOut(psOut);
        PrintStream psErr = new PrintStream(new StdStream(logTab, System.err), true);
        System.setErr(psErr);
        HOOKED.set(true);
    }

    public static void reset() {
        System.setOut(STD_OUT);
        System.setErr(STD_ERR);
        HOOKED.set(false);
    }

    public static boolean isHooked() {
        return HOOKED.get();
    }

}
