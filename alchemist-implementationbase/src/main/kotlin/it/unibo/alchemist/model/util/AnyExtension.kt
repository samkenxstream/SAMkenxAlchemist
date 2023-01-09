/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.util

import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Position
import java.awt.geom.Point2D

/**
 * Collection of extensions that apply to [Any] object.
 */
object AnyExtension {
    /**
     * The opposite of [fold].
     *
     * @param extractor A function that provides a sequence of
     * elements given a specific element of the same type.
     * @receiver The starting element to unfold.
     * @return A sequence of [E] generated by unfolding on each
     * element provided by the [extractor] function.
     *
     * @See [fold].
     */
    fun <E> E.unfold(extractor: (E) -> Sequence<E>): Sequence<E> =
        sequenceOf(this) + extractor(this).flatMap { it.unfold(extractor) }

    /**
     * Tries to convert a concentration [T] into a valid position of type [P] descriptor.
     * Types are bound to the [environment] types.
     */
    inline fun <T, reified P : Position<P>> T.toPosition(environment: Environment<T, P>): P = when (this) {
        is P -> this
        is Iterable<*> -> {
            val numbers = this.map {
                when (it) {
                    is Number -> it
                    else ->
                        error(
                            "The Iterable '$this' being converted to position must contain only Numbers, " +
                                "but element '$it' has type ${it?.javaClass?.name ?: "null"}"
                        )
                }
            }
            environment.makePosition(*numbers.toTypedArray())
        }
        is Point2D -> {
            check(environment.dimensions == 2) {
                "Cannot convert the bidimensional Point2D ($x, $y) to a ${environment.dimensions}-D position"
            }
            environment.makePosition(x, y)
        }
        else ->
            throw IllegalArgumentException(
                "$this (type: ${if (this is Any) this.javaClass else null}) can't get converted to a Position"
            )
    }
}
