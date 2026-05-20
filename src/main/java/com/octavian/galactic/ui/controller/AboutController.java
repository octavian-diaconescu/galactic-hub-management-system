package com.octavian.galactic.ui.controller;

import com.octavian.galactic.ui.AppContext;
import com.octavian.galactic.ui.RefreshablePanel;
import javafx.fxml.FXML;

public class AboutController implements RefreshablePanel {

    @FXML
    private void initialize() {
        refresh();
    }

    @Override
    public void refresh() {
        AppContext.setStatus("Help loaded");
    }
}
