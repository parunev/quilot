package com.quilot.ui.history;

import com.quilot.db.dao.InterviewDao;
import com.quilot.db.model.Interview;
import com.quilot.audio.ouput.AudioOutputService;
import com.quilot.utils.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * A dialog to display a list of all past interviews from the database.
 */
public class InterviewHistoryDialog extends JDialog {

    private final InterviewDao interviewDao;
    private final AudioOutputService audioOutputService;
    private final JList<Interview> interviewList;

    public InterviewHistoryDialog(Frame owner, InterviewDao interviewDao, AudioOutputService audioOutputService) {
        super(owner, "Interview History", true);
        this.interviewDao = interviewDao;
        this.audioOutputService = audioOutputService;
        this.interviewList = new JList<>();

        initComponents();
        loadInterviews();
    }

    private void initComponents() {
        setSize(500, 600);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout(10, 10));

        interviewList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        interviewList.setCellRenderer(new InterviewListCellRenderer());
        interviewList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    viewSelectedInterview();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(interviewList);
        JPanel buttonPanel = getJPanel();

        add(new JLabel("Double-click an interview to view details."), BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel getJPanel() {
        JButton viewButton = new JButton("View Selected");
        JButton deleteButton = new JButton("Delete Selected");
        JButton closeButton = new JButton("Close");

        viewButton.addActionListener(_ -> viewSelectedInterview());
        deleteButton.addActionListener(_ -> deleteSelectedInterview());
        closeButton.addActionListener(_ -> dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(deleteButton);
        buttonPanel.add(viewButton);
        buttonPanel.add(closeButton);
        return buttonPanel;
    }

    private void loadInterviews() {
        try {
            List<Interview> interviews = interviewDao.getAllInterviews();
            DefaultListModel<Interview> model = new DefaultListModel<>();
            interviews.forEach(model::addElement);
            interviewList.setModel(model);
        } catch (SQLException e) {
            Logger.error("Failed to load interview history.", e);
            JOptionPane.showMessageDialog(this, "Could not load interview history:\n" + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewSelectedInterview() {
        Interview selected = interviewList.getSelectedValue();
        if (selected != null) {
            InterviewReviewDialog reviewDialog = new InterviewReviewDialog(this, selected, interviewDao, audioOutputService);
            reviewDialog.setVisible(true);
        }
    }

    /**
     * Deletes the currently selected interview after user confirmation.
     */
    private void deleteSelectedInterview() {
        Interview selected = interviewList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select an interview to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int response = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to permanently delete this interview?\n\"" + selected.getTitle() + "\"",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (response == JOptionPane.YES_OPTION) {
            try {
                interviewDao.deleteInterview(selected.getId());
                ((DefaultListModel<Interview>) interviewList.getModel()).removeElement(selected);
                JOptionPane.showMessageDialog(this, "Interview deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException e) {
                Logger.error("Failed to delete interview with ID: " + selected.getId(), e);
                JOptionPane.showMessageDialog(this, "Could not delete interview:\n" + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Custom renderer to display interview title and date in the JList.
     */
    private static class InterviewListCellRenderer extends DefaultListCellRenderer {
        private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd 'at' HH:mm");

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Interview interview) {
                String date = interview.getInterviewDate().format(FORMATTER);
                setText(String.format("<html><b>%s</b><br><small>%s</small></html>", interview.getTitle(), date));
            }
            return this;
        }
    }
}
