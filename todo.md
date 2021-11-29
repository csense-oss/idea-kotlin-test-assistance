# Test (file/class) not matching any code
- inspection

# Test to code nav
- kinda relies on the test file / class not matching (inverse search)

# Handle class extensions test code generation

in short

```kotlin
    class X {
    fun String.test() {

    }
}

//test
    class XTest{
        class StringTest{
            @Test
            fun empty(){
                val clz = X()
                with(clz){
                    val result =  "".test()
                }
            }
            @Test
            fun whitespace(){
                //... etc
            }
        }
    }
```


# Better test code generation
    - eg strings (empty, whitespace, types of content etc) 
