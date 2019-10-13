package csense.idea.kotlin.test.inspections

import com.intellij.codeHighlighting.*
import com.intellij.codeInspection.*
import csense.idea.kotlin.test.bll.*
import csense.idea.kotlin.test.quickfixes.*
import org.jetbrains.kotlin.idea.inspections.*
import org.jetbrains.kotlin.idea.refactoring.*
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.*
import kotlin.system.*

class MissingTestsForPropertyInspector : AbstractKotlinInspection() {
    override fun getDisplayName(): String {
        return "Missing test for property (getter/setter)"
    }

    override fun getStaticDescription(): String? {
        return "Highlights properties with custom getter / setter that are missing test(s)"
    }

    override fun getDefaultLevel(): HighlightDisplayLevel {
        return HighlightDisplayLevel.WEAK_WARNING
    }

    override fun getDescriptionFileName(): String? {
        return "Highlights properties with custom getter / setter that are missing test(s) "
    }

    override fun getShortName(): String {
        return "MissingTestProperty"
    }

    override fun getGroupDisplayName(): String {
        return Constants.groupName
    }

    override fun isEnabledByDefault(): Boolean {
        return true
    }

    override fun buildVisitor(holder: ProblemsHolder,
                              isOnTheFly: Boolean): KtVisitorVoid {
        return propertyVisitor { prop ->
            if (prop.isPrivate() ||
                    prop.isProtected() ||
                    prop.isAbstract() ||
                    !prop.hasCustomSetterGetter() ||
                    prop.containingKtFile.shouldIgnore() ||
                    prop.isInTestModule()) {
                return@propertyVisitor//ignore private & protected  methods / non kt files.
            }

            if (prop.hasConstantCustomGetterOnly()) {
                return@propertyVisitor
            }
            val timeInMs = measureTimeMillis {

                //step 2 is to find the test file in the test root
                val testModule = prop.findTestModule() ?: return@propertyVisitor
                val resultingDirectory = testModule.findPackageDir(prop.containingKtFile)

                val testFile = resultingDirectory?.findTestFile(prop.containingKtFile)

                if (testFile == null && !prop.isTopLevel) {
                    return@propertyVisitor //skip class / obj functions if no test file is found
                }


                val namesToLookAt = prop.computeViableNames()
                val haveTestFunction = testFile?.haveTestOfMethodName(namesToLookAt) == true
                val haveTestObject = testFile?.haveTestObjectOfMethodName(namesToLookAt) == true
                if (!haveTestFunction && !haveTestObject) {
                    val fixes = createQuickFixesForFunction(testFile, prop)
                    holder.registerProblem(prop.nameIdentifier ?: prop,
                            "You have properly not tested this property (getter/setter)",
                            *fixes)
                }
            }
            if (timeInMs > 10) {
                println("Took $timeInMs ms")
            }
        }
    }

    fun createQuickFixesForFunction(file: KtFile?, ourProp: KtProperty): Array<LocalQuickFix> {
        val ktClassOrObject = file?.collectDescendantsOfType<KtClassOrObject>()?.firstOrNull()
                ?: return arrayOf()
        val testName = ourProp.computeMostPreciseName()
        return arrayOf(AddTestPropertyQuickFix(
                ourProp,
                testName,
                ktClassOrObject
        ))
    }


}

fun KtProperty.computeViableNames(): List<String> {
    val safeName = name ?: ""
    val extensionNames: List<String> = if (isExtensionDeclaration()) {
        val extName = receiverTypeReference?.text?.safeDecapitizedFunctionName()
        return listOfNotNull(extName + safeName.capitalize())
    } else {
        listOf()
    }
    return listOf(safeName) + extensionNames
}


fun KtProperty.hasCustomSetterGetter(): Boolean {
    return getter != null || setter != null
}

fun KtProperty.hasConstantCustomGetterOnly(): Boolean {
    return getter != null && setter == null && isGetterConstant()
}

fun KtProperty.isGetterConstant(): Boolean {
    val exp = getter?.bodyBlockExpression ?: getter?.bodyExpression ?: return false
    return exp.isConstant()
}

fun KtExpression.isConstant(): Boolean = when (this) {
    is KtConstantExpression -> {
        true
    }
    is KtStringTemplateExpression -> {
        this.isConstant()
    }
    else -> false
}

fun KtStringTemplateExpression.isConstant(): Boolean =
        isPlainWithEscapes()

fun KtProperty.computeMostPreciseName(): String {
    return if (isExtensionDeclaration()) {
        val extensionName = receiverTypeReference?.text?.safeDecapitizedFunctionName()
        extensionName?.plus(name?.capitalize() ?: "") ?: name ?: ""
    } else {
        name ?: ""
    }
}
