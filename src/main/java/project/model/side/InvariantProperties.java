package project.model.side;

import project.model.Cell;
import project.model.Vector;

import static project.Constants.G;

public record InvariantProperties(
        Cell cell,
        double step,
        Vector tangentialVector,
        Vector normalVector,
        Side oppositeSide,
        Invariant mainInvariant, Invariant optionalInvariant
) {

    public InvariantProperties(Cell cell, Side side) {
        Side oppositeSide = cell.getOppositeSide(side);
        Vector midline = Vector.create(oppositeSide.getCenterPoint(), side.getCenterPoint()),
                tangentialVector = midline.getNormalized(), normalVector = tangentialVector.getNormal();

        this(
                cell,
                midline.length(),
                tangentialVector,
                normalVector,
                oppositeSide,
                (values) -> Vector.scalarMultiply(values.w(), tangentialVector) + Math.sqrt(G / cell.getH()) * values.h(),
                (values) -> Vector.scalarMultiply(values.w(), normalVector)
        );
    }
}
