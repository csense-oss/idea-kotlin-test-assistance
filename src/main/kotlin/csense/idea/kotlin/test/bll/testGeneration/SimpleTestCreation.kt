package csense.idea.kotlin.test.bll.testGeneration

import csense.kotlin.extensions.collections.isNotNullOrEmpty
import csense.kotlin.extensions.mapLazy
import org.intellij.lang.annotations.Language
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtTypeReference
//
//fun KtNamedFunction.createSimpleTests(
//    testName: String,
//    shouldGenerateAssertions: Boolean
//): String {
//    //we are to create a simple function that will invoke the method with say simple test data.
//    val firstParameter = getFirstParameterOrNull()
//    val invocationPattern = toFunctionInvocationPattern()
//    if (firstParameter != null) {
//
//        return SimpleTestCreation.createTestForSingleArgument(
//            testName,
//            firstParameter,
//            invocationPattern,
//            shouldGenerateAssertions
//        )
//    }
//
//    return SimpleTestCreation.createTestFunctionWithCodeLines(
//        testName,
//        listOf(),
//        AssertionType.None
//    )
//}

//
//object SimpleTestCreation {
//
//    fun createTestForSingleArgument(
//        testName: String,
//        type: KtTypeReference,
//        invocationPattern: FunctionInvocationPattern?,
//        shouldGenerateAssertions: Boolean
//    ): String {
//        val firstParameterType: String = type.text
//        //return decl required for assertions...
//        val assertionType = shouldGenerateAssertions.mapLazy(
//            { type.toAssertionType() }, { AssertionType.None })
//        val testCode = findKnownTestCodeLines(firstParameterType)
//        return if (testCode.isNotNullOrEmpty()) {
//            createTestFunctionWithCodeLines(
//                testName,
//                testCode.map {
//                    TestCode(
//                        expectedResult = it.expectedResult,
//                        testCode = it.testCode.withInvocationPattern(invocationPattern)
//                    )
//                },
//                assertionType
//            )
//        } else {
//            createTestFunctionWithCodeLines(
//                testName,
//                listOf(),
//                assertionType
//            )
//        }
//    }
//
//    fun findKnownTestCodeLines(nameOfType: String): List<TestCode>? = when (nameOfType) {
//
//        //region lists
//        "List<Byte>" -> byteListExamples
//        "List<Short>" -> shortListExamples
//        "List<Int>" -> intListExamples
//        "List<Long>" -> longListExamples
//
//        "List<Float>" -> floatListExamples
//        "List<Double>" -> doubleListExamples
//
//        "List<Boolean>" -> booleanListExamples
//
//        "List<Char>" -> charListExamples
//        "List<String>" -> stringListExamples
//        //endregion
//
//        //region array
//        "Array<Byte>" -> byteArrayExamples
//        "Array<Short>" -> shortArrayExamples
//        "Array<Int>" -> intArrayExamples
//        "Array<Long>" -> longArrayExamples
//
//        "Array<Float>" -> floatArrayExamples
//        "Array<Double>" -> doubleArrayExamples
//
//        "Array<Boolean>" -> booleanArrayExamples
//
//        "Array<Char>" -> charArrayExamples
//        "Array<String>" -> stringArrayExamples
//        //endregion
//
//        //region primitives
//        "Byte" -> byteExamples
//        "Short" -> shortExamples
//        "Int" -> intExamples
//        "Long" -> longExamples
//
//        "Float" -> floatExamples
//        "Double" -> doubleExamples
//
//        "Char" -> charExamples
//
//        "Boolean" -> boolExamples
//
//        "String" -> stringExamples
//        //endregion
//
//        else -> null
//    }
//
//    fun createTestFunctionWithCodeLines(
//        testName: String,
//        testCode: List<TestCode>,
//        assertionType: AssertionType
//    ): String {
//        return createSimpleTestCodeFromTemplate(
//            testName,
//            testCode.joinToString("\n") {
//                it.generateTestAssertions(
//                    assertionType
//                )
//            }
//        )
//    }
//
//    //region primitive data
//    private val stringExamples = listOf(
//        TestCode("", "\"\""),
//        TestCode("", "\" \""),
//        TestCode("", "\"a\""),
//        TestCode("", "\"abc\""),
//        TestCode("", "\"1234\""),
//        TestCode("", "\"Other region 한\""),
//        TestCode("", "\"Hi ☺\""),
//        TestCode("", "\"�\b\""),
//        TestCode("", "\"\\n\""),
//        TestCode("", "\"...()[]\"")
//    )
//
//    private val intExamples = listOf(
//        TestCode("", "(-1)"),
//        TestCode("", "0"),
//        TestCode("", "1"),
//        TestCode("", "(-50)"),
//        TestCode("", "42")
//    )
//
//    private val longExamples = listOf(
//        TestCode("", "(-1L)"),
//        TestCode("", "0L"),
//        TestCode("", "1L"),
//        TestCode("", "(-50L)"),
//        TestCode("", "42L")
//    )
//
//    private val doubleExamples = listOf(
//        TestCode("", "(-1).toDouble()"),
//        TestCode("", "0.toDouble()"),
//        TestCode("", "1.toDouble()"),
//        TestCode("", "(-50).toDouble()"),
//        TestCode("", "42.toDouble()"),
//        TestCode("", "0.5"),
//        TestCode("", "100.956")
//    )
//    private val floatExamples = listOf(
//        TestCode("", "(-1).toFloat()"),
//        TestCode("", "0.toFloat()"),
//        TestCode("", "1.toFloat()"),
//        TestCode("", "(-50).toFloat()"),
//        TestCode("", "42.toFloat()"),
//        TestCode("", "0.5f"),
//        TestCode("", "100.956f")
//    )
//    private val boolExamples = listOf(
//        TestCode("", "false"),
//        TestCode("", "true")
//    )
//
//    private val charExamples = listOf(
//        TestCode("", "' '"),
//        TestCode("", "'a'"),
//        TestCode("", "'Q'"),
//        TestCode("", "'1'"),
//        TestCode("", "'?'"),
//        TestCode("", "'\b'"),
//        TestCode("", "'\\n'")
//    )
//    private val byteExamples = listOf(
//        TestCode("", "0.toByte()"),
//        TestCode("", "(-1).toByte()"),
//        TestCode("", "1.toByte()"),
//        TestCode("", "80.toByte()"),
//        TestCode("", "(-82).toByte()")
//    )
//    private val shortExamples = listOf(
//        TestCode("", "0.toShort()"),
//        TestCode("", "(-1).toShort()"),
//        TestCode("", "1.toShort()"),
//        TestCode("", "80.toShort()"),
//        TestCode("", "(-82).toShort()")
//    )
////endregion
//
//    //region list data
//    private val stringListExamples = listOf(
//        TestCode("", "listOf<String>()"),
//        TestCode("", "listOf(\"\")"),
//        TestCode("", "listOf(\"a\")"),
//        TestCode("", "listOf(\"a\",\"b\")")
//    )
//
//    private val intListExamples = listOf(
//        TestCode("", "listOf<Int>()"),
//        TestCode("", "listOf(0)"),
//        TestCode("", "listOf(-1)"),
//        TestCode("", "listOf(1,2)")
//    )
//
//    private val charListExamples = listOf(
//        TestCode("", "listOf<Char>()"),
//        TestCode("", "listOf(' ')"),
//        TestCode("", "listOf('a')"),
//        TestCode("", "listOf('a','b')")
//    )
//
//    private val booleanListExamples = listOf(
//        TestCode("", "listOf<Boolean>()"),
//        TestCode("", "listOf(false)"),
//        TestCode("", "listOf(true)"),
//        TestCode("", "listOf(false,true)")
//    )
//
//    private val floatListExamples = listOf(
//        TestCode("", "listOf<Float>()"),
//        TestCode("", "listOf(0f)"),
//        TestCode("", "listOf(1f)"),
//        TestCode("", "listOf(-1f)"),
//        TestCode("", "listOf(5f,-5f)")
//    )
//
//    private val doubleListExamples = listOf(
//        TestCode("", "listOf<Double>()"),
//        TestCode("", "listOf(0.toDouble())"),
//        TestCode("", "listOf(5.toDouble())"),
//        TestCode("", "listOf((-5).toDouble())"),
//        TestCode("", "listOf(80.toDouble(),(-80).toDouble()))")
//    )
//
//    private val longListExamples = listOf(
//        TestCode("", "listOf<Long>()"),
//        TestCode("", "listOf(0L)"),
//        TestCode("", "listOf(5L)"),
//        TestCode("", "listOf(-5L)"),
//        TestCode("", "listOf(80L,-80L))")
//    )
//
//    private val byteListExamples = listOf(
//        TestCode("", "listOf<Byte>()"),
//        TestCode("", "listOf(0.toByte())"),
//        TestCode("", "listOf(5.toByte())"),
//        TestCode("", "listOf((-5).toByte())"),
//        TestCode("", "listOf(80.toByte(),(-80).toByte()))")
//    )
//    private val shortListExamples = listOf(
//        TestCode("", "listOf<Short>()"),
//        TestCode("", "listOf(0.toShort())"),
//        TestCode("", "listOf(5.toShort())"),
//        TestCode("", "listOf((-5).toShort())"),
//        TestCode("", "listOf(80.toShort(),(-80).toShort()))")
//    )
////endregion
//
//    //region array data
//    private val stringArrayExamples = listOf(
//        TestCode("", "arrayOf<String>()"),
//        TestCode("", "arrayOf(\"\")"),
//        TestCode("", "arrayOf(\"a\")"),
//        TestCode("", "arrayOf(\"a\",\"b\")")
//    )
//
//    private val intArrayExamples = listOf(
//        TestCode("", "arrayOf<Int>()"),
//        TestCode("", "arrayOf(0)"),
//        TestCode("", "arrayOf(-1)"),
//        TestCode("", "arrayOf(1,2)")
//    )
//
//    private val charArrayExamples = listOf(
//        TestCode("", "arrayOf<Char>()"),
//        TestCode("", "arrayOf(' ')"),
//        TestCode("", "arrayOf('a')"),
//        TestCode("", "arrayOf('a','b')")
//    )
//
//    private val booleanArrayExamples = listOf(
//        TestCode("", "arrayOf<Boolean>()"),
//        TestCode("", "arrayOf(false)"),
//        TestCode("", "arrayOf(true)"),
//        TestCode("", "arrayOf(false,true)")
//    )
//
//    private val floatArrayExamples = listOf(
//        TestCode("", "arrayOf<Float>()"),
//        TestCode("", "arrayOf(0f)"),
//        TestCode("", "arrayOf(1f)"),
//        TestCode("", "arrayOf(-1f)"),
//        TestCode("", "arrayOf(5f,-5f)")
//    )
//
//    private val doubleArrayExamples = listOf(
//        TestCode("", "arrayOf<Double>()"),
//        TestCode("", "arrayOf(0.toDouble())"),
//        TestCode("", "arrayOf(5.toDouble())"),
//        TestCode("", "arrayOf((-5).toDouble())"),
//        TestCode("", "arrayOf(80.toDouble(),(-80).toDouble()))")
//    )
//
//    private val longArrayExamples = listOf(
//        TestCode("", "arrayOf<Long>()"),
//        TestCode("", "arrayOf(0L)"),
//        TestCode("", "arrayOf(5L)"),
//        TestCode("", "arrayOf(-5L)"),
//        TestCode("", "arrayOf(80L,-80L))")
//    )
//
//    private val byteArrayExamples = listOf(
//        TestCode("", "arrayOf<Byte>()"),
//        TestCode("", "arrayOf(0.toByte())"),
//        TestCode("", "arrayOf(5.toByte())"),
//        TestCode("", "arrayOf((-5).toByte())"),
//        TestCode("", "arrayOf(80.toByte(),(-80).toByte()))")
//    )
//    private val shortArrayExamples = listOf(
//        TestCode("", "arrayOf<Short>()"),
//        TestCode("", "arrayOf(0.toShort())"),
//        TestCode("", "arrayOf(5.toShort())"),
//        TestCode("", "arrayOf((-5).toShort())"),
//        TestCode("", "arrayOf(80.toShort(),(-80).toShort()))")
//    )
////endregion
//
//
//    fun createSimpleTestCodeFromTemplate(
//        testName: String,
//        testCode: String
//    ): String {
//        @Suppress("UnnecessaryVariable")
//        @Language("kotlin")
//        val code = """
//        @Test
//        fun $testName(){
//            $testCode
//        }
//    """.trimIndent()
//        return code
//    }
//}
//
//fun String.withInvocationPattern(
//    invocationPattern: FunctionInvocationPattern?
//): String {
//    if (invocationPattern == null) {
//        return this
//    }
//    return invocationPattern.toCode(listOf(this))
//}

class TestCode(
    val expectedResult: String,
    //also called Actual
    val testCode: String
)