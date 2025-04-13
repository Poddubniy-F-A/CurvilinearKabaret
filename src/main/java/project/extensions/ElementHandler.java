package project.extensions;

@FunctionalInterface
public interface ElementHandler<T> {

    T handle(int i, int j);
}
