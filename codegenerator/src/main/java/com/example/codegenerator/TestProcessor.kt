package com.example.codegenerator

import com.example.codegenerator.TestProcessor.Companion.KAPT_KOTLIN_GENERATED_OPTION_NAME
import com.example.lib.JsonObject
import com.google.gson.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.util.SimpleElementVisitor8
import javax.tools.Diagnostic
import kotlin.reflect.KClass

@SupportedAnnotationTypes("com.example.lib.JsonObject")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions(KAPT_KOTLIN_GENERATED_OPTION_NAME, "isTest")
class TestProcessor : AbstractProcessor() {
    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }

    override fun process(
        elements: MutableSet<out TypeElement>,
        roundEnvironment: RoundEnvironment
    ): Boolean {
        if (roundEnvironment.processingOver()) return true
        roundEnvironment
            .getElementsAnnotatedWith(elements.first())
            .forEach {
                processElement(it)
            }
        return true
    }

    private fun processElement(element: Element) {
        val stringType = processingEnv.elementUtils.getTypeElement(String::class.java.canonicalName)
        if (element.asType() != stringType.asType()) return
        element.accept(ConstVisitor(), element.getAnnotation(JsonObject::class.java).name)
    }

    inner class ConstVisitor : SimpleElementVisitor8<Unit, String>() {

        override fun visitVariable(p0: VariableElement?, name: String) {
            p0?.constantValue?.let {
                processModelGeneration(
                    it as String,
                    name,
                    processingEnv.elementUtils.getPackageOf(p0).toString()
                )
            }
            super.visitVariable(p0, name)
        }

        private fun processModelGeneration(json: String, name: String, pkg: String) {
            val parsedObject = JsonParser.parseString(json)

            val file = FileSpec.builder(
                pkg,
                name
            )
            val jsonClass = TypeSpec
                .classBuilder(name)
                .addModifiers(KModifier.DATA)
            val constructor = FunSpec.constructorBuilder()
            val properties = parsedObject.asJsonObject
                .entrySet()
                .map {
                    val propertyType = getType(it.value)
                    constructor.addParameter(it.key, propertyType)
                    PropertySpec
                        .builder(it.key, propertyType)
                        .initializer(it.key)
                        .build()
                }
            jsonClass.primaryConstructor(constructor.build())
            jsonClass.addProperties(properties)
            file
                .addType(jsonClass.build())
                .build()
                .writeTo(processingEnv.filer)
        }

        private fun getType(jsonElement: JsonElement): TypeName {
            return if (jsonElement.isJsonPrimitive) {
                getPrimitiveType(jsonElement.asJsonPrimitive).asTypeName()
            } else {
                try {
                    val jsonArray = jsonElement.asJsonArray
                    if (jsonArray.size() == 0) emptyArray<Any>()::class.asTypeName()
                    else
                        List::class.parameterizedBy(
                            getPrimitiveType(jsonElement.asJsonArray.first().asJsonPrimitive!!)
                        )
                } catch (ex: Exception) {
                    processingEnv.messager.printMessage(
                        Diagnostic.Kind.ERROR,
                        "Cannot generate object yet"
                    )
                    Any::class.asTypeName()
                }
            }
        }

        private fun getPrimitiveType(jsonPrimitive: JsonPrimitive): KClass<*> {
            return when {
                jsonPrimitive.isBoolean -> Boolean::class
                jsonPrimitive.isNumber -> Number::class
                jsonPrimitive.isString -> String::class
                else -> Any::class
            }
        }
    }

    private fun isTest(): Boolean? {
        return processingEnv.options["isTest"]?.toBoolean()
    }
}