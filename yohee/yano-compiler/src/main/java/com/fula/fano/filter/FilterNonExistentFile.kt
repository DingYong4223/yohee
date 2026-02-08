package com.fula.fano.filter

import com.fula.fano.options.OPTION_PROJECT_PATH
import com.fula.fano.utils.UtilMessager
import java.io.File
import javax.lang.model.element.TypeElement

/**
 * Filters out non existent files and reports an error.
 */
object FilterNonExistentFile : (Pair<TypeElement, File>) -> Boolean {

    override fun invoke(pair: Pair<TypeElement, File>): Boolean {
        val (element, file) = pair
        return file.exists().also {
            if (!it) {
                UtilMessager.reportError(
                        element,
                        "File does not exist at path: ${file.path}."
                                + "Note: if the path is not pointing to the root of your project, "
                                + "you probably need to pass project.rootDir to the "
                                + "$OPTION_PROJECT_PATH annotation processing option."
                )
            }
        }
    }

}