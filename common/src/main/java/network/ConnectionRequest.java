package network;

public class  ConnectionRequest extends Request {
    protected Boolean success = null;

    public ConnectionRequest(){}
    public ConnectionRequest(boolean success) {
        this.success = success;
    }

    public void setSuccess(boolean s){
        this.success = s;
    }
    public Boolean isSuccess(){
        return success;
    }
    @Override
    public Object getContent() {
        return isSuccess();
    }
}
