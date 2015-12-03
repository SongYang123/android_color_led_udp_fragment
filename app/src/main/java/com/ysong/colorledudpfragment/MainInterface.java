package com.ysong.colorledudpfragment;

public interface MainInterface {

	void toastShow(String str);
	void setIP(String ip);
	boolean invalidIP();
	boolean getSocketLocked();
	void setSocketLocked(boolean locked);
    int socketReceive(byte[] buf, int timeout) throws Exception;
	void socketSend(byte[] buf) throws Exception;
}
