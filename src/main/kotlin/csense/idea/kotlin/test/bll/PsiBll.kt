package csense.idea.kotlin.test.bll

import com.intellij.psi.util.parents
import csense.idea.base.bll.kotlin.isCompanion
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtClassOrObject

fun KtClassOrObject.namedClassOrObject(): KtClassOrObject {
    return if (isCompanion()) {
        parents().firstOrNull {
            it is KtClassOrObject
        } as? KtClassOrObject ?: this
    } else {
        this
    }
}
