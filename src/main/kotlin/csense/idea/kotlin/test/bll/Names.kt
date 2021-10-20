package csense.idea.kotlin.test.bll

fun String.safeClassName(): String {
    return safeNameDecl()
}

fun String.safeFunctionName(): String {
    val safeName = safeNameDecl()
    if (safeName in dangerousFunctionNames) {
        return "${safeName}Test"
    }
    return safeName
}

private val dangerousFunctionNames = setOf(
    "hashCode",
    "toString"
)

fun String.safeNameDecl(): String {
    return replace("?", "")
        .replace("<", "")
        .replace(">", "")
        .replace(".", "")
        .replace(",", "")
        .replace(" ", "")
        .replace("*", "")
        .replace("+", "")
        .replace("-", "")
}

fun String.safeDecapitizedFunctionName(): String {
    return decapitalize().safeFunctionName()
}