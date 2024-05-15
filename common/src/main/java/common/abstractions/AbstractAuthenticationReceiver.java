package common.abstractions;

import common.user.Session;
import common.user.User;

public abstract class AbstractAuthenticationReceiver extends AbstractReceiver {
    private AbstractReceiver receiver;
    private Session currentSession;

    public AbstractAuthenticationReceiver(AbstractReceiver receiver) {
        super(receiver.inputManager, receiver.outputManager);
        this.receiver = receiver;
    }

    public void setCurrentSession(Session currentSession) {
        this.currentSession = currentSession;
    }

    public Session getCurrentSession() {
        return currentSession;
    }

    public abstract void authUser(Object[] args);

    public abstract void registerUser(Object[] args);

    public abstract void logOut(Object[] args);

    @Override
    public void update(Object[] args) {
        receiver.update(args);
    }

    @Override
    public void removeLower(Object[] args) {
        receiver.removeLower(args);
    }

    @Override
    public void removeFirst(Object[] args) {
        receiver.removeFirst(args);
    }

    @Override
    public void removeById(Object[] args) {
        receiver.removeById(args);
    }

    @Override
    public void removeAllByGoldenPalmCount(Object[] args) {
        receiver.removeAllByGoldenPalmCount(args);
    }

    @Override
    public void minByCoordinates(Object[] args) {
        receiver.minByCoordinates(args);
    }

    @Override
    public void history(Object[] args) {
        receiver.history(args);
    }

    @Override
    public void help(Object[] args) {
        receiver.help(args);
    }

    @Override
    public void filterByGoldenPalmCount(Object[] args) {
        receiver.filterByGoldenPalmCount(args);
    }

    @Override
    public void executeScript(Object[] args) {
        receiver.executeScript(args);
    }

    @Override
    public void info(Object[] args) {
        receiver.info(args);
    }

    @Override
    public void exit(Object[] args) {
        receiver.exit(args);
    }

    @Override
    public void show(Object[] args) {
        receiver.show(args);
    }

    @Override
    public void clear(Object[] args) {
        receiver.clear(args);
    }

    @Override
    public void save(Object[] args) {
        receiver.save(args);
    }

    @Override
    public void add(Object[] args) {
        receiver.add(args);
    }
}
