package csense.idea.kotlin.test.res

import com.intellij.openapi.util.*
import javax.swing.Icon

object Icons {
    val testIcon: Icon by lazy {
        IconLoader.getIcon("/icons/test_icon.svg", javaClass)
    }
}