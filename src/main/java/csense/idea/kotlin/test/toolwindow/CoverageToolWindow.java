package csense.idea.kotlin.test.toolwindow;

import javax.swing.*;


public class CoverageToolWindow {
    private JButton RefreshButton;
    private JButton ShowMissingButton;
    private JPanel content;
    private JLabel classesTestedLabel;
    private JLabel MethodsTestedLabel;
    private JComboBox<String> SelectedModule;
    private JLabel PropertiesTestedLabel;
    private JTable missingClassesTable;

    public JButton getRefreshButton() {
        return RefreshButton;
    }

    public JButton getShowMissingButton() {
        return ShowMissingButton;
    }

    public JLabel getClassesTestedLabel() {
        return classesTestedLabel;
    }

    public JLabel getMethodsTestedLabel() {
        return MethodsTestedLabel;
    }

    public JPanel getContent() {
        return content;
    }

    public JComboBox<String> getSelectedModule() {
        return SelectedModule;
    }

    public JLabel getPropertiesTestedLabel() {
        return PropertiesTestedLabel;
    }
}
