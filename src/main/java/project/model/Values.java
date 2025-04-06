package project.model;

public record Values(double h, Vector w) {

    public static Values getAverage(Values v1, Values v2) {
        return new Values((v1.h() + v2.h()) / 2, Vector.getAverage(v1.w(), v2.w()));
    }
}
