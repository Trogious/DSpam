package net.swmud.trog.dspam.core;

import java.util.concurrent.Executor;

public class BackgroundExecutor implements Executor {
    @Override
    public void execute(Runnable command) {
        new Thread(command).start();
    }
}
