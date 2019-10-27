package csense.idea.kotlin.test.quickfixes

import com.intellij.codeInspection.*
import com.intellij.openapi.module.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import csense.idea.kotlin.test.bll.*
import csense.kotlin.extensions.*
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.*


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
        val file = dir.createFile(fileName)
        file.add(newClass.createPackageDirective(ktFile.packageFqName))
        file.add(newClass.createClass("class $className{\n}"))
        return
    }

}

fun PsiDirectory.createPackageFolders(packageName: String): PsiDirectory? = tryAndLog {
    val names = packageName.split('.')
    var dir = this
    names.forEach {
        dir = dir.findSubdirectory(it) ?: dir.createSubdirectory(it)
    }
    return dir
}