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
    private JList<CoverageListData> missingClassesList;
    private JList<CoverageListData> missingPropertiesList;
    private JList<CoverageListData> missingFunctionsList;

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

    public JList<CoverageListData> getMissingClassesList() {
        return missingClassesList;
    }

    public JList<CoverageListData> getMissingPropertiesList() {
        return missingPropertiesList;
    }

    public JList<CoverageListData> getMissingFunctionsList() {
        return missingFunctionsList;
    }
}
