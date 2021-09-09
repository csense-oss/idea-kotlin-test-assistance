package csense.idea.kotlin.test.quickfixes

import com.intellij.codeInspection.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.util.*
import csense.idea.base.module.*
import csense.idea.kotlin.test.bll.*
import org.jetbrains.kotlin.psi.*
import java.io.*


class CreateTestFileQuickFix(
    val rootDir: PsiDirectory,
    val resultingDirectory: PsiDirectory?,
    val ktFile: KtFile
) : LocalQuickFix {

    override fun getName(): String {
        return "Add test file"
    }

    override fun getFamilyName(): String {
        return this::class.java.simpleName
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val newClass = KtPsiFactory(project)
        val packageName = ktFile.packageFqName.asString()
        val className = ktFile.virtualFile.nameWithoutExtension.capitalize().safeClassName() + "Test"
        val fileName = ktFile.virtualFile.nameWithoutExtension.capitalize() + "Test.kt"
        val dir: PsiDirectory = resultingDirectory ?: (rootDir.createPackageFolders(packageName) ?: return)
        try {
            //if the file was already created
            if (dir.files.any { it.name == fileName }) {
                return
            }
            val file = dir.createFile(fileName)
//            file.add(newClass.createAnnotationEntry("@file:Suppress(\"unused\")")) //causes null ptr exceptions ?
            if (ktFile.packageFqName.asString().isNotEmpty()) {
                file.add(newClass.createPackageDirective(ktFile.packageFqName))
            }
            file.add(newClass.createClass("class $className{\n}"))
        } catch (e: IncorrectOperationException) {
            throw e
        } catch (e: IOException) {
            // we do not want to disturb the user, this is expected,
            // say the file exists and IDEA just have not updated the quickfix.
        }
        return
    }

}
