package interfaces;

public interface Filterable {
    public boolean passesFilter(String category, String operator, String value);
}
