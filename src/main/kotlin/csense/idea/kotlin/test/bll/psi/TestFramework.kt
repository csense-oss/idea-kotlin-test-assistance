package csense.idea.kotlin.test.bll.psi

import csense.kotlin.extensions.*

sealed class TestFramework(
    val testFqName: String,
    val ignoreFqName: String,
) {
    data object Junit4 : TestFramework(
        testFqName = "org.junit.Test",
        ignoreFqName = "org.junit.Ignore"
    )

    data object Junit5 : TestFramework(
        testFqName = "org.junit.jupiter.api.Test",
        ignoreFqName = "org.junit.jupiter.api.Disabled"
    )

    data object KotlinTest : TestFramework(
        testFqName = "kotlin.test.Test",
        ignoreFqName = "kotlin.test.Ignore"
    )

    companion object {
        val allFrameworks: Set<TestFramework> by lazy {
            setOf(
                KotlinTest,
                Junit5,
                Junit4
            )
        }

        val allTestFqNames: Set<String> by lazy {
            allFrameworks.mapToSet { it: TestFramework ->
                it.testFqName
            }
        }
        val allIgnoreFqNames: Set<String> by lazy {
            allFrameworks.mapToSet { it: TestFramework ->
                it.ignoreFqName
            }
        }

    }
}