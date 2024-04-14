package globalquake.db;

public enum UsersCacheListType {
    ALL_ACTIVE_USERS("active"),
    USERS_WITH_EARTHQUAKE_ALERT("earthquake"),
    USERS_WITH_CLUSTER_ALERT("cluster"),
    USERS_WITH_STATION_ALERT("station");

    private final String name;

    UsersCacheListType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static UsersCacheListType findByName(String name){
        for(UsersCacheListType usersCacheListType : values()){
            if(usersCacheListType.getName().equalsIgnoreCase(name)){
                return usersCacheListType;
            }
        }
        return null;
    }
}
