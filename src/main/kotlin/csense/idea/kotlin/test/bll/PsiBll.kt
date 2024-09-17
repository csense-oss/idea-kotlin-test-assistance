package csense.idea.kotlin.test.bll

import csense.idea.base.bll.kotlin.*
import org.jetbrains.kotlin.psi.*


fun KtClassOrObject.namedClassOrObject(): KtClassOrObject? {
    if (!isCompanion()) {
        return this
    }
    return selectParentByOrNull { it: KtElement ->
        it as? KtClassOrObject
    }
}
