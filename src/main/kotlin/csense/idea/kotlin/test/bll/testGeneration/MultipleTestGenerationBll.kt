package csense.idea.kotlin.test.bll.testGeneration

fun computeListTestCode(
    testName: String,
    typeName: String,
    functionInvocationPattern: FunctionInvocationPattern
): String {
    return """
        class ${testName.capitalize()} {
            @Test
            fun empty(){
                //TODO test empty condition here.
                ${functionInvocationPattern.toCode(listOf("listOf<$typeName>()"))}
            }
            
            @Test
            fun single(){
                //TODO test single element condition here.
                ${functionInvocationPattern.toCode(listOf("listOf<$typeName>($typeName())"))}
            }
            
            @Test
            fun multiple(){
                //TODO test multiple element condition here.
                ${
        functionInvocationPattern.toCode(
            listOf(
                "listOf<$typeName>($typeName(),$typeName())"
            )
        )
    }
            }
        }
        """
}
//
//fun computeEnumListTestCode() {
//
//}
//
//fun computeSealedClassListTestCode() {
//
//}