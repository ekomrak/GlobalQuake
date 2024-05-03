package globalquake.db;

public enum EarthquakeCacheListType {
    ALL("all");

    private final String name;

    EarthquakeCacheListType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static EarthquakeCacheListType findByName(String name){
        for(EarthquakeCacheListType usersCacheListType : values()){
            if(usersCacheListType.getName().equalsIgnoreCase(name)){
                return usersCacheListType;
            }
        }
        return null;
    }
}
