package com.ysong.colorledudpfragment;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class MainActivity extends AppCompatActivity implements MainInterface {

	public static final String IP_AP = "192.168.4.1";
	public static final String IP_INVALID = "0.0.0.0";
	private static final int PORT = 5678;
	private LedFragment ledFragment = null;
	private ConnectionFragment connectionFragment = null;
	private Fragment shownFragment = null;
	private Toast toast = null;
	private String ip = IP_INVALID;
	private DatagramSocket socket = null;
	private boolean socketLocked = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		connectionFragment = new ConnectionFragment();
		ledFragment = new LedFragment();
		getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, connectionFragment).hide(connectionFragment).commit();
		getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, ledFragment).hide(ledFragment).commit();
		getSupportFragmentManager().beginTransaction().show(connectionFragment).commit();
		shownFragment = connectionFragment;
		toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
		try {
			socket = new DatagramSocket();
			socketLocked = false;
		} catch (Exception e) {
			toastShow(e.toString());
		}
	}

	@Override
	protected void onDestroy() {
		socketLocked = true;
		socket.close();
		socket = null;
		ip = null;
		toast = null;
		ledFragment = null;
		connectionFragment = null;
		shownFragment = null;
		super.onDestroy();
	}

	public void onCxnHandler(View view) {
		if (shownFragment != connectionFragment) {
			getSupportFragmentManager().beginTransaction().hide(shownFragment).show(connectionFragment).commit();
			shownFragment = connectionFragment;
		}
	}

	public void onLedHandler(View view) {
		if (shownFragment != ledFragment) {
			getSupportFragmentManager().beginTransaction().hide(shownFragment).show(ledFragment).commit();
			shownFragment = ledFragment;
		}
	}

	@Override
	public void useUiThread(Runnable runnable) {
		runOnUiThread(runnable);
	}

	@Override
	public void toastShow(String str) {
		toast.setText(str);
		toast.show();
	}

	@Override
	public void setIP(String ip) {
		this.ip = ip;
	}

	@Override
	public boolean invalidIP() {
		return ip.equals(IP_INVALID);
	}

	@Override
	public boolean getSocketLocked() {
		return socketLocked;
	}

	@Override
	public void setSocketLocked(boolean locked) {
		socketLocked = locked;
	}

	@Override
	public int socketReceive(byte[] buf, int timeout) throws Exception {
		DatagramPacket pkt = new DatagramPacket(buf, buf.length);
		socket.setSoTimeout(timeout);
		socket.receive(pkt);
		return pkt.getLength();
	}

	@Override
	public void socketSend(byte[] buf) throws Exception {
		socket.send(new DatagramPacket(buf, buf.length, InetAddress.getByName(ip), PORT));
	}
}
