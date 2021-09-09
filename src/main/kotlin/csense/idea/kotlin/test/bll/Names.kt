package csense.idea.kotlin.test.bll

fun String.safeClassName(): String {
    return safeNameDecl()
}

fun String.safeFunctionName(): String {
    return safeNameDecl()
}

fun String.safeNameDecl():String{
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