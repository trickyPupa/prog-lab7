package network;

import common.commands.abstractions.Command;

import java.io.Serializable;
import java.util.Collection;

public abstract class Request implements Serializable {
    public abstract Object getContent();
}
