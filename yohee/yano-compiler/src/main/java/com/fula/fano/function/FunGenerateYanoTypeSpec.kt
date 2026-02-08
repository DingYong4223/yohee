package com.fula.fano.function

import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.Modifier

object FunGenerateYanoTypeSpec : (List<TypeSpec>) -> TypeSpec {

    private const val CLASS_NAME = "YanoGenerator"

    override fun invoke(typeSpecs: List<TypeSpec>): TypeSpec {
        val yanoConstructorBuilder = MethodSpec
                .constructorBuilder()
                .addModifiers(Modifier.PRIVATE)

        val yanoTypeSpecBuilder = TypeSpec
                .classBuilder(CLASS_NAME)
                .addModifiers(Modifier.FINAL)
                .addModifiers(Modifier.PUBLIC)
                .addMethod(yanoConstructorBuilder.build())

        typeSpecs.forEach { yanoTypeSpecBuilder.addType(it) }

        return yanoTypeSpecBuilder.build()
    }

}