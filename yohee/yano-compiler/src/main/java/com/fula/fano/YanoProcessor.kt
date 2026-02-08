package com.fula.fano

import com.fula.fano.extensions.doOnNext
import com.fula.fano.filter.FilterNonExistentFile
import com.fula.fano.filter.FilterSupportedElement
import com.fula.fano.function.*
import com.fula.fano.options.OPTION_PROJECT_PATH
import com.fula.fano.source.SrcYanoElement
import com.fula.fano.utils.UtilFileGen
import com.fula.fano.utils.UtilMessager
import com.google.auto.service.AutoService
import java.nio.file.Paths
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

@AutoService(Processor::class)
class YanoProcessor : AbstractProcessor() {

    private var isProcessed = false

    override fun getSupportedAnnotationTypes() = mutableSetOf(file2string::class.java.name)

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latestSupported()

    override fun getSupportedOptions() = mutableSetOf(OPTION_PROJECT_PATH)

    override fun init(processingEnvironment: javax.annotation.processing.ProcessingEnvironment) {
        super.init(processingEnvironment)
        UtilMessager.messager = processingEnvironment.messager
        UtilFileGen.filer = processingEnvironment.filer
    }

    override fun process(set: Set<TypeElement>, roundEnvironment: RoundEnvironment): Boolean {
        if (isProcessed) {
            return true
        }

        isProcessed = true

        val projectRoot = processingEnv.options[OPTION_PROJECT_PATH] ?: Paths.get("").toAbsolutePath().toString()

        UtilMessager.reportInfo("Starting processing")

        SrcYanoElement(roundEnvironment)
                .createElementStream()
                .filter(FilterSupportedElement)
                .map(ElementToTypeAndFilePairFunction(projectRoot))
                .filter(FilterNonExistentFile)
                .doOnNext { typeElementFileEntry ->
                    UtilMessager.reportInfo("Processing file: ${typeElementFileEntry.second}")
                }
                .map(FunFileToString)
                .map(FunGenerateFileStreamTypeSpec)
                .toList()
                .run(FunGenerateYanoTypeSpec)
                .run(FunTypeSpecToJavaFile)
                .run { UtilFileGen.writeToDisk(this) }
                .run { UtilMessager.reportInfo("File successfully processed") }
        return true
    }
}
