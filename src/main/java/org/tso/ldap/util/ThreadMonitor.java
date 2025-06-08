package org.tso.ldap.util;

import org.gnome.glib.GLib;
import org.gnome.gtk.ProgressBar;

public class ThreadMonitor {

    @FunctionalInterface
    public interface CompletionCallback {

        void onCompletion();

    }

    Thread thread;
    ProgressBar progressBar;
    CompletionCallback callback;

    public ThreadMonitor(Runnable runnable, ProgressBar progressBar) {

        this.thread = new Thread(runnable);
        this.progressBar = progressBar;
    }

    public void process(CompletionCallback callback) {
        thread.start();

        this.progressBar.setVisible(true);
        this.progressBar.setFraction(0);

        GLib.timeoutAdd(GLib.PRIORITY_DEFAULT, 100, () -> {

            if (!thread.isAlive()) {
                progressBar.setVisible(true);
                callback.onCompletion();

            } else {
                progressBar.pulse();
            }

            return thread.isAlive();

        });

    }

     public void process() {

        this.process(()-> {
        });

     }
}
