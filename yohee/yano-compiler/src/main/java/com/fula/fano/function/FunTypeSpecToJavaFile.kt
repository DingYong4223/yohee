package com.fula.fano.function

import com.fula.fano.function.FunTypeSpecToJavaFile.PACKAGE_NAME
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeSpec

/**
 * Generates a [JavaFile] from a [TypeSpec], [PACKAGE_NAME] will be used as the package name.
 */
object FunTypeSpecToJavaFile : (TypeSpec) -> JavaFile {

    private const val PACKAGE_NAME = "com.fula.fano"

    override fun invoke(typeSpec: TypeSpec): JavaFile =
            JavaFile.builder(PACKAGE_NAME, typeSpec).build()

}