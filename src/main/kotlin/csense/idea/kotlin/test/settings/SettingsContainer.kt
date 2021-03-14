package csense.idea.kotlin.test.settings

import com.intellij.ide.util.*

object SettingsContainer {
    private const val settingsPrefixed = "CsenseKotlinTests"

    private val backend by lazy {
        PropertiesComponent.getInstance()
    }

    private const val shouldGenerateAssertStatementsName = settingsPrefixed + "shouldGenerateAssertStatements"
    var shouldGenerateAssertStatements: Boolean
        get() = backend.getBoolean(shouldGenerateAssertStatementsName, true)
        set(value) = backend.setValue(shouldGenerateAssertStatementsName, value, true)

    private const val generateAssertStatementOfNameName = settingsPrefixed + "generateAssertStatementOfName"
    var generateAssertStatementOfName: String
        get() = backend.getValue(generateAssertStatementOfNameName, "csense")
        set(value) = backend.setValue(generateAssertStatementOfNameName, value, "csense")
}