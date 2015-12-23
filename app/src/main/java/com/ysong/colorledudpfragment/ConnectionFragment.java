package com.ysong.colorledudpfragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;


public class ConnectionFragment extends Fragment {

	private static final int LOGIN_TIMEOUT = 50000;
	private static final int GET_IP_TIMEOUT = 5000;

	private MainInterface mainInterface = null;
	private EditText editCxnName = null;
	private EditText editCxnPassword = null;
	private Button btnCxnLogin = null;
	private Button btnCxnIP = null;
	private SharedPreferences sp = null;
	private String name = null;
	private String password = null;

	private class CxnLoginAsyncTask extends AsyncTask<Void, String, Void> {
		@Override
		protected Void doInBackground(Void... v) {
			mainInterface.setSocketLocked(true);
			try {
				byte[] buf = new byte[32];
				mainInterface.socketFlush(buf);
				mainInterface.socketSend(("N" + name).getBytes());
				mainInterface.socketSend(("P" + password).getBytes());
				int length = mainInterface.socketReceive(buf, LOGIN_TIMEOUT);
				if (new String(buf, 0, length).equals("OK")) {
					publishProgress("Station Login Success");
				} else {
					publishProgress("Station Login Fail");
				}
			} catch (Exception e) {
				publishProgress(e.toString());
			}
			mainInterface.setSocketLocked(false);
			return null;
		}

		@Override
		protected void onProgressUpdate(String... msg) {
			mainInterface.toastShow(msg[0]);
		}
	}

	private class CxnIPAsyncTask extends AsyncTask<Void, String, Void> {

		@Override
		protected Void doInBackground(Void... v) {
			mainInterface.setSocketLocked(true);
			try {
				byte[] buf = new byte[32];
				mainInterface.socketFlush(buf);
				mainInterface.socketSend("IP".getBytes());
				int length = mainInterface.socketReceive(buf, GET_IP_TIMEOUT);
				String str = new String(buf, 0, length);
				if (str.equals("FAIL")) {
					publishProgress("Get Station IP Fail");
				} else {
					SharedPreferences.Editor ed = sp.edit();
					ed.putString("ip", str);
					ed.commit();
					publishProgress("Get Station IP Success: " + str);
				}
			} catch (Exception e) {
				publishProgress(e.toString());
			}
			mainInterface.setSocketLocked(false);
			return null;
		}

		@Override
		protected void onProgressUpdate(String... msg) {
			mainInterface.toastShow(msg[0]);
		}
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		mainInterface = (MainInterface)context;
		sp = context.getSharedPreferences("IP", Context.MODE_PRIVATE);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_connection, container, false);
		editCxnName = (EditText)view.findViewById(R.id.edit_cxn_name);
		editCxnPassword = (EditText)view.findViewById(R.id.edit_cxn_password);
		Button btnCxnAp = (Button)view.findViewById(R.id.btn_cxn_ap);
		Button btnCxnSta = (Button)view.findViewById(R.id.btn_cxn_sta);
		btnCxnLogin = (Button)view.findViewById(R.id.btn_cxn_login);
		btnCxnIP = (Button)view.findViewById(R.id.btn_cxn_ip);
		setStationWidgetEnabled(false);
		btnCxnAp.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mainInterface.getSocketLocked()) {
					mainInterface.toastShow("Socket Occupied");
				} else {
					mainInterface.setIP(MainActivity.IP_AP);
					setStationWidgetEnabled(true);
					mainInterface.toastShow("Access Point IP Applied");
				}
			}
		});
		btnCxnSta.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mainInterface.getSocketLocked()) {
					mainInterface.toastShow("Socket Occupied");
				} else {
					String ip = sp.getString("ip", MainActivity.IP_INVALID);
					if (ip.equals(MainActivity.IP_INVALID)) {
						mainInterface.toastShow("Station IP Invalid, Not Applied");
					} else {
						mainInterface.setIP(ip);
						setStationWidgetEnabled(false);
						mainInterface.toastShow("Station IP Applied");
					}
				}
			}
		});
        btnCxnLogin.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				name = editCxnName.getText().toString();
				password = editCxnPassword.getText().toString();
				if (name.equals("") || password.equals("")) {
					mainInterface.toastShow("Field Empty");
				} else if (mainInterface.getSocketLocked()) {
					mainInterface.toastShow("Socket Occupied");
				} else {
					new CxnLoginAsyncTask().execute();
				}
			}
		});
		btnCxnIP.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mainInterface.getSocketLocked()) {
					mainInterface.toastShow("Socket Occupied");
				} else {
					new CxnIPAsyncTask().execute();
				}
			}
		});
		return view;
	}

	@Override
	public void onDestroyView() {
		btnCxnIP = null;
		btnCxnLogin = null;
		editCxnPassword = null;
		editCxnName = null;
		super.onDestroyView();
	}

	@Override
	public void onDetach() {
		password = null;
		name = null;
		sp = null;
		mainInterface = null;
		super.onDetach();
	}

	private void setStationWidgetEnabled(boolean enabled) {
		editCxnName.setEnabled(enabled);
		editCxnPassword.setEnabled(enabled);
		btnCxnLogin.setEnabled(enabled);
		btnCxnIP.setEnabled(enabled);
	}
}
