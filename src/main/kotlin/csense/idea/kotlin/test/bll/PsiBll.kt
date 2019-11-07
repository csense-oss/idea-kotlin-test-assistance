package csense.idea.kotlin.test.bll

import com.intellij.psi.util.parentOfType
import csense.kotlin.extensions.mapLazy
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtClassOrObject

fun KtClassOrObject.namedClassOrObject(): KtClassOrObject = this.isCompanionObject().mapLazy(
        ifTrue = {
            this.parentOfType() ?: this
        },
        ifFalse = { this })

fun KtClassOrObject.isCompanionObject(): Boolean =
        hasModifier(KtTokens.COMPANION_KEYWORD)
