/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.test

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.core.implementations.Engine
import it.unibo.alchemist.loader.InitializedEnvironment
import it.unibo.alchemist.loader.LoadAlchemist
import it.unibo.alchemist.loader.export.exporters.CSVExporter
import it.unibo.alchemist.loader.export.exporters.GlobalExporter
import it.unibo.alchemist.model.interfaces.Position
import org.junit.jupiter.api.Assertions.assertNotNull
import org.kaikikm.threadresloader.ResourceLoader
import java.io.File

class TestCSVExporter<T, P : Position<P>> : StringSpec({
    "test exporting data on CSV file" {
        val file = ResourceLoader.getResource("testCSVExporter.yml")
        assertNotNull(file)
        val loader = LoadAlchemist.from(file)
        assertNotNull(loader)
        val initialized: InitializedEnvironment<T, P> = loader.getDefault()
        val simulation = Engine(initialized.environment)
        simulation.addOutputMonitor(GlobalExporter(initialized.exporters))
        simulation.play()
        simulation.run()
        initialized.exporters.size shouldBe 1
        val exporter = initialized.exporters.first()
        require(exporter is CSVExporter) {
            "Invalid exporter type '${exporter::class.simpleName}'"
        }
        val outputFile = File(exporter.exportPath)
            .listFiles()
            ?.find { it.name.startsWith("00-testing_csv_export_") && it.extension == exporter.fileExtension }
        outputFile.shouldNotBeNull()
        outputFile.shouldExist()
    }
})