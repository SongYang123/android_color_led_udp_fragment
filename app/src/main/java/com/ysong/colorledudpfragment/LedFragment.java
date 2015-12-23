package com.ysong.colorledudpfragment;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;

public class LedFragment extends Fragment {

	private MainInterface mainInterface = null;
	private Button btnSktStart = null;
	private Button btnSktStop = null;
	private SeekBar seekLedRed = null;
	private SeekBar seekLedGreen = null;
	private SeekBar seekLedBlue = null;
	private SeekBar seekLedHue = null;
	private boolean updateThreadEnabled = false;
	private String update = null;

	private class UpdateAsyncTask extends AsyncTask<Void, String, Void> {
		@Override
		protected Void doInBackground(Void... v) {
			mainInterface.setSocketLocked(true);
			while (updateThreadEnabled) {
				try {
					if (update != null) {
						mainInterface.socketSend(update.getBytes());
						update = null;
					}
					Thread.sleep(25);
				} catch (Exception e) {
					publishProgress(e.toString());
				}
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
	}

	@Override
	public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_led, container, false);
		btnSktStart = (Button)view.findViewById(R.id.btn_led_start);
		btnSktStop = (Button)view.findViewById(R.id.btn_led_stop);
		seekLedRed = (SeekBar)view.findViewById(R.id.seek_led_red);
		seekLedGreen = (SeekBar)view.findViewById(R.id.seek_led_green);
		seekLedBlue = (SeekBar)view.findViewById(R.id.seek_led_blue);
		seekLedHue = (SeekBar)view.findViewById(R.id.seek_led_hue);
		setSeekBarEnabled(false, false, false, false);
		btnSktStart.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mainInterface.getSocketLocked()) {
					mainInterface.toastShow("Socket Occupied");
				} else if (mainInterface.invalidIP()) {
					mainInterface.toastShow("No IP Applied");
				} else {
					updateThreadEnabled = true;
					new UpdateAsyncTask().execute();
					setSeekBarEnabled(true, true, true, true);
				}
			}
		});
		btnSktStop.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (updateThreadEnabled) {
					setSeekBarEnabled(false, false, false, false);
					updateThreadEnabled = false;
				} else {
					mainInterface.toastShow("Stopped Already");
				}
			}
		});
		seekLedRed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
				update = "R" + toHex(progressValue);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				setSeekBarEnabled(true, false, false, false);
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				setSeekBarEnabled(true, true, true, true);
			}
		});
		seekLedGreen.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
				update = "G" + toHex(progressValue);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
                setSeekBarEnabled(false, true, false, false);
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
                setSeekBarEnabled(true, true, true, true);
			}
		});
		seekLedBlue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
				update = "B" + toHex(progressValue);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				setSeekBarEnabled(false, false, true, false);
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				setSeekBarEnabled(true, true, true, true);
			}
		});
		seekLedHue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
				update = "H" + toHex(progressValue);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				setSeekBarEnabled(false, false, false, true);
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				setSeekBarEnabled(true, true, true, true);
			}
		});
		return view;
	}

	@Override
	public void onDestroyView() {
		update = null;
		updateThreadEnabled = false;
		btnSktStop = null;
		btnSktStart = null;
		seekLedHue = null;
		seekLedBlue = null;
		seekLedGreen = null;
		seekLedRed = null;
		super.onDestroyView();
	}

	@Override
	public void onDetach() {
		mainInterface = null;
		super.onDetach();
	}

	private void setSeekBarEnabled(boolean red, boolean green, boolean blue, boolean hue) {
		seekLedRed.setEnabled(red);
		seekLedGreen.setEnabled(green);
		seekLedBlue.setEnabled(blue);
		seekLedHue.setEnabled(hue);
	}

	private String toHex(int x) {
		final char[] hex = {
				'0', '1', '2', '3',
				'4', '5', '6', '7',
				'8', '9', ':', ';',
				'<', '=', '>', '?'
		};
		return "" + hex[x / 16] + hex[x % 16];
	}
}
