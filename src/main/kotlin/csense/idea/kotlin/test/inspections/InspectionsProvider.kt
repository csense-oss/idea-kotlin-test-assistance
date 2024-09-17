package csense.idea.kotlin.test.inspections

import com.intellij.codeInspection.*
import csense.idea.kotlin.test.inspections.emptyTest.*
import csense.idea.kotlin.test.inspections.missingTestAnnotation.*
import csense.idea.kotlin.test.inspections.missingTestsForFunction.*
import csense.idea.kotlin.test.inspections.missingTestsForProperty.*

class InspectionsProvider : InspectionToolProvider {
    override fun getInspectionClasses(): Array<Class<out LocalInspectionTool>> {
        return arrayOf(
            EmptyTestInspection::class.java,
            MissingTestAnnotationInspection::class.java,
            MissingTestsForFunctionInspector::class.java,
            MissingTestsForClassInspector::class.java,
            MissingTestsForPropertyInspector::class.java
        )
    }
}