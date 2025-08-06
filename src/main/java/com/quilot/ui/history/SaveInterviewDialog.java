package com.quilot.ui.history;

import lombok.Getter;

import javax.swing.*;
import java.awt.*;

/**
 * A modal dialog that prompts the user to name their interview session after recording.
 */
@Getter
public class SaveInterviewDialog extends JDialog {

    private final JTextField titleField;
    private String interviewTitle;

    /**
     * Constructs the dialog.
     * @param owner The parent frame.
     * @param defaultTitle The pre-populated title for the interview.
     */
    public SaveInterviewDialog(Frame owner, String defaultTitle) {
        super(owner, "Save Interview", true);
        this.interviewTitle = defaultTitle;

        setLayout(new BorderLayout(10, 10));
        titleField = new JTextField(defaultTitle, 30);
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(_ -> onSave());
        cancelButton.addActionListener(_ -> dispose());

        JPanel textPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        textPanel.add(new JLabel("Interview Title:"));
        textPanel.add(titleField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        add(textPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    /**
     * Handles the save action, updating the internal title and closing the dialog.
     */
    private void onSave() {
        String newTitle = titleField.getText().trim();
        if (!newTitle.isEmpty()) {
            this.interviewTitle = newTitle;
        }
        dispose();
    }
}