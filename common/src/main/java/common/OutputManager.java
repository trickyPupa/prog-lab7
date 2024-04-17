package common;

import common.abstractions.IOutputManager;

public class OutputManager implements IOutputManager {
    @Override
    public void print(String s) {
        System.out.println(s);
    }

    @Override
    public void print(Object s) {
        System.out.println(s.toString());
    }
}
