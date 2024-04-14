package globalquake.db;

public enum CacheListType {
    ALL_ACTIVE_USERS("active"),
    USERS_WITH_EARTHQUAKE_ALERT("earthquake"),
    USERS_WITH_CLUSTER_ALERT("cluster"),
    USERS_WITH_STATION_ALERT("station");

    private final String name;

    CacheListType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static CacheListType findByName(String name){
        for(CacheListType cacheListType : values()){
            if(cacheListType.getName().equalsIgnoreCase(name)){
                return cacheListType;
            }
        }
        return null;
    }
}
