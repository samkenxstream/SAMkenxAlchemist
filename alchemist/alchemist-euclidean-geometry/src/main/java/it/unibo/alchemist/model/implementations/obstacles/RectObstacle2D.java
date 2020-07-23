/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.obstacles;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.model.interfaces.Obstacle2D;
import it.unibo.alchemist.model.interfaces.geometry.Vector2D;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.MathArrays;
import org.jetbrains.annotations.NotNull;

import java.awt.geom.Rectangle2D;

import static org.apache.commons.math3.util.FastMath.max;
import static org.apache.commons.math3.util.FastMath.min;
import static org.danilopianini.lang.MathUtils.closestTo;
import static org.danilopianini.lang.MathUtils.fuzzyEquals;
import static org.danilopianini.lang.MathUtils.fuzzyGreaterEquals;

/**
 * This class implements a rectangular obstacle, whose sides are parallel to the
 * cartesian axis.
 *
 * @param <V>
 */
@SuppressFBWarnings("EQ_DOESNT_OVERRIDE_EQUALS")
public final class RectObstacle2D<V extends Vector2D<V>> extends Rectangle2D.Double implements Obstacle2D<V> {

    /**
     * Relative precision value under which two double values are considered to
     * be equal by fuzzyEquals.
     */
    private static final long serialVersionUID = -3552947311155196461L;
    private final int id = System.identityHashCode(this);
    private final double minX, maxX, minY, maxY;

    /*
     * This code was built upon Alexander Hristov's, see:
     *
     * http://www.ahristov.com/tutorial/geometry-games/intersection-segments.html
     */
    private static double[] intersection(final double x1, final double y1, final double x2, final double y2, final double x3, final double y3, final double x4, final double y4) {
        final double d = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
        if (d == 0) {
            return new double[] { x2, y2 };
        }
        /*
         * Intersection point between lines
         */
        double xi = ((x3 - x4) * (x1 * y2 - y1 * x2) - (x1 - x2) * (x3 * y4 - y3 * x4)) / d;
        double yi = ((y3 - y4) * (x1 * y2 - y1 * x2) - (y1 - y2) * (x3 * y4 - y3 * x4)) / d;
        /*
         * If a point is on a border, reduce it to the exact border
         */
        xi = fuzzyEquals(xi, x3) ? x3 : fuzzyEquals(xi, x4) ? x4 : xi;
        yi = fuzzyEquals(yi, y3) ? y3 : fuzzyEquals(yi, y4) ? y4 : yi;
        /*
         * Check if there is actual intersection
         */
        if (intersectionOutOfRange(xi, x1, x2) || intersectionOutOfRange(xi, x3, x4) || intersectionOutOfRange(yi, y1, y2) || intersectionOutOfRange(yi, y3, y4)) {
            return new double[] { x2, y2 };
        }
        return new double[] { xi, yi };
    }

    private static boolean intersectionOutOfRange(final double intersection, final double start, final double end) {
        final double min = Math.min(start, end);
        final double max = Math.max(start, end);
        return !fuzzyGreaterEquals(intersection, min) || !fuzzyGreaterEquals(max, intersection);
    }

    @NotNull
    @Override
    public V next(@NotNull final V start, @NotNull final V end) {
        final double startx = start.getX();
        final double starty = start.getY();
        final double endx = end.getX();
        final double endy = end.getY();
        final double[] onBorders = enforceBorders(startx, starty, endx, endy);
        if (onBorders != null) {
            /*
             * The starting point was on the border.
             */
            return start.newFrom(onBorders[0], onBorders[1]);
        }
        final double[] intersection = nearestIntersection(start, end).getCoordinates();
        /*
         * Ensure the intersection is outside the boundaries. Force it to be.
         */
        while (contains(intersection[0], intersection[1])) {
            intersection[0] = FastMath.nextAfter(intersection[0], startx);
            intersection[1] = FastMath.nextAfter(intersection[1], starty);
        }
        final double[] restricted = enforceBorders(intersection[0], intersection[1], intersection[0], intersection[1]);
        if (restricted == null) {
            return start.newFrom(intersection[0], intersection[1]);
        }
        return start.newFrom(restricted[0], restricted[1]);
    }

    @SuppressFBWarnings("PZLA_PREFER_ZERO_LENGTH_ARRAYS")
    private double[] enforceBorders(final double startx, final double starty, final double endx, final double endy) {
        /*
         * Check if the point is somehow inside the obstacle, and reply
         * accordingly
         */
        if (fuzzyGreaterEquals(starty, minY)
                && fuzzyGreaterEquals(maxY, starty)
                && fuzzyGreaterEquals(startx, minX)
                && fuzzyGreaterEquals(maxX, startx)) {
            final double[] res = { endx, endy };
            final boolean startIsVertex = (fuzzyEquals(startx, minX) || fuzzyEquals(startx, maxX))
                    && (fuzzyEquals(starty, minY) || fuzzyEquals(starty, maxY));
            /*
             * Allows axis-aligned movements from obstacle's vertices to points on its border.
             */
            if (startIsVertex && (fuzzyEquals(startx, endx) || fuzzyEquals(starty, endy))) {
                return res;
            }
            if (fuzzyEquals(startx, minX) && endx >= minX) {
                /*
                 * Left border
                 */
                res[0] = FastMath.nextAfter(minX, startx);
            } else if (fuzzyEquals(startx, maxX) && endx <= maxX) {
                /*
                 * Right border
                 */
                res[0] = FastMath.nextAfter(maxX, startx);
            }
            if (fuzzyEquals(starty, minY) && endy >= minY) {
                /*
                 * Bottom border
                 */
                res[1] = FastMath.nextAfter(minY, starty);
            } else if (fuzzyEquals(starty, maxY) && endy <= maxY) {
                /*
                 * Top border
                 */
                res[1] = FastMath.nextAfter(maxY, starty);
            }
            return res;
        }
        return null;
    }

    @Override
    @SuppressFBWarnings("FE_FLOATING_POINT_EQUALITY")
    public V nearestIntersection(final V start, final V end) {
        final double startx = start.getX();
        final double starty = start.getY();
        final double endx = end.getX();
        final double endy = end.getY();
        final double nearx = closestTo(startx, maxX, minX);
        final double neary = closestTo(starty, maxY, minY);
        final double farx = nearx == maxX ? minX : maxX;
        final double fary = neary == maxY ? minY : maxY;
        final double[] intersectionSide1 = intersection(startx, starty, endx, endy, nearx, neary, nearx, fary);
        final double[] intersectionSide2 = intersection(startx, starty, endx, endy, nearx, neary, farx, neary);
        final double d1 = MathArrays.distance(intersectionSide1, new double[] { startx, starty });
        final double d2 = MathArrays.distance(intersectionSide2, new double[] { startx, starty });
        if (d1 < d2) {
            return start.newFrom(intersectionSide1[0], intersectionSide1[1]);
        }
        return start.newFrom(intersectionSide2[0], intersectionSide2[1]);
    }

    /**
     * Builds a new RectObstacle2D, given a point, the width and the height.
     * 
     * @param x
     *            x coordinate of the starting point
     * @param y
     *            y coordinate of the starting point
     * @param w
     *            the rectangle width. Can be negative.
     * @param h
     *            the rectangle height. Can be negative.
     */
    public RectObstacle2D(final double x, final double y, final double w, final double h) {
        super(min(x, x + w), min(y, y + h), Math.abs(w), Math.abs(h));
        minX = min(x, x + w);
        minY = min(y, y + h);
        maxX = max(x, x + w);
        maxY = max(y, y + h);
//        rect = Geometries.rectangle(minX, minY, maxX, maxY);
    }

    @Override
    public boolean contains(final double x, final double y) {
        return x >= minX && y >= minY && x <= maxX && y <= maxY;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "[" + minX + "," + minY + " -> " + maxX + "," + maxY + "]";
    }

    @Override
    public double getMaxX() {
        return maxX;
    }

    @Override
    public double getMinX() {
        return minX;
    }

    @Override
    public double getMaxY() {
        return maxY;
    }

    @Override
    public double getMinY() {
        return minY;
    }

}
