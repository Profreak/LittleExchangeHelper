package de.linfotech.littleexchangehelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import android.os.AsyncTask;
import android.util.Log;

/**
 * 
 * This class downloads the exchange rate and notify the class which needs the information
 * 
 * @author Ludwig Biermann
 * @version 1.0
 * 
 */
public class GetterTask extends AsyncTask<URL, Integer, String> {

	private String TAG = GetterTask.class.getSimpleName().toString();

	private ExchangeListener listener;
	private String result;
	private URL url;

	/**
	 * 
	 * create a new GetterTask
	 * 
	 * @param listener the listener which needs the exchange rate
	 */
	public GetterTask(ExchangeListener listener) {
		this.listener = listener;
	}

	@Override
	protected String doInBackground(URL... params) {
		url = params[0];
		Log.d(TAG, "URL: " + url.toString());
		result = "";

		publishProgress(0);

		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(
					url.openStream()));
			String str;
			while ((str = in.readLine()) != null) {
				result += str;
			}

			in.close();
		} catch (IOException e) {
			Log.e(TAG, "IO Exception");

			e.printStackTrace();
		}

		return result;
	}

	@Override
	protected void onProgressUpdate(Integer... ignored) {
	}

	@Override
	protected void onPostExecute(String result) {

		result = result.replaceAll(".*rate\": (.*),.*", "$1");
		result.trim();

		Log.d(TAG, "Result: " + result);

		if (!result.isEmpty()) {
			if (result.matches("[0-9]*[,.]?[0-9]*")) {
				// replace ,
				result.replace(',', '.');
				listener.setExchangeRate(Float.parseFloat(result));
			} else {
				listener.callExcaption("server failure");
			}
		} else {
			listener.callExcaption("connection failure");
		}

	}
}
