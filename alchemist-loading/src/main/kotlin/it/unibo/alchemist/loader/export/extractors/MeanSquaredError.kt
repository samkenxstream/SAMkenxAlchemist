/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.loader.export.extractors

import it.unibo.alchemist.loader.export.Extractor
import it.unibo.alchemist.loader.export.StatUtil
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Incarnation
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.Time
import org.apache.commons.math3.stat.descriptive.UnivariateStatistic

/**
 * Exports the Mean Squared Error for the concentration of some molecule, given
 * another molecule that carries the correct result. The correct value is
 * extracted from every node, then the provided {@link UnivariateStatistic} is
 * applied to get a single, global correct value. Then, the actual value is
 * extracted from every node, its value is compared (subtracted) to the computed
 * correct value, it gets squared, and then logged.
 *
 * @param <T> concentration type
 */
class MeanSquaredError<T> @JvmOverloads constructor(
    incarnation: Incarnation<T, *>,
    localCorrectValueMolecule: String,
    localCorrectValueProperty: String = "",
    statistics: String,
    localValueMolecule: String,
    localValueProperty: String = "",
) : Extractor<Double> {

    private val statistic: UnivariateStatistic = StatUtil.makeUnivariateStatistic(statistics)
        .orElseThrow { IllegalArgumentException("Could not create univariate statistic $statistics") }
    private val mReference: Molecule = incarnation.createMolecule(localCorrectValueMolecule)
    private val pReference: String = localCorrectValueProperty
    private val pActual: String = localValueProperty
    private val mActual: Molecule = incarnation.createMolecule(localValueMolecule)
    private val name: String = with(StringBuilder("MSE(")) {
        append(statistics)
        append('(')
        if (pReference.isNotEmpty()) {
            append(pReference).append('@')
        }
        append(localCorrectValueMolecule).append("),")
        if (pActual.isNotEmpty()) {
            append(pActual).append('@')
        }
        append(localValueMolecule).append(')')
        toString()
    }
    override val columnNames = listOf(name)

    override fun <T> extractData(
        environment: Environment<T, *>,
        reaction: Reaction<T>?,
        time: Time,
        step: Long
    ): Map<String, Double> {
        val incarnation: Incarnation<T, *> = environment.incarnation
            .orElseThrow { IllegalStateException("No incarnation available in the environment!") }
        val value: Double = statistic.evaluate(
            environment.nodes.map { incarnation.getProperty(it, mReference, pReference) }.toDoubleArray()
        )
        val mse: Double = environment.nodes.parallelStream()
            .mapToDouble { incarnation.getProperty(it, mActual, pActual) - value }
            .map { it * it }
            .average()
            .orElse(Double.NaN)
        return mapOf(name to mse)
    }
}