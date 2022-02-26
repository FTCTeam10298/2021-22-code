package us.brainstormz.localizer

class Box<T> {
    var insides:T? = null
}

fun foo(){
    val age = 41

    val myBox = Box<Int>()

    myBox.insides = age
}

fun boo(box:Box<Int>){
    print(box.insides?:0 + 3)
}

fun main() {
    println("hello world")
    val age = 41

    val myBox = Box<Int>()

    myBox.insides = age

    boo(myBox)
}
