package common.model.enums;

import java.io.Serializable;

public enum MpaaRating implements Serializable {
    PG,
    PG_13,
    NC_17;

    public static boolean contains(String a){
        for(MpaaRating s : values()){
            if (s.toString().equals(a)){
                return true;
            }
        }
        return false;
    }
}
