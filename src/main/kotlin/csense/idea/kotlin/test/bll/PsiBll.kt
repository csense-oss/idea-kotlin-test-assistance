package csense.idea.kotlin.test.bll

import com.intellij.psi.util.*
import csense.idea.base.bll.kotlin.*
import org.jetbrains.kotlin.psi.*


fun KtClassOrObject.namedClassOrObject(): KtClassOrObject {
    return if (isCompanion()) {
        parentsWithSelf.firstOrNull {
            it is KtClassOrObject
        } as? KtClassOrObject ?: this
    } else {
        this
    }
}
