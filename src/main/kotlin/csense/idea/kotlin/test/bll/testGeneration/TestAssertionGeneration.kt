package csense.idea.kotlin.test.bll.testGeneration

import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtTypeReference


fun String.generateTestAssertions(
    assertionType: AssertionType
): String {
    if (assertionType == AssertionType.None) {
        return this
    }
    //TODO handle types, but in a Polymorphic way
    //csense
    val assertion = when (assertionType) {
        AssertionType.Equality -> ".assert()"
        else -> ".assertContains()"
    }
    return "${this}${assertion}"
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