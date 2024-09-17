package csense.idea.kotlin.test.bll.psi

import com.intellij.psi.*
import csense.idea.base.bll.kotlin.*
import csense.idea.kotlin.test.bll.search.*
import org.jetbrains.kotlin.psi.*

fun KtClassOrObject.hasTestFor(name: String): Boolean {
    return getAllFunctions().any { it: KtNamedFunction ->
        it.name == name
    }
}

fun KtClassOrObject.getTests(): List<PsiElement> {
    val containingKtFile: KtFile = containingKtFile
    val testFile: KtFile = TestSearch.findTestFileBy(containingKtFile) ?: return emptyList()
    val classTestNames: Collection<String> = this.name?.computeTestNames() ?: emptyList()
    return testFile.findTestByNameOrNull(
        fnNames = classTestNames.toSet(),
        orgFile = containingKtFile,
        orgClass = this
    )
}