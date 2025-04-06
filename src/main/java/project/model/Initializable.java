package project.model;

import project.init.Initializer;

@FunctionalInterface
public interface Initializable {
    void init(Initializer initializer);
}
