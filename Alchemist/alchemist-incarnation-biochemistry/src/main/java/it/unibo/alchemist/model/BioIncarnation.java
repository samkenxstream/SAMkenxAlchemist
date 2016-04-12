/*
 * Copyright (C) 2010-2016, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model;

import org.apache.commons.math3.random.RandomGenerator;

import it.unibo.alchemist.model.implementations.molecules.Biomolecule;

import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.TimeDistribution;
import it.unibo.alchemist.model.interfaces.Action;
import it.unibo.alchemist.model.interfaces.Condition;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Incarnation;

/**
 */
public class BioIncarnation implements Incarnation<Double> {

    @Override
    public double getProperty(final Node<Double> node, final Molecule mol, final String prop) {
        return (Double) node.getConcentration(mol);
    }

    @Override
    public Molecule createMolecule(final String s) {
        return (Molecule) new Biomolecule(s);
    }

    @Override
    public Node<Double> createNode(final RandomGenerator rand, final Environment<Double> env, final String param) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TimeDistribution<Double> createTimeDistribution(final RandomGenerator rand, final Environment<Double> env,
            final Node<Double> node, final String param) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Reaction<Double> createReaction(final RandomGenerator rand, final Environment<Double> env, final Node<Double> node,
            final TimeDistribution<Double> time, final String param) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Condition<Double> createCondition(final RandomGenerator rand, final Environment<Double> env, final Node<Double> node,
            final TimeDistribution<Double> time, final Reaction<Double> reaction, final String param) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Action<Double> createAction(final RandomGenerator rand, final Environment<Double> env, final Node<Double> node,
            final TimeDistribution<Double> time, final Reaction<Double> reaction, final String param) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Double createConcentration(final String s) {
        // TODO Auto-generated method stub
        return null;
    }
}
