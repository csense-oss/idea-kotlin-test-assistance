package csense.idea.kotlin.test.bll

fun computeListTestCode(testName: String): String {
    return """
        class ${testName.capitalize()} {
            @Test
            fun empty(){
                //TODO test empty condition here.
            }
            
            @Test
            fun single(){
                //TODO test single element condition here.
            }
            
            @Test
            fun multiple(){
                //TODO test multiple element condition here.
            }
        }
        """
}

fun computeEnumListTestCode() {

}

fun computeSealedClassListTestCode() {

}