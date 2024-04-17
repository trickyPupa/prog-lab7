package common.exceptions;

public class NoSuchCommandException extends RuntimeException{
    public NoSuchCommandException(){
        super();
    }
    public NoSuchCommandException(String message){
        super(message);
    }

    @Override
    public String toString(){
        return "Не существует команды \"" + this.getMessage() + "\"\nДля справки используйте help";
    }
}
