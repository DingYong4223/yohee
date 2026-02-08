package com.fula.fano.filter

import com.fula.fano.utils.UtilMessager
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier

/**
 * Filters a list of elements for only the elements supported by yano. If unsupported elements
 * are in the stream, it will report a message to the [UtilMessager] so that the consumer knows
 * what they have done wrong.
 */
object FilterSupportedElement : (Element) -> Boolean {

    override fun invoke(classElement: Element): Boolean {

        if (classElement.kind != ElementKind.INTERFACE) {
            UtilMessager.reportError(classElement, "Only interfaces are supported")
            return false
        }

        if (!classElement.modifiers.contains(Modifier.PUBLIC)) {
            UtilMessager.reportError(classElement, "Only public interfaces are supported")
            return false
        }

        if (classElement.enclosedElements.size != 1) {
            UtilMessager.reportError(classElement, "Only interfaces with 1 method are supported")
            return false
        }

        val methodElement = classElement.enclosedElements[0]

        if (methodElement !is ExecutableElement) {
            UtilMessager.reportError(methodElement, "Interface must contain one method")
            return false
        }

        if (methodElement.returnType.toString() != String::class.java.name.replace('$', '.')) {
            UtilMessager.reportError(methodElement, "Interface's single method must have a String return type")
            return false
        }

        return true
    }

}
