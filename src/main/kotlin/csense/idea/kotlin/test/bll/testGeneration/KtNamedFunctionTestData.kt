package csense.idea.kotlin.test.bll.testGeneration

import com.intellij.psi.*
import com.intellij.psi.search.searches.*
import csense.idea.base.*
import csense.idea.base.bll.kotlin.*
import csense.idea.base.bll.psi.*
import csense.idea.kotlin.test.bll.testGeneration.KtNamedFunctionTestData.computeTestCreationLookup
import csense.kotlin.extensions.*
import csense.kotlin.extensions.collections.list.*
import csense.kotlin.specificExtensions.string.*
import org.jetbrains.kotlin.asJava.*
import org.jetbrains.kotlin.idea.quickfix.createFromUsage.callableBuilder.*
import org.jetbrains.kotlin.psi.*

object KtNamedFunctionTestData {
    fun computeMostViableSimpleTestData(
        function: KtNamedFunction,
        testName: String,
        assertion: TestAssertionType
    ): String {
        val receiver = function.receiverTypeReference

        val inputTypes: MutableList<TestCreationParameter> = mutableListOf()
        if (receiver != null) {
            inputTypes += TestCreationParameter(testName, receiver)
        }
        inputTypes += function.valueParameters.mapNotNull {
            val ref = it.typeReference ?: return@mapNotNull null
            TestCreationParameter(it.nameAsSafeName.asString(), ref)
        }

        return createTestFor(
            function,
            testName,
            assertion,
            inputTypes
        )
    }

    fun createTestFor(
        function: KtNamedFunction,
        toTestName: String,
        assertion: TestAssertionType,
        inputTypesToCreateFor: List<TestCreationParameter>
    ): String {
        if (inputTypesToCreateFor.isEmpty()) {
            return createFunction(testName = toTestName, tests = listOf(), assertion = assertion, expected = "")
        }
        val functions = inputTypesToCreateFor.mapIndexed { index: Int, parameter: TestCreationParameter ->
            createTestCodeFor(
                parameter,
                index,
                function,
                inputTypesToCreateFor,
                assertion
            )
        }
        val singleFunction = functions.singleOrNull()
        if (singleFunction != null) {
            return singleFunction
        }
        return createClassCode(testName = toTestName, functions = functions)
    }

    fun createClassCode(testName: String, functions: List<String>): String {
        val startClass = "class ${testName.capitalize()}{"
        val endClass = "}"
        val nl = "\n"
        val functionsToString = functions.joinToString(nl)
        return "$startClass$nl$functionsToString$nl$endClass"
    }

    fun createFunction(
        testName: String,
        tests: List<String>,
        assertion: TestAssertionType,
        expected: String
    ): String {
        val code = assertion.applyTo(tests, expected).joinToString("\n")
        return """
            @Test
            fun ${testName.safeFunctionName()}(){
                $code
            }
        """
    }

    fun TestAssertionType.applyTo(testCode: List<String>, expected: String): List<String> = testCode.map {
        applyTo(it, expected)
    }

    fun TestAssertionType.applyTo(testCode: String, expected: String): String {
        if (expected.isEmpty()) {
            return testCode
        }
        return when (this) {
            //TODO improve
            TestAssertionType.Csense -> "$testCode.assert($expected)"
            TestAssertionType.Junit -> "assertEquals($expected,$testCode)"
            TestAssertionType.None -> testCode
        }
    }

    fun createTestCodeFor(
        typeToVary: TestCreationParameter,
        typeToVaryIndex: Int,
        function: KtNamedFunction,
        allParameters: List<TestCreationParameter>,
        assertion: TestAssertionType
    ): String { //TODO list instead, such that lists can create multiple fun's?
        val parametersForType: List<String> = typeToVary.lookup().parameters
        val invocation = function.toFunctionInvocationPattern() ?: return ""
        val parameters: List<String> = allParameters.map {
            it.lookup().emptyParameter
        }


        val returnType = function.getReturnTypeReference()
        val expectedEmpty = returnType?.let {
            TestCreationParameter("", it).lookup().emptyParameter
        } ?: ""

        val testName = function.nameAsSafeName.asString().safeFunctionName()

        val testCode = parametersForType.map {
            val updatedParameters = parameters.toMutableList()
            updatedParameters[typeToVaryIndex] = it
            invocation.toCode(updatedParameters)
        }

        val isProperlyAList = typeToVary.typeReference.isProperlyAListType()
        return if (isProperlyAList) {
            val innerType = typeToVary.typeReference.resolveListType()
            val isNullable = innerType?.isNullableType() == true
            val forSingleTestCode = testCode.take(isNullable.map(ifTrue = 2, ifFalse = 1))
            val forEmpty = listOf(
                invocation.toCode(
                    listOf(
                        getCollectionTypeWrapper()[typeToVary.getTypeFqName()]?.wrapWithTypeArgs(
                            s = "",
                            typeArgs = innerType?.resolve()?.getKotlinFqNameString() ?: ""
                        ) ?: ""
                    )
                )
            )
            createClassCode(
                testName,
                listOf(
                    createFunction("empty", forEmpty, assertion, expectedEmpty),
                    createFunction("single", forSingleTestCode, assertion, expectedEmpty),
                    createFunction("multiple", testCode, assertion, expectedEmpty)
                )
            )
        } else {
            createFunction(testName, testCode, assertion, expectedEmpty)
        }

    }

    fun TestCreationParameter.getTypeFqName(): String {
        val resolved = typeReference.resolve()
        return resolved?.getKotlinFqNameString() ?: return ""
    }


    fun KtTypeReference.lookup(): TestCreationLookup {
        val resolved = resolve()
        val fqName = resolved?.getKotlinFqNameString()
        //default / primitives / hardcoded
        getLookupTable()[fqName]?.let {
            return@lookup it.withNullIf(isNullableType())
        }

        //Lists & arrays, sets etc (not multiple types)
        val innerType = resolveListType()
        val innerTypeLookedUp = innerType?.lookup()
        if (innerType != null && innerTypeLookedUp != null) {
            getCollectionTypeWrapper()[fqName]?.let {
                return@lookup innerTypeLookedUp.withWrapping(it.prefix, it.postfix)
            }
        }
        //classes
        if (resolved is KtClass) {
            return when {
                resolved.isEnum() -> resolved.computeEnumToTestCreationLookup()
                resolved.isSealed() -> resolved.computeSealedClassToTestCreationLook()
                else -> resolved.computeTestCreationLookup()
            }
        }
        return TestCreationLookup.empty
    }


    fun TestCreationParameter.lookup(): TestCreationLookup {
        return typeReference.lookup()
    }

    fun KtClass.computeSealedClassToTestCreationLook(): TestCreationLookup {
        val allClassCases = findSealedClassInheritors()
        //TODO improve this as well
        val constructors: List<String> = allClassCases.map { ktClass: KtClassOrObject ->
            val x = ktClass.computeTestCreationLookup(prefix = this.nameAsSafeName.asString())
            x.emptyParameter
        }
        val first = constructors.firstOrNull() ?: return TestCreationLookup.empty
        return TestCreationLookup(
            emptyParameter = first,
            parameters = constructors
        )
    }

    fun KtClass.findSealedClassInheritors(): List<KtClassOrObject> {
        return ClassInheritorsSearch.search(toLightClass() as PsiClass)
            .mapNotNull { it.navigationElement as? KtClassOrObject }
    }

    fun KtClassOrObject.computeTestCreationLookup(prefix: String? = null): TestCreationLookup {
        if (this is KtClass) {
            return computeTestCreationLookup(prefix)
        }
        //this is KtObject (if there ever was such a thing)
        val name = prefix.mapOptional("$prefix.", "") + this.nameAsSafeName.asString()
        return TestCreationLookup(
            emptyParameter = name,
            parameters = listOf(name)
        )
    }

    fun KtClass.computeTestCreationLookup(prefix: String? = null): TestCreationLookup {
        //TODO do all constructors
        val name = name
        val args = if (allConstructors.isEmpty()) {
            ""
        } else {
            val ctr = allConstructors.firstOrNull() ?: return TestCreationLookup.empty
            val args = ctr.valueParameters.map { parameter ->
                val type = parameter.typeReference ?: return@computeTestCreationLookup TestCreationLookup.empty
                TestCreationParameter(parameter.nameAsSafeName.asString(), type).lookup()
            }
            val argsAsString = args.joinToString(", ") { lookup ->
                lookup.emptyParameter
            }
            argsAsString
        }

        val optPrefixString = prefix.mapOptional("$prefix.", "")
        val code = "${optPrefixString}${nameAsSafeName.asString()}($args)"
        return TestCreationLookup(
            emptyParameter = code,
            parameters = listOf(code)
        )
    }

    fun KtClass.computeEnumToTestCreationLookup(): TestCreationLookup {
        val allEnumCases = getEnumValues()
        val className = this.nameAsSafeName.asString()
        val converted: List<String> = allEnumCases.map {
            className + "." + it.nameAsSafeName.asString()
        }
        val first = converted.firstOrNull() ?: return TestCreationLookup.empty
        return TestCreationLookup(first, converted)
    }


    fun getLookupTable(): Map<String, TestCreationLookup> {
        return mapOf(
            "kotlin.Int" to TestCreationLookup(
                emptyParameter = "0", parameters = intExamples
            ),
            "kotlin.Long" to TestCreationLookup(
                emptyParameter = "0L", parameters = longExamples
            ),
            "kotlin.Float" to TestCreationLookup(
                emptyParameter = "0f", parameters = floatExamples
            ),
            "kotlin.Double" to TestCreationLookup(
                emptyParameter = "0.0", parameters = doubleExamples
            ),
            "kotlin.Byte" to TestCreationLookup(
                emptyParameter = "0", parameters = byteExamples
            ),
            "kotlin.Char" to TestCreationLookup(
                emptyParameter = "'a'", parameters = charExamples
            ),
            "kotlin.String" to TestCreationLookup(
                emptyParameter = "\"\"", parameters = stringExamples
            ),
            "kotlin.Boolean" to TestCreationLookup(
                emptyParameter = "false", parameters = boolExamples
            ),
            "kotlin.Short" to TestCreationLookup(
                emptyParameter = "0", parameters = shortExamples
            )
        )
    }

    fun getCollectionTypeWrapper(): Map<String, CollectionTypeWrapper> {
        return mapOf(
            "kotlin.collections.List" to CollectionTypeWrapper(
                prefix = "listOf(",
                postfix = ")"
            ),
            "kotlin.collections.ArrayList" to CollectionTypeWrapper(
                prefix = "listOf(",
                postfix = ")"
            ),
            "kotlin.collections.Collection" to CollectionTypeWrapper(
                prefix = "listOf(",
                postfix = ")"

            ),
            "kotlin.collections.Iterable" to CollectionTypeWrapper(
                prefix = "listOf(",
                postfix = ")"

            ),
            "kotlin.collections.Set" to CollectionTypeWrapper(
                prefix = "setOf(",
                postfix = ")"

            ),
            "kotlin.collections.HashSet" to CollectionTypeWrapper(
                prefix = "hashSetOf(",
                postfix = ")"
            ),
            "kotlin.Array" to CollectionTypeWrapper(
                prefix = "arrayOf(",
                postfix = ")"
            ),
        )
    }
//    TODO after dev do this instead
//    val lookupTable: Map<String, TestCreationLookup> =

}


data class CollectionTypeWrapper(
    val prefix: String,
    val postfix: String
) {
    fun wrap(s: String): String {
        return s.modifications.wrapIn(prefix, postfix)
    }

    fun wrapWithTypeArgs(s: String, typeArgs: String): String {
        //TODO ugly hack...
        val typedPrefix = prefix.replace("(", "<$typeArgs>(")
        return s.modifications.wrapIn(typedPrefix, postfix)
    }
}

fun TestCreationLookup.withWrapping(prefix: String, postfix: String): TestCreationLookup {
    return TestCreationLookup(
        emptyParameter = emptyParameter.modifications.wrapIn(prefix, postfix),
        parameters = this.parameters.map {
            it.modifications.wrapIn(prefix, postfix)
        }
    )
}

fun TestCreationLookup.withNullIf(nullableType: Boolean): TestCreationLookup = this.applyIf(nullableType) {
    withNull()
}


fun TestCreationLookup.withNull(): TestCreationLookup {
    return TestCreationLookup(
        emptyParameter = "null",
        parameters = listOf("null", this.emptyParameter) + this.parameters
    )
}


class TestCreationLookup(val emptyParameter: String, val parameters: List<String>) {
    companion object {
        val empty = TestCreationLookup("", listOf())
    }
}

class TestCreationParameter(val name: String, val typeReference: KtTypeReference)

fun KtTypeReference.isProperlyAListType(): Boolean {
    return text?.isTypeProperlyAListType() ?: false
}

fun KtNamedFunction.getFirstParameterOrNull(): KtTypeReference? {
    return receiverTypeReference ?: valueParameters.firstOrNull()?.typeReference
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
                classNameInstance = clz.name ?: "",
                clz = clz
            )
        }
    }
}

sealed class FunctionInvocationPattern(val name: String) {
    abstract fun toCode(parameters: List<String>): String

    //TODO receiver function in class / object?
    class ReceiverFunction(name: String) : FunctionInvocationPattern(name) {
        override fun toCode(parameters: List<String>): String {
            return "${parameters.first()}.$name(${
                parameters.subListSafe(1, parameters.size).joinToString(",")
            })"
        }

    }

    class Method(
        name: String,
        private val classNameInstance: String,
        val clz: KtClassOrObject
    ) : FunctionInvocationPattern(name) {
        override fun toCode(parameters: List<String>): String {
            val startup = clz.computeTestCreationLookup().emptyParameter
            return "$startup.$name(${parameters.joinToString(",")})"
        }
    }

    class TopLevelFunction(name: String) : FunctionInvocationPattern(name) {
        override fun toCode(parameters: List<String>): String {
            return "$name(${parameters.joinToString(",")})"
        }
    }
}

enum class TestCodeType {
    Primitive,
    Comparable,
    List,
    Set,
    Map
}

fun KtTypeReference.resolve(): PsiElement? {
    return typeElement?.resolve()
}

fun KtTypeElement.resolve(): PsiElement? {
    return when (val type = this) {
        is KtUserType -> type.resolve()
        is KtNullableType -> type.resolve()
        is KtDynamicType -> null //javascript's dynamic..
        is KtSelfType -> null //what the... recursive types?
        else -> null
    }
}

fun KtNullableType.resolve(): PsiElement? {
    return this.innerType?.resolve()
}

fun KtUserType.resolve(): PsiElement? {
    return referenceExpression
        ?.references?.firstOrNull()
        ?.resolve()
        ?.mapIfInstance { it: KtTypeAlias ->
            it.getTypeReference()?.resolve()
        }
}

fun KtTypeReference.resolveListType(): KtTypeReference? {
    return typeElement?.resolveListType()
}

fun KtTypeElement.resolveListType(): KtTypeReference? {
    return when (val type = this) {
        is KtUserType -> type.resolveListType()
        is KtNullableType -> type.resolveListType()
        is KtDynamicType -> null //javascript's dynamic..
        is KtSelfType -> null //what the... recursive types?
        else -> null
    }
}

fun KtUserType.resolveListType(): KtTypeReference? {
    return typeArgumentsAsTypes.firstOrNull()
}


fun KtNullableType.resolveListType(): KtTypeReference? {
    return typeArgumentsAsTypes.firstOrNull()
}


fun KtTypeReference.isNullableType(): Boolean {
    return typeElement is KtNullableType
}


fun <T> T.applyIf(shouldApply: Boolean, function: (T) -> T): T {
    return if (shouldApply) {
        function(this)
    } else {
        this
    }
}
