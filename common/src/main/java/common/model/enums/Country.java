package common.model.enums;

import java.io.Serializable;

public enum Country implements Serializable {
    FRANCE,
    INDIA,
    VATICAN,
    THAILAND;

    public static boolean contains(String a){
        for(Country s : values()){
            if (s.toString().equals(a)){
                return true;
            }
        }
        return false;
    }
}
