package cucumber.perf.api;

public interface Mapper<T, R> {
    R map(T o);
}