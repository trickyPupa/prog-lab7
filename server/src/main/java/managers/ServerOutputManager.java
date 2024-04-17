package managers;

import common.abstractions.IOutputManager;

public class ServerOutputManager implements IOutputManager {
    private String response = "";

    @Override
    public void print(String s) {
        response += s + "\n";
    }

    @Override
    public void print(Object s){
        ;
    }

    public String getResponse(){
        return response;
    }

    public String popResponce(){
        String s = response;
        response = "";
        return s;
    }
}
