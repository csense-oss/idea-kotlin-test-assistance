package csense.idea.kotlin.test.bll.testGeneration

import csense.kotlin.extensions.collections.isNotNullOrEmpty
import csense.kotlin.extensions.mapLazy
import org.intellij.lang.annotations.Language
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtTypeReference

fun KtNamedFunction.createSimpleTests(
    testName: String,
    shouldGenerateAssertions: Boolean
): String {
    //we are to create a simple function that will invoke the method with say simple test data.
    val firstParameter = getFirstParameterOrNull()
    val invocationPattern = toFunctionInvocationPattern()
    if (firstParameter != null) {

        return SimpleTestCreation.createTestForSingleArgument(
            testName,
            firstParameter,
            invocationPattern,
            shouldGenerateAssertions
        )
    }

    return SimpleTestCreation.createTestFunctionWithCodeLines(
        testName,
        listOf(),
        AssertionType.None
    )
}


object SimpleTestCreation {

    fun createTestForSingleArgument(
        testName: String,
        type: KtTypeReference,
        invocationPattern: FunctionInvocationPattern?,
        shouldGenerateAssertions: Boolean
    ): String {
        val firstParameterType: String = type.text
        //return decl required for assertions...
        val assertionType = shouldGenerateAssertions.mapLazy(
            { type.toAssertionType() }, { AssertionType.None })
        val testCode = findKnownTestCodeLines(firstParameterType)
        if (testCode.isNotNullOrEmpty()) {

            val testCodeWithCall = testCode.withInvocationPattern(invocationPattern)
            return createTestFunctionWithCodeLines(
                testName,
                testCodeWithCall,
                assertionType
            )
        }
        return ""//TODO
    }

    fun findKnownTestCodeLines(nameOfType: String): List<String>? = when (nameOfType) {

        //region lists
        "List<Byte>" -> byteListExamples
        "List<Short>" -> shortListExamples
        "List<Int>" -> intListExamples
        "List<Long>" -> longListExamples

        "List<Float>" -> floatListExamples
        "List<Double>" -> doubleListExamples

        "List<Boolean>" -> booleanListExamples

        "List<Char>" -> charListExamples
        "List<String>" -> stringListExamples
        //endregion

        //region array
        "Array<Byte>" -> byteArrayExamples
        "Array<Short>" -> shortArrayExamples
        "Array<Int>" -> intArrayExamples
        "Array<Long>" -> longArrayExamples

        "Array<Float>" -> floatArrayExamples
        "Array<Double>" -> doubleArrayExamples

        "Array<Boolean>" -> booleanArrayExamples

        "Array<Char>" -> charArrayExamples
        "Array<String>" -> stringArrayExamples
        //endregion

        //region primitives
        "Byte" -> byteExamples
        "Short" -> shortExamples
        "Int" -> intExamples
        "Long" -> longExamples

        "Float" -> floatExamples
        "Double" -> doubleExamples

        "Char" -> charExamples

        "Boolean" -> boolExamples

        "String" -> stringExamples
        //endregion

        else -> null
    }

    fun createTestFunctionWithCodeLines(
        testName: String,
        testCode: List<String>,
        assertionType: AssertionType
    ): String {
        return createSimpleTestCodeFromTemplate(
            testName,
            testCode.joinToString("\n") {
                it.generateTestAssertions(
                    assertionType
                )
            }
        )
    }

    //region primitive data
    private val stringExamples = listOf(
        "\"\"",
        "\" \"",
        "\"a\"",
        "\"abc\"",
        "\"1234\"",
        "\"Other region 한\"",
        "\"Hi ☺\"",
        "\"�\b\"",
        "\"\\n\"",
        "\"...()[]\""
    )

    private val intExamples = listOf(
        "(-1)",
        "0",
        "1",
        "(-50)",
        "42"
    )

    private val longExamples = listOf(
        "(-1L)",
        "0L",
        "1L",
        "(-50L)",
        "42L"
    )

    private val doubleExamples = listOf(
        "(-1).toDouble()",
        "0.toDouble()",
        "1.toDouble()",
        "(-50).toDouble()",
        "42.toDouble()",
        "0.5",
        "100.956"
    )
    private val floatExamples = listOf(
        "(-1).toFloat()",
        "0.toFloat()",
        "1.toFloat()",
        "(-50).toFloat()",
        "42.toFloat()",
        "0.5f",
        "100.956f"
    )
    private val boolExamples = listOf(
        "false",
        "true"
    )

    private val charExamples = listOf(
        "' '",
        "'a'",
        "'Q'",
        "'1'",
        "'?'",
        "'\b'",
        "'\\n'"
    )
    private val byteExamples = listOf(
        "0.toByte()",
        "(-1).toByte()",
        "1.toByte()",
        "80.toByte()",
        "(-82).toByte()"
    )
    private val shortExamples = listOf(
        "0.toShort()",
        "(-1).toShort()",
        "1.toShort()",
        "80.toShort()",
        "(-82).toShort()"
    )
//endregion

    //region list data
    private val stringListExamples = listOf(
        "listOf<String>()",
        "listOf(\"\")",
        "listOf(\"a\")",
        "listOf(\"a\",\"b\")"
    )

    private val intListExamples = listOf(
        "listOf<Int>()",
        "listOf(0)",
        "listOf(-1)",
        "listOf(1,2)"
    )

    private val charListExamples = listOf(
        "listOf<Char>()",
        "listOf(' ')",
        "listOf('a')",
        "listOf('a','b')"
    )

    private val booleanListExamples = listOf(
        "listOf<Boolean>()",
        "listOf(false)",
        "listOf(true)",
        "listOf(false,true)"
    )

    private val floatListExamples = listOf(
        "listOf<Float>()",
        "listOf(0f)",
        "listOf(1f)",
        "listOf(-1f)",
        "listOf(5f,-5f)"
    )

    private val doubleListExamples = listOf(
        "listOf<Double>()",
        "listOf(0.toDouble())",
        "listOf(5.toDouble())",
        "listOf((-5).toDouble())",
        "listOf(80.toDouble(),(-80).toDouble()))"
    )

    private val longListExamples = listOf(
        "listOf<Long>()",
        "listOf(0L)",
        "listOf(5L)",
        "listOf(-5L)",
        "listOf(80L,-80L))"
    )

    private val byteListExamples = listOf(
        "listOf<Byte>()",
        "listOf(0.toByte())",
        "listOf(5.toByte())",
        "listOf((-5).toByte())",
        "listOf(80.toByte(),(-80).toByte()))"
    )
    private val shortListExamples = listOf(
        "listOf<Short>()",
        "listOf(0.toShort())",
        "listOf(5.toShort())",
        "listOf((-5).toShort())",
        "listOf(80.toShort(),(-80).toShort()))"
    )
//endregion

    //region array data
    private val stringArrayExamples = listOf(
        "arrayOf<String>()",
        "arrayOf(\"\")",
        "arrayOf(\"a\")",
        "arrayOf(\"a\",\"b\")"
    )

    private val intArrayExamples = listOf(
        "arrayOf<Int>()",
        "arrayOf(0)",
        "arrayOf(-1)",
        "arrayOf(1,2)"
    )

    private val charArrayExamples = listOf(
        "arrayOf<Char>()",
        "arrayOf(' ')",
        "arrayOf('a')",
        "arrayOf('a','b')"
    )

    private val booleanArrayExamples = listOf(
        "arrayOf<Boolean>()",
        "arrayOf(false)",
        "arrayOf(true)",
        "arrayOf(false,true)"
    )

    private val floatArrayExamples = listOf(
        "arrayOf<Float>()",
        "arrayOf(0f)",
        "arrayOf(1f)",
        "arrayOf(-1f)",
        "arrayOf(5f,-5f)"
    )

    private val doubleArrayExamples = listOf(
        "arrayOf<Double>()",
        "arrayOf(0.toDouble())",
        "arrayOf(5.toDouble())",
        "arrayOf((-5).toDouble())",
        "arrayOf(80.toDouble(),(-80).toDouble()))"
    )

    private val longArrayExamples = listOf(
        "arrayOf<Long>()",
        "arrayOf(0L)",
        "arrayOf(5L)",
        "arrayOf(-5L)",
        "arrayOf(80L,-80L))"
    )

    private val byteArrayExamples = listOf(
        "arrayOf<Byte>()",
        "arrayOf(0.toByte())",
        "arrayOf(5.toByte())",
        "arrayOf((-5).toByte())",
        "arrayOf(80.toByte(),(-80).toByte()))"
    )
    private val shortArrayExamples = listOf(
        "arrayOf<Short>()",
        "arrayOf(0.toShort())",
        "arrayOf(5.toShort())",
        "arrayOf((-5).toShort())",
        "arrayOf(80.toShort(),(-80).toShort()))"
    )
//endregion


    fun createSimpleTestCodeFromTemplate(
        testName: String,
        testCode: String
    ): String {
        @Suppress("UnnecessaryVariable")
        @Language("kotlin")
        val code = """
        @Test
        fun $testName(){
            $testCode
        }
    """.trimIndent()
        return code
    }


}

fun List<String>.withInvocationPattern(
    invocationPattern: FunctionInvocationPattern?
): List<String> {
    if (invocationPattern == null) {
        return this
    }
    return map {
        invocationPattern.toCode(listOf(it))
    }
}

