package project.model.side.extrapolation;

import project.model.node.Node;
import project.model.Vector;

import static java.lang.Math.*;
import static project.extensions.Constants.G;
import static project.model.Vector.scalarMultiply;

public class Extrapolator {

    private final ExtrapolationProperties properties;

    private final Invariant invariant;
    private final EigenValue eigenValue;

    public Extrapolator(ExtrapolationProperties properties, boolean isMain) {
        this.properties = properties;

        Node centralNode = properties.centralNode();
        Vector tangential = properties.tangentialVector(), normal = properties.normalVector();
        if (isMain) {
            invariant = values -> scalarMultiply(values.w(), tangential) + sqrt(G / centralNode.getH()) * values.h();
            eigenValue = () -> scalarMultiply(centralNode.getW(), tangential) + sqrt(G * centralNode.getH());
        } else {
            invariant = values -> scalarMultiply(values.w(), normal);
            eigenValue = () -> scalarMultiply(centralNode.getW(), tangential);
        }
    }

    public double limitedExtrapolationBy(double tau) {
        double
                valuesI = invariant.I(properties.node().getValues()),
                oldOppositeSideValuesI = invariant.I(properties.oppositeNode().getNotUpdatedValues()),
                oldCValuesI = invariant.I(properties.centralNode().getOldValues()),
                CValuesI = invariant.I(properties.centralNode().getValues()),

                add = 2 * (CValuesI - oldCValuesI) +
                        eigenValue.lambda() * (valuesI - oldOppositeSideValuesI) * tau / properties.step(),
                extrapolation = 2 * CValuesI - oldOppositeSideValuesI;

        /*if (getCenterPoint().x() == 0.52 && getCenterPoint().y() == 0.51) {
            System.out.println(oppositeSideOldValues + " " + curValues);
            System.out.println(oldValuesC);
            System.out.println(valuesC);

            System.out.println((invariant.I(valuesC) - invariant.I(oldValuesC)) + " " + e.lambda() + " " + (invariant.I(curValues) - invariant.I(oppositeSideOldValues)));
            System.out.println(limiter1);
            System.out.println(limiterC);
            System.out.println(limiter2);
            System.out.println();
        }*/

        return min(
                max(oldOppositeSideValuesI, max(oldCValuesI, valuesI)) + add,
                max(
                        min(oldOppositeSideValuesI, min(oldCValuesI, valuesI)) + add,
                        extrapolation
                )
        );
    }
}
