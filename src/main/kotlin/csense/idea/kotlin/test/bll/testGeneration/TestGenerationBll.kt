package csense.idea.kotlin.test.bll.testGeneration

import com.intellij.psi.*
import csense.idea.base.bll.kotlin.*
import csense.idea.base.bll.psi.*
import csense.kotlin.extensions.*
import csense.kotlin.extensions.collections.list.*
import csense.kotlin.extensions.primitives.*
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.*

fun KtClassOrObject.isUnit(): Boolean {
    val name = fqName?.asString()
    return name == "kotlin.Unit"
}

fun PsiClass.isClassContainedIn(containingName: String): Boolean {
    return getKotlinFqNameString()?.contains(".$containingName.") ?: false
}


private fun String.wrapInAsFunction(safeTestName: String, ktPsiFactory: KtPsiFactory): KtNamedFunction {
    return ktPsiFactory.createFunction(
        """
        @Test
        fun $safeTestName(){
            //TODO make me.
            $this
        }
        """.trimMargin()
    )
}


fun KtProperty.computeMostViableSimpleTestData(safeTestName: String, ktPsiFactory: KtPsiFactory): PsiElement {
    val receiverTypeReference = receiverTypeReference
    val typeToGuessOpt: KtTypeReference? = if (receiverTypeReference != null && valueParameters.isEmpty()) {
        receiverTypeReference
    } else if (valueParameters.size == 1 && receiverTypeReference == null) {
        valueParameters.firstOrNull()?.typeReference
    } else {
        if (receiverTypeReference != null && receiverTypeReference.text.isTypeProperlyAListType()) {
            return ktPsiFactory.createClass(
                computeListTestCode(
                    safeTestName,
                    "String",
                    FunctionInvocationPattern.ReceiverFunction(name ?: "")
                )
            )
        }
        return "".wrapInAsFunction(safeTestName, ktPsiFactory)
    }
    val code = handleOuterType(typeToGuessOpt, isTopLevel, false, safeTestName)
    return if (code.isClass) {
        KtPsiFactory(project).createClass(code.result)
    } else {
        code.result.wrapInAsFunction(safeTestName, ktPsiFactory)
    }
}

class OuterTypeResult(val isClass: Boolean, val result: String)

private fun KtNamedDeclaration.handleOuterType(
    typeToGuessOpt: KtTypeReference?,
    topLevel: Boolean,
    generateInvocation: Boolean,
    safeTestName: String
): OuterTypeResult {
    val typeToGuess = typeToGuessOpt ?: return OuterTypeResult(false, "")
    val nameOfType = typeToGuess.text
    val isKnown = when (nameOfType) {

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
    if (isKnown != null) {
        return OuterTypeResult(false, convertListToRealCode(isKnown, this, topLevel, generateInvocation))
    }//run though all classes and create that as "cases".
    //create code for each edition of the enum

    if (nameOfType.isTypeProperlyAListType()) {
        return OuterTypeResult(
            true,
            computeListTestCode(
                safeTestName, nameOfType, FunctionInvocationPattern.ReceiverFunction(
                    name ?: ""
                )
            )
        )
    }

    val realElement = typeToGuess.resolve() ?: return OuterTypeResult(true, "")
    when (realElement) {
        is KtClass -> when {
            realElement.isEnum() -> {
                val values = realElement.getEnumValues()
                return OuterTypeResult(
                    false,
                    convertListToRealCode(values.map { "$nameOfType.${it.name}" }, this, topLevel, generateInvocation)
                )
                //create code for each edition of the enum
            }
            realElement.isSealed() -> {
                val cases = realElement.findSealedClassInheritors()
                return OuterTypeResult(
                    false,
                    convertListToRealCode(cases.map { "${it.name}" }, this, topLevel, generateInvocation)
                )
                //run though all classes and create that as "cases".
            }
        }
    }

    return OuterTypeResult(false, "")
}

fun String.isTypeProperlyAListType(): Boolean =
    startsWithAny(
        "Array<",
        "Iterable<",
        "MutableList<",
        "List<",
        "Set<",
        "MutableSet<",
        "Collection<",
        "Map<",
        "MutableMap<"
    )

fun convertListToRealCode(
    isKnown: List<String>,
    fnc: KtNamedDeclaration,
    topLevel: Boolean,
    generateInvocation: Boolean
): String {
    val invocation = generateInvocation.map("()", "")
    val nameOfFnc = fnc.name ?: ""
    val result = if (fnc.isExtensionDeclaration()) {
        isKnown.map { "$it.$nameOfFnc$invocation" }
    } else if (topLevel) {
        //can only be a function.. we do not look at 0 arg things..
        isKnown.map { "$nameOfFnc($it)" }
    } else {
        val containingClass = fnc.containingClassOrObject
        if (containingClass?.isObjectLiteral() == true) {
            isKnown.map { "${containingClass.name}.$it()" }
        } else {
            //we are in a class ect.
            listOf("val clz = ${containingClass?.name}()") + isKnown.map { "clz.$it$invocation" }
        }
    }
    return result.joinToString("\n")
}



//region primitive data
val stringExamples = listOf(
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

val intExamples = listOf(
    "(-1)",
    "0",
    "1",
    "(-50)",
    "42"
)

val longExamples = listOf(
    "(-1L)",
    "0L",
    "1L",
    "(-50L)",
    "42L"
)

val doubleExamples = listOf(
    "(-1).toDouble()",
    "0.toDouble()",
    "1.toDouble()",
    "(-50).toDouble()",
    "42.toDouble()"
)
val floatExamples = listOf(
    "(-1).toFloat()",
    "0.toFloat()",
    "1.toFloat()",
    "(-50).toFloat()",
    "42.toFloat()"
)
val boolExamples = listOf(
    "false",
    "true"
)

val charExamples = listOf(
    "' '",
    "'a'",
    "'Q'",
    "'1'",
    "'?'",
    "'\b'",
    "'\\n'"
)
val byteExamples = listOf(
    "0.toByte()",
    "(-1).toByte()",
    "1.toByte()",
    "80.toByte()",
    "(-82).toByte()"
)
val shortExamples = listOf(
    "0.toShort()",
    "(-1).toShort()",
    "1.toShort()",
    "80.toShort()",
    "(-82).toShort()"
)
//endregion

//region list data
val stringListExamples = listOf(
    "listOf<String>()",
    "listOf(\"\")",
    "listOf(\"a\")",
    "listOf(\"a\",\"b\")"
)

val intListExamples = listOf(
    "listOf<Int>()",
    "listOf(0)",
    "listOf(-1)",
    "listOf(1,2)"
)

val charListExamples = listOf(
    "listOf<Char>()",
    "listOf(' ')",
    "listOf('a')",
    "listOf('a','b')"
)

val booleanListExamples = listOf(
    "listOf<Boolean>()",
    "listOf(false)",
    "listOf(true)",
    "listOf(false,true)"
)

val floatListExamples = listOf(
    "listOf<Float>()",
    "listOf(0f)",
    "listOf(1f)",
    "listOf(-1f)",
    "listOf(5f,-5f)"
)

val doubleListExamples = listOf(
    "listOf<Double>()",
    "listOf(0.toDouble())",
    "listOf(5.toDouble())",
    "listOf((-5).toDouble())",
    "listOf(80.toDouble(),(-80).toDouble()))"
)

val longListExamples = listOf(
    "listOf<Long>()",
    "listOf(0L)",
    "listOf(5L)",
    "listOf(-5L)",
    "listOf(80L,-80L))"
)

val byteListExamples = listOf(
    "listOf<Byte>()",
    "listOf(0.toByte())",
    "listOf(5.toByte())",
    "listOf((-5).toByte())",
    "listOf(80.toByte(),(-80).toByte()))"
)
val shortListExamples = listOf(
    "listOf<Short>()",
    "listOf(0.toShort())",
    "listOf(5.toShort())",
    "listOf((-5).toShort())",
    "listOf(80.toShort(),(-80).toShort()))"
)
//endregion

//region array data
val stringArrayExamples = listOf(
    "arrayOf<String>()",
    "arrayOf(\"\")",
    "arrayOf(\"a\")",
    "arrayOf(\"a\",\"b\")"
)

val intArrayExamples = listOf(
    "arrayOf<Int>()",
    "arrayOf(0)",
    "arrayOf(-1)",
    "arrayOf(1,2)"
)

val charArrayExamples = listOf(
    "arrayOf<Char>()",
    "arrayOf(' ')",
    "arrayOf('a')",
    "arrayOf('a','b')"
)

val booleanArrayExamples = listOf(
    "arrayOf<Boolean>()",
    "arrayOf(false)",
    "arrayOf(true)",
    "arrayOf(false,true)"
)

val floatArrayExamples = listOf(
    "arrayOf<Float>()",
    "arrayOf(0f)",
    "arrayOf(1f)",
    "arrayOf(-1f)",
    "arrayOf(5f,-5f)"
)

val doubleArrayExamples = listOf(
    "arrayOf<Double>()",
    "arrayOf(0.toDouble())",
    "arrayOf(5.toDouble())",
    "arrayOf((-5).toDouble())",
    "arrayOf(80.toDouble(),(-80).toDouble()))"
)

val longArrayExamples = listOf(
    "arrayOf<Long>()",
    "arrayOf(0L)",
    "arrayOf(5L)",
    "arrayOf(-5L)",
    "arrayOf(80L,-80L))"
)

val byteArrayExamples = listOf(
    "arrayOf<Byte>()",
    "arrayOf(0.toByte())",
    "arrayOf(5.toByte())",
    "arrayOf((-5).toByte())",
    "arrayOf(80.toByte(),(-80).toByte()))"
)
val shortArrayExamples = listOf(
    "arrayOf<Short>()",
    "arrayOf(0.toShort())",
    "arrayOf(5.toShort())",
    "arrayOf((-5).toShort())",
    "arrayOf(80.toShort(),(-80).toShort()))"
)
//endregion
