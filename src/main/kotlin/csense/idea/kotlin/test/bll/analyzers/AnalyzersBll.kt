package csense.idea.kotlin.test.bll.analyzers

import com.intellij.codeInspection.*
import com.intellij.psi.*


class AnalyzerError(
        val psiElement: PsiElement,
        val descriptionTemplate: String,
        val fixes: Array<LocalQuickFix>
)

fun ProblemsHolder.registerProblem(analyzeError: AnalyzerError) {
    registerProblem(analyzeError.psiElement, analyzeError.descriptionTemplate, *analyzeError.fixes)
}

interface Analyzer<T : PsiElement> {
    fun analyze(item: T): AnalyzerResult
}

class AnalyzerResult(val errors: List<AnalyzerError>) {
    companion object {
        val empty = AnalyzerResult(listOf())
    }
}