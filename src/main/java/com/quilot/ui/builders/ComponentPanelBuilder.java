package com.quilot.ui.builders;

import javax.swing.*;

/**
 * Defines a contract for classes responsible for building a specific JPanel component.
 * This adheres to the Builder design pattern and Single Responsibility Principle.
 */
public interface ComponentPanelBuilder {
    /**
     * Builds and returns the JPanel component.
     * @return The constructed JPanel.
     */
    JPanel build();
}
