package com.github.konovalovmaksim.gp.scraper.gui.custom;

import javafx.scene.control.RadioButton;

public class NamedRadioButton extends RadioButton {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
