package csense.idea.kotlin.test.settings;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class SettingsPaneUi {
    @NotNull
    private JCheckBox generateAssertionsCheckBox;
    @NotNull
    private JComboBox<String> assertionTypeComboBox;
    @NotNull
    private JPanel root;

    @NotNull
    public JCheckBox getGenerateAssertionsCheckBox() {
        return generateAssertionsCheckBox;
    }

    @NotNull
    public JComboBox<String> getAssertionTypeComboBox() {
        return assertionTypeComboBox;
    }

    public boolean didChange = false;

    public SettingsPaneUi() {
        generateAssertionsCheckBox.setSelected(SettingsContainer.INSTANCE.getShouldGenerateAssertStatements());
        generateAssertionsCheckBox.setAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                didChange = true;
            }
        });
        assertionTypeComboBox.addItem("Csense kotlin test");
    }

    public void store() {
        SettingsContainer.INSTANCE.setShouldGenerateAssertStatements(generateAssertionsCheckBox.isSelected());
    }

    @NotNull
    public JPanel getRoot() {
        return root;
    }

}
