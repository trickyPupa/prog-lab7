package network;

import java.io.Serializable;

public abstract class Request implements Serializable {
    public abstract Object getContent();
}
