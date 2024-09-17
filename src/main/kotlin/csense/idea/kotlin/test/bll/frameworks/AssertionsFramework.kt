package csense.idea.kotlin.test.bll.frameworks

sealed class AssertionsFramework {
    data object Csense : AssertionsFramework()
}