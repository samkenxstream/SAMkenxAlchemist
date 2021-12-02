/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.loader.export

import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.Time

/**
 * A generic exporter for the simulation.
 * [T] is the concentration type,
 * [P] is the position type, and
 */
interface Exporter<T, P : Position<P>> {

    /**
     *  The List of [Extractor] used to export simulations data.
     */
    val dataExtractors: List<Extractor<*>>

    /**
     * Assign the list of [dataExtractors] to the selected [Exporter].
     */
    fun bindDataExtractors(dataExtractors: List<Extractor<*>>)

    /**
     * Assign the map of [variables] to the selected [Exporter].
     */
    fun bindVariables(variables: Map<String, *>)

    /**
     *  Prepare the export environment.
     *  This method is called only once upon simulation initialization.
     */
    fun setup(environment: Environment<T, P>)

    /**
     * Main method used by exporters to export data.
     * This method is called at each step of the simulation.
     */
    fun update(environment: Environment<T, P>, reaction: Reaction<T>?, time: Time, step: Long)

    /**
     * Close the export environment.
     * This method is called when the simulation finishes.
     */
    fun close(environment: Environment<T, P>, time: Time, step: Long)
}