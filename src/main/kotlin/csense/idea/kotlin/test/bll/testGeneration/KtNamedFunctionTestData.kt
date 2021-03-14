package csense.idea.kotlin.test.bll.testGeneration

import csense.idea.base.bll.psi.findParentOfType
import csense.kotlin.extensions.collections.list.subListSafe
import csense.kotlin.extensions.mapOptional
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.KtTypeReference

object KtNamedFunctionTestData {
    fun computeMostViableSimpleTestData(
        function: KtNamedFunction,
        testName: String,
        createAssertStatements: Boolean = false
    ): String = when {
        function.shouldUseSimpleTests() -> function.createSimpleTests(testName, createAssertStatements)
        else -> function.createComplexTests(testName)
    }
}

fun KtNamedFunction.shouldUseSimpleTests(): Boolean {
    val params = getTotalParameterCount()
    val firstParam = getFirstParameterOrNull()
    return when {
        hasTypeParameters() -> false //generic code is complex! the generic type could contain a list
        params == 0 -> true
        params == 1 && firstParam != null -> !firstParam.isProperlyAListType()
        else -> false //its properly complex
    }
}

fun KtTypeReference.isProperlyAListType(): Boolean {
    return text?.isTypeProperlyAListType() ?: false
}

fun KtNamedFunction.getFirstParameterOrNull(): KtTypeReference? {
    return receiverTypeReference ?: valueParameters.firstOrNull()?.typeReference
}

fun KtNamedFunction.hasTypeParameters(): Boolean {
    return typeParameters.isNotEmpty()
}

fun KtNamedFunction.getTotalParameterCount(): Int {
    return valueParameters.size + receiverTypeReference.mapOptional(1, 0)
}

fun KtNamedFunction.isReceiver(): Boolean {
    return receiverTypeReference != null
}

fun KtNamedFunction.toFunctionInvocationPattern(): FunctionInvocationPattern? {
    when {
        isReceiver() ->
            return FunctionInvocationPattern.ReceiverFunction(name ?: "")
        isTopLevel ->
            return FunctionInvocationPattern.TopLevelFunction(name ?: "")
        else -> {
            val clz = findParentOfType<KtClassOrObject>() ?: return null
            return FunctionInvocationPattern.Method(
                name = name ?: "",
                classNameInstance = clz.name ?: ""
            )
        }
    }
}

sealed class FunctionInvocationPattern(val name: String) {
    abstract fun toCode(parameters: List<String>): String

    class ReceiverFunction(name: String) : FunctionInvocationPattern(name) {
        override fun toCode(parameters: List<String>): String {
            return "${parameters.first()}.$name(${
                parameters.subListSafe(1, parameters.size).joinToString(",")
            })"
        }

    }

    class Method(
        name: String,
        private val classNameInstance: String
    ) : FunctionInvocationPattern(name) {
        override fun toCode(parameters: List<String>): String {
            return "$classNameInstance.$name(${parameters.joinToString(",")})"
        }
    }

    class TopLevelFunction(name: String) : FunctionInvocationPattern(name) {
        override fun toCode(parameters: List<String>): String {
            return "$name(${parameters.joinToString(",")})"
        }
    }


}