package csense.idea.kotlin.test.bll.testGeneration

import org.intellij.lang.annotations.Language
import org.jetbrains.kotlin.psi.KtNamedFunction


fun KtNamedFunction.createComplexTests(testName: String): String {
    val cases = createTestCodeCases()
    val functions = cases.map {
        ComplexTestCreation.createFunctionCode(it.name, it.statements)
    }
    return ComplexTestCreation.createClassCode(testName, functions)
}

data class ComplexTestCodeCase(val name: String, val statements: List<String>)

private fun KtNamedFunction.createTestCodeCases(): List<ComplexTestCodeCase> {

    return listOf()
}

private fun KtNamedFunction.createTestStatements(): List<String> {
    return listOf()
}

object ComplexTestCreation {


    fun createFunctionCode(
        functionName: String,
        functionCode: List<String>
    ): String {
        @Suppress("UnnecessaryVariable")
        @Language("kotlin")
        val code = """
        @Test
        fun ${functionName.safeDecapitizedFunctionName()}(){
            ${functionCode.joinToString("\n")}
        }
        """.trimIndent()
        return code
    }

    fun createClassCode(
        className: String,
        functions: List<String>
    ): String {
        @Suppress("UnnecessaryVariable")
        @Language("kotlin")
        val code = """
        class ${className.capitalize()}{
            ${functions.joinToString("\n")}
        }
        """.trimIndent()
        return code
    }
}
