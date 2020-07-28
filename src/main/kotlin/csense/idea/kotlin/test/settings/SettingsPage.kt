package csense.idea.kotlin.test.settings

import com.intellij.openapi.options.*
import csense.kotlin.extensions.*
import javax.swing.*

class SettingsPage : SearchableConfigurable {
    private var ui: SettingsPaneUi? = null
    
    override fun isModified(): Boolean {
        return ui?.didChange ?: false
    }
    
    override fun getId(): String {
        return "csenseKotlinCheckedExceptionsSettingsPage"
    }
    override fun getDisplayName(): String {
        return "Csense - Kotlin Checked exceptions"
    }
    
    override fun apply() {
        ui?.store()
    }
    
    override fun createComponent(): JComponent? {
        return tryAndLog {
            ui = SettingsPaneUi()
            ui?.root
        }
    }
    
    override fun disposeUIResources() {
        ui = null
    }
}