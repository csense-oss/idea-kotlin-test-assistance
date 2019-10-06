# Current things to consider
- multiple overloads with same arg names
```kotlin
fun String.magic(a: Int){

}
fun String.magic(a: Int, b: Int){

}
```
could be solved by stating that all parts of the name should be in fun name. 
```kotlin
@Test
fun magicA(){}
@Test
fun magicAB(){}
``` 

but it still does not solve
```kotlin
fun String.magic(a: Int, b: Char){

}
fun String.magic(a: Int, b: Int){

}
```

But at this point it might also be a signal that some of the code might be weird ? hmm

----------------------------
