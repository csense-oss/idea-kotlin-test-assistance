package csense.idea.kotlin.test.inspections

import com.intellij.codeInspection.*
import csense.idea.kotlin.test.inspections.emptyTest.*

class InspectionsProvider : InspectionToolProvider {
    override fun getInspectionClasses(): Array<Class<out LocalInspectionTool>> {
        return arrayOf(
            MissingTestsForFunctionInspector::class.java,
            MissingTestsForClassInspector::class.java,
            EmptyTestInspection::class.java,
            MissingTestsForPropertyInspector::class.java
        )
    }
}