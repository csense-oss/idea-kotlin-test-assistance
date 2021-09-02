package csense.idea.kotlin.test.bll.testGeneration

import org.jetbrains.kotlin.psi.KtTypeReference


fun TestCode.generateTestAssertions(
    assertionType: AssertionType
): String {
    //TODO read from settings
    val testType = TestAssertionType.Csense
    return when (testType) {
        TestAssertionType.Junit -> assertionType.generateTestAssertionsForJunit(this.testCode, this.expectedResult)
        TestAssertionType.Csense -> assertionType.generateTestAssertionsForCsense(this.testCode, this.expectedResult)
        else -> testCode
    }
}

enum class TestAssertionType {
    Csense,
    Junit,
    None
}

fun AssertionType.generateTestAssertionsForCsense(testCode: String, expected: String): String {
    val assertion = when (this) {
        AssertionType.Equality -> ".assert($expected)"
        AssertionType.Contains -> ".assertContains($expected)"
        AssertionType.None -> ""
    }
    return "${testCode}${assertion}"
}

fun AssertionType.generateTestAssertionsForJunit(testCode: String, expected: String): String {
    return when (this) {
        AssertionType.Equality -> "Assertions.assertEquals($expected, $testCode)"
        AssertionType.Contains -> "Assertions.assertIterableEquals($expected, $testCode)"
        AssertionType.None -> ""
    }
}

sealed class AssertionType {
    object None : AssertionType()
    object Equality : AssertionType()
    object Contains : AssertionType()
}

fun KtTypeReference.toAssertionType(): AssertionType {
    val type = text
    if (type.isTypeProperlyAListType()) {
        return AssertionType.Contains
    }
    return AssertionType.Equality
}