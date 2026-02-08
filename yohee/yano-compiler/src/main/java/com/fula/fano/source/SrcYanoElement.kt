package com.fula.fano.source

import com.fula.fano.file2string
import javax.annotation.processing.RoundEnvironment

/**
 * An element source that provides a stream of elements annotated with the [file2string] annotation.
 */
class SrcYanoElement(private val roundEnvironment: RoundEnvironment) {

    /**
     * A stream of elements.
     *
     * @return a valid set of elements.
     */
    fun createElementStream() = roundEnvironment.getElementsAnnotatedWith(file2string::class.java).asSequence()

}
