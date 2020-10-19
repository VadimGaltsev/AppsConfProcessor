package com.example.myapplication.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.myapplication.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val firstVisitor = FirstVisitor()
        val secondVisitor = SecondVisitor()
        val firstElement = FirstElement()
        val secondElement = SecondElement()
        secondElement.accept(firstVisitor)
        secondElement.accept(secondVisitor)
        firstElement.accept(firstVisitor)
        firstElement.accept(secondVisitor)
    }
}


interface Visitor {

    fun visit(element: FirstElement)

    fun visit(element: SecondElement)
}

class FirstVisitor : Visitor {

    override fun visit(element: SecondElement) {
        println("FirstVisitor visits second $element")

    }

    override fun visit(element: FirstElement) {
        println("FirstVisitor visits first $element")
    }
}

class SecondVisitor : Visitor {

    override fun visit(element: SecondElement) {
        println("SecondVisitor visits second $element")

    }

    override fun visit(element: FirstElement) {
        println("SecondVisitor visits first $element")
    }
}

interface Element {

    fun accept(visitor: Visitor)
}

class FirstElement : Element {

    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}

class SecondElement : Element {

    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}