/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/

/**
 * 
 */
package it.unibo.alchemist.model.implementations.reactions;

import it.unibo.alchemist.model.interfaces.Action;
import it.unibo.alchemist.model.interfaces.Condition;
import it.unibo.alchemist.model.interfaces.Context;
import it.unibo.alchemist.model.interfaces.Dependency;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.Time;
import it.unibo.alchemist.model.interfaces.TimeDistribution;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.danilopianini.util.Hashes;
import org.danilopianini.util.LinkedListSet;
import org.danilopianini.util.ListSet;
import org.danilopianini.util.ListSets;

/**
 * The type which describes the concentration of a molecule
 * 
 * This class offers a partial implementation of Reaction. In particular, it
 * allows to write new reaction specifying only which distribution time to adopt
 * 
 * @param <T>
 */
public abstract class AbstractReaction<T> implements Reaction<T> {

    /**
     * How bigger should be the StringBuffer with respect to the previous
     * interaction.
     */
    private static final byte MARGIN = 20;
    /**
     * Separators for toString.
     */
    protected static final String SEP1 = " -", SEP2 = "-> ";
    private static final long serialVersionUID = 1L;
    private final int hash;
    private List<? extends Action<T>> actions = new ArrayList<Action<T>>(0);
    private List<? extends Condition<T>> conditions = new ArrayList<Condition<T>>(0);
    private Context incontext = Context.LOCAL, outcontext = Context.LOCAL;
    private ListSet<Dependency> outbound = new LinkedListSet<>();
    private ListSet<Dependency> inbound = new LinkedListSet<>();
    private int stringLength = Byte.MAX_VALUE;
    private final TimeDistribution<T> dist;
    private final Node<T> node;

    /**
     * Builds a new reaction, starting at time t.
     * 
     * @param n
     *            the node this reaction belongs to
     * @param pd
     *            the time distribution this reaction should follow
     */
    public AbstractReaction(final Node<T> n, final TimeDistribution<T> pd) {
        hash = Hashes.hash32(n.hashCode(), n.getChemicalSpecies(), n.getReactions().size());
        dist = pd;
        node = n;
    }

    /**
     * Allows subclasses to add influenced molecules.
     *
     * @param m
     *            the influenced molecule
     */
    protected void addInfluencedMolecule(final Molecule m) {
        outbound.add(m);
    }

    /**
     * Allows subclasses to add influencing molecules.
     *
     * @param m
     *            the molecule to add
     */
    protected void addInfluencingMolecule(final Molecule m) {
        inbound.add(m);
    }

    @Override
    public boolean canExecute() {
        if (conditions == null) {
            return true;
        }
        int i = 0;
        while (i < conditions.size() && conditions.get(i).isValid()) {
            i++;
        }
        return i == conditions.size();
    }

    @Override
    public int compareTo(final Reaction<T> o) {
        return getTau().compareTo(o.getTau());
    }

    @Override
    public final boolean equals(final Object o) {
        return this == o;
    }

    @Override
    public void execute() {
        for (final Action<T> a : actions) {
            a.execute();
        }
    }

    @Override
    public List<Action<T>> getActions() {
        return Collections.unmodifiableList(actions);
    }

    @Override
    public List<Condition<T>> getConditions() {
        return Collections.unmodifiableList(conditions);
    }

    @Override
    public ListSet<Dependency> getInfluencedMolecules() {
        return optionallyImmodifiableView(outbound);
    }

    @Override
    public ListSet<Dependency> getInfluencingMolecules() {
        return optionallyImmodifiableView(inbound);
    }

    @Override
    public Context getInputContext() {
        return incontext;
    }

    @Override
    public Context getOutputContext() {
        return outcontext;
    }

    /**
     * @return a {@link String} representation of the rate
     */
    protected String getRateAsString() {
        return Double.toString(dist.getRate());
    }

    /**
     * This method is used to provide a reaction name in toString().
     *
     * @return the name for this reaction.
     */
    protected String getReactionName() {
        return getClass().getSimpleName();
    }

    @Override
    public Time getTau() {
        return dist.getNextOccurence();
    }

    @Override
    public final TimeDistribution<T> getTimeDistribution() {
        return dist;
    }

    @Override
    public final int hashCode() {
        return hash;
    }

    @Override
    public void initializationComplete(final Time t, final Environment<T, ?> env) { }

    /**
     * This method provides facility to clone reactions. Given a constructor in
     * form of a {@link Supplier}, it populates the actions and conditions with
     * cloned version of the ones registered in this reaction.
     *
     * @param builder
     *            the supplier
     *
     * @param <R>
     *            The reaction type
     * @return the populated cloned reaction
     */
    protected <R extends Reaction<T>> R makeClone(final Supplier<R> builder) {
        final R res = builder.get();
        final Node<T> n = res.getNode();
        final ArrayList<Condition<T>> c = new ArrayList<Condition<T>>(conditions.size());
        for (final Condition<T> cond : getConditions()) {
            c.add(cond.cloneCondition(n, res));
        }
        final ArrayList<Action<T>> a = new ArrayList<Action<T>>(actions.size());
        for (final Action<T> act : getActions()) {
            a.add(act.cloneAction(n, res));
        }
        res.setActions(a);
        res.setConditions(c);
        return res;
    }

    @Override
    public void setActions(final List<Action<T>> a) {
        actions = Objects.requireNonNull(a, "The actions list can't be null");
        Context lessStrict = Context.LOCAL;
        outbound = new LinkedListSet<>();
        for (final Action<T> act : actions) {
            final Context condcontext = Objects.requireNonNull(act, "Actions can't be null").getContext();
            lessStrict = lessStrict.isMoreStrict(condcontext) ? condcontext : lessStrict;
            final List<? extends Dependency> mod = act.getOutboundDependencies();
            /*
             * This check is needed because of the meaning of a null list of
             * modified molecules: it means that the reaction will influence
             * every other reaction. This must be managed directly by the
             * dependency graph, and consequently the whole reaction must have a
             * null list of modified molecules.
             */
            if (mod != null) {
                outbound.addAll(mod);
            } else {
                outbound = null;
                break;
            }
        }
        setOutputContext(lessStrict);
    }

    @Override
    public void setConditions(final List<Condition<T>> c) {
        conditions = c;
        Context lessStrict = Context.LOCAL;
        inbound = new LinkedListSet<>();
        for (final Condition<T> cond : conditions) {
            final Context condcontext = cond.getContext();
            lessStrict = lessStrict.isMoreStrict(condcontext) ? condcontext : lessStrict;
            final ListSet<? extends Dependency> mod = cond.getInboundDependencies();
            /*
             * This check is needed because of the meaning of a null list of
             * modified molecules: it means that the reaction will influence
             * every other reaction. This must be managed directly by the
             * dependency graph, and consequently the whole reaction must have a
             * null list of modified molecules.
             */
            if (mod != null) {
                inbound.addAll(mod);
            } else {
                inbound = null;
                break;
            }
        }
        setInputContext(lessStrict);
    }

    /**
     * Used by sublcasses to set their input context.
     * 
     * @param c
     *            the new input context
     */
    protected void setInputContext(final Context c) {
        incontext = c;
    }

    /**
     * Used by sublcasses to set their output context.
     * 
     * @param c
     *            the new input context
     */
    protected void setOutputContext(final Context c) {
        outcontext = c;
    }

    @Override
    public String toString() {
        final StringBuilder tot = new StringBuilder(stringLength + MARGIN);
        tot.append(getReactionName());
        tot.append('@');
        tot.append(getTau());
        tot.append(getConditions().toString());
        tot.append('-');
        tot.append(getRateAsString());
        tot.append("->");
        tot.append(getActions().toString());
        stringLength = tot.length();
        return tot.toString();
    }

    @Override
    public final void update(final Time curTime, final boolean executed, final Environment<T, ?> env) {
        updateInternalStatus(curTime, executed, env);
        dist.update(curTime, executed, getRate(), env);
    }

    /**
     * This method gets called as soon as
     * {@link #update(Time, boolean, Environment)} is called. It is useful to
     * update the internal status of the reaction.
     * 
     * @param curTime
     *            the current simulation time
     * @param executed
     *            true if this reaction has just been executed, false if the
     *            update has been triggered due to a dependency
     * @param env
     *            the current environment
     */
    protected abstract void updateInternalStatus(Time curTime, boolean executed, Environment<T, ?> env);

    /**
     * @param influenced
     *            the new influenced molecules. Can be null.
     */
    @SuppressWarnings("unchecked")
    protected void setInfluencedMolecules(final ListSet<? extends Dependency> influenced) {
        this.outbound = (ListSet<Dependency>) influenced;
    }

    /**
     * @param influencing
     *            the new influencing molecules. Can be null.
     */
    @SuppressWarnings("unchecked")
    protected void setInfluencingMolecules(final ListSet<? extends Dependency> influencing) {
        this.inbound = (ListSet<Dependency>) influencing;
    }

    @Override
    public Node<T> getNode() {
        return node;
    }


    private static <E> ListSet<E> optionallyImmodifiableView(ListSet<E> in) {
        return in == null ? null : ListSets.unmodifiableListSet(in);
    }
}
