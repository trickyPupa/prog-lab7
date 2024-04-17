package common.model.enums;

import java.io.Serializable;

public enum HairColor implements Serializable {
    GREEN,
    RED,
    BLUE,
    YELLOW,
    ORANGE;

    public static boolean contains(String a){
        for(HairColor s : values()){
            if (s.toString().equals(a)){
                return true;
            }
        }
        return false;
    }
}
