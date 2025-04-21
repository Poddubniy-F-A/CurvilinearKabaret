package project.model.side.extrapolation;

import project.model.node.Node;
import project.model.Vector;

public record ExtrapolationProperties(
        Node node, Node centralNode, Node oppositeNode,
        Vector tangentialVector, Vector normalVector,
        double step
) {

    public ExtrapolationProperties(Node node, Node centralNode, Node oppositeNode) {
        Vector midline = Vector.create(oppositeNode.getPoint(), node.getPoint()),
                tangentialVector = midline.getNormalized(), normalVector = tangentialVector.getNormal();

        this(
                node, centralNode, oppositeNode,
                tangentialVector, normalVector,
                midline.length()
        );
    }
}
