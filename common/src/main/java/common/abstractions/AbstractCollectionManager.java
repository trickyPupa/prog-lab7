package common.abstractions;

import java.time.LocalDateTime;
import java.util.Map;

public abstract class AbstractCollectionManager<T> {
    private final LocalDateTime creationDate;

    public AbstractCollectionManager(){
        creationDate = LocalDateTime.now();
    }

    public abstract void add(T element);
    public abstract T remove(int i);
    public abstract T remove(T i);
    public abstract T removeFirst();
    public abstract String presentView();
    public abstract Map<String, String> getInfo();
    public abstract void clear();
}
