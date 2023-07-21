package csense.idea.kotlin.test.bll.psi

import csense.idea.base.bll.psiWrapper.imports.*
import csense.idea.base.bll.psiWrapper.imports.operations.*
import org.jetbrains.kotlin.psi.*

fun KtFile.guessTestFrameworkInUse(): TestFramework? {
// TODO consider    TestFrameworks.detectFramework()
    val imports: List<KtPsiImports.Kt> = ktPsiImports()
    return when {
        imports.isTestFramework(TestFramework.Junit4) -> TestFramework.Junit4
        imports.isTestFramework(TestFramework.Junit5) -> TestFramework.Junit5
        imports.isTestFramework(TestFramework.KotlinTest) -> TestFramework.KotlinTest
        else -> null
    }
}

private fun List<KtPsiImports>.isTestFramework(framework: TestFramework): Boolean {
    return any { it: KtPsiImports ->
        it.isForFqName(framework.testFqName)
    }
}