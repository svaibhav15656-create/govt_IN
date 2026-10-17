package in.beforeyougo.location.dto;

/**
 * A single result from location typeahead search.
 * Includes the location code, names, hierarchy level, and breadcrumb path.
 */
public class LocationSearchResult {

    public enum Level {
        STATE, DISTRICT, SUB_DISTRICT, VILLAGE, PINCODE
    }

    private final String code;
    private final String nameEn;
    private final String nameLocal;
    private final Level level;
    private final String path;

    public LocationSearchResult(String code, String nameEn, String nameLocal,
                                 Level level, String path) {
        this.code = code;
        this.nameEn = nameEn;
        this.nameLocal = nameLocal;
        this.level = level;
        this.path = path;
    }

    public String getCode() { return code; }
    public String getNameEn() { return nameEn; }
    public String getNameLocal() { return nameLocal; }
    public Level getLevel() { return level; }
    public String getPath() { return path; }
}
