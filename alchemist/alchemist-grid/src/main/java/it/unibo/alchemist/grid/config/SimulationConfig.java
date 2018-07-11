/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
package it.unibo.alchemist.grid.config;

import java.io.Serializable;
import java.util.Map;

/**
 * Simulation's configs for only one simulation.
 *
 */
public interface SimulationConfig extends Serializable {
    /**
     * 
     * @return Simulation's inizialization variables
     */
    Map<String, ? extends Serializable> getVariables();
    @Override
    boolean equals(Object obj);
    @Override
    int hashCode();
}
