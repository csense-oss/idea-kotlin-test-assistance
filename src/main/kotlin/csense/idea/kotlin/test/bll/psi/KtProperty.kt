package csense.idea.kotlin.test.bll.psi

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import csense.idea.base.bll.kotlin.*
import csense.idea.base.bll.platform.*
import csense.idea.base.module.*
import csense.idea.kotlin.test.bll.*
import csense.idea.kotlin.test.bll.search.*
import csense.kotlin.extensions.primitives.*
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.*

fun KtProperty.getTestedMethod(): List<PsiElement> {
    val parent: KtClassOrObject? = containingClassOrObject?.namedClassOrObject()
    val containingFile: KtFile = containingKtFile
    if (parent != null && parent.isAnonymous()) {
        return emptyList()
    }

    val testModule: VirtualFile = TestInformationCache.lookupModuleTestSourceRoot(containingFile)
        ?: return emptyList()
    val resultingDirectory: PsiDirectory? = testModule.toPsiDirectory(project)?.findPackageDir(containingFile)
    val testFile: KtFile = resultingDirectory?.findTestFile(containingFile) ?: return emptyList()
    val namesToLookAt: Set<String> = computeViableNames()
    return testFile.findTestByNameOrNull(
        fnNames = namesToLookAt,
        orgFile = containingFile,
        orgClass = parent
    )
}

fun KtProperty.computeViableNames(): Set<String> {
    val safeName: String = name ?: ""
    val extensionNames: List<String> = if (isExtensionDeclaration()) {
        val extName: String? = receiverTypeReference?.text?.safeDecapitizedFunctionName()
        return setOfNotNull(extName + safeName.titleCaseFirstWord())
    } else {
        listOf()
    }
    return setOf(safeName) + extensionNames
}

fun KtProperty.computeMostPreciseName(): String {
    return if (isExtensionDeclaration()) {
        val extensionName: String? = receiverTypeReference?.text?.safeDecapitizedFunctionName()
        extensionName?.plus(name?.titleCaseFirstWord() ?: "") ?: name ?: ""
    } else {
        name ?: ""
    }
}

