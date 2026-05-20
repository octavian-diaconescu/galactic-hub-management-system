package com.octavian.galactic.ui;

/** Controllers that need to refresh the shell (bay table + current panel). */
public interface MainWindowAware {
    void setMainWindow(Object mainWindowController);
}
