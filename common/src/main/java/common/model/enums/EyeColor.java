package common.model.enums;

import java.io.Serializable;

public enum EyeColor implements Serializable {
    BLUE,
    YELLOW,
    ORANGE,
    WHITE,
    BROWN;

    public static boolean contains(String a){
        for(EyeColor s : values()){
            if (s.toString().equals(a)){
                return true;
            }
        }
        return false;
    }
}
