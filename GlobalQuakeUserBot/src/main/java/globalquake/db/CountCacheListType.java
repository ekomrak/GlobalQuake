package globalquake.db;

public enum CountCacheListType {
    ALL("all_count"),
    ACTIVE("active_count");

    private final String name;

    CountCacheListType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static CountCacheListType findByName(String name){
        for(CountCacheListType countCacheListType : values()){
            if(countCacheListType.getName().equalsIgnoreCase(name)){
                return countCacheListType;
            }
        }
        return null;
    }
}
