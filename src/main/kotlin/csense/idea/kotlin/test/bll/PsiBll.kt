package csense.idea.kotlin.test.bll

import com.intellij.psi.util.parents
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

fun KtClassOrObject.isCompanion(): Boolean = hasModifier(KtTokens.COMPANION_KEYWORD)

