package common.abstractions;

import common.commands.abstractions.Command;

public abstract class DataInputReceiver extends AbstractReceiver {
    private AbstractReceiver rec;

    public DataInputReceiver(AbstractReceiver rec){
        super(rec.inputManager, rec.outputManager);
        this.rec = rec;
    }

    public abstract void add(Object[] args, Command command);

    public abstract void removeLower(Object[] args, Command command);

    public abstract void update(Object[] args, Command command);

    @Override
    public void add(Object[] args) {
        rec.add(args);
    }

    @Override
    public void save(Object[] args) {
        rec.save(args);
    }

    @Override
    public void clear(Object[] args) {
        rec.clear(args);
    }

    @Override
    public void show(Object[] args) {
        rec.show(args);
    }

    @Override
    public void exit(Object[] args) {
        rec.exit(args);
    }

    @Override
    public void info(Object[] args) {
        rec.info(args);
    }

    @Override
    public void executeScript(Object[] args) {
        rec.executeScript(args);
    }

    @Override
    public void filterByGoldenPalmCount(Object[] args) {
        rec.filterByGoldenPalmCount(args);
    }

    @Override
    public void help(Object[] args) {
        rec.help(args);
    }

    @Override
    public void history(Object[] args) {
        rec.history(args);
    }

    @Override
    public void minByCoordinates(Object[] args) {
        rec.minByCoordinates(args);
    }

    @Override
    public void removeAllByGoldenPalmCount(Object[] args) {
        rec.removeAllByGoldenPalmCount(args);
    }

    @Override
    public void removeById(Object[] args) {
        rec.removeById(args);
    }

    @Override
    public void removeFirst(Object[] args) {
        rec.removeFirst(args);
    }

    @Override
    public void removeLower(Object[] args) {
        rec.removeLower(args);
    }

    @Override
    public void update(Object[] args) {
        rec.update(args);
    }
}
