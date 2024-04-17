package common.abstractions;

public abstract class AbstractReceiver {
    protected IInputManager inputManager;
    protected IOutputManager outputManager;

    public AbstractReceiver(IInputManager inp, IOutputManager out) {
        inputManager = inp;
        outputManager = out;
    }

    public void add(Object[] args){
        return;
    }

    public void save(Object[] args){
        return;
    }

    public void clear(Object[] args){
        return;
    }

    public void show(Object[] args){
        return;
    }

    public void exit(Object[] args){
        return;
    }

    public void info(Object[] args){
        return;
    }

    public void executeScript(Object[] args){
        return;
    }

    public void filterByGoldenPalmCount(Object[] args){
        return;
    }

    public void help(Object[] args){
        return;
    }

    public void history(Object[] args){
        return;
    }

    public void minByCoordinates(Object[] args){
        return;
    }

    public void removeAllByGoldenPalmCount(Object[] args){
        return;
    }

    public void removeById(Object[] args){
        return;
    }

    public void removeFirst(Object[] args){
        return;
    }

    public void removeLower(Object[] args){
        return;
    }

    public void update(Object[] args){
        return;
    }
    
}
