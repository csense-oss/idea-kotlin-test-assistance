package csense.idea.kotlin.test.quickfixes

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.intellij.util.IncorrectOperationException
import csense.idea.kotlin.test.bll.findKotlinRootDir
import csense.kotlin.extensions.tryAndLog
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtPsiFactory


class CreateTestFileQuickFix(
        val testModule: Module,
        val resultingDirectory: PsiDirectory?,
        val ktFile: KtFile) : LocalQuickFix {

    override fun getName(): String {
        return "Add test file"
    }

    override fun getFamilyName(): String {
        return this::class.java.simpleName
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val newClass = KtPsiFactory(project)
        val packageName = ktFile.packageFqName.asString()
        val className = ktFile.virtualFile.nameWithoutExtension + "Test"
        val fileName = "$className.kt"
        val dir: PsiDirectory = if (resultingDirectory != null) {
            resultingDirectory
        } else {
            val root = testModule.findKotlinRootDir() ?: return
            root.createPackageFolders(packageName) ?: return
        }
        try {
            val file = dir.createFile(fileName)
            file.add(newClass.createAnnotationEntry("@file:Suppress(\"unused\")"))
            file.add(newClass.createPackageDirective(ktFile.packageFqName))
            file.add(newClass.createClass("class $className{\n}"))
        } catch (e: IncorrectOperationException) {
            throw e //TODO make me.
        }
        return
    }

}

fun PsiDirectory.createPackageFolders(packageName: String): PsiDirectory? = tryAndLog {
    val names = packageName.split('.')
    var dir = this
    names.forEach {
        try {
            dir = dir.findSubdirectory(it) ?: dir.createSubdirectory(it)
        } catch (e: IncorrectOperationException) {
            //TODO handle.
            return@tryAndLog null
        }
    }
    return dir
}