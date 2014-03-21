package de.linfotech.littleexchangehelper;

import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * This class administrates the main Features of the the Program
 * 
 * 
 * @author Ludwig Biermann
 * @version 3.0
 * 
 */
public class LittleExchangeHelperActivity extends Activity implements OnItemSelectedListener, ExchangeListener {

	private String TAG = LittleExchangeHelperActivity.class.getSimpleName().toString();
//	String url = "http://rate-exchange.appspot.com/currency?from=USD&to=EUR";	
	String url = "http://rate-exchange.appspot.com/currency?from=";
	
	private EditText value1;
	private EditText value2;
	
	private Button paste;
	private Button copy;
	
	private Spinner spinner1;
	private Spinner spinner2;
	
//	Exchange Rate	
	private float exchangeRate; 
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

//		initial 1  for testing and debug		
		exchangeRate = 1;
		
		value1 = (EditText) this.findViewById(R.id.value1);
		value2 = (EditText) this.findViewById(R.id.value2);
		value2.setEnabled(false);
		
		paste = (Button) this.findViewById(R.id.paste);
		copy = (Button) this.findViewById(R.id.copy);

		value1.addTextChangedListener(new TextUpdateListener());
		paste.setOnClickListener(new PasteToClipboard());
		copy.setOnClickListener(new CopyToClipboard());
		this.findViewById(R.id.helpSwitcher).setOnClickListener(new HelpListener());
		
		spinner1 = (Spinner) findViewById(R.id.currency1);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
		        R.array.currency, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		spinner1.setAdapter(adapter);
		spinner1.setOnItemSelectedListener(this);
		
		spinner2 = (Spinner) findViewById(R.id.currency2);
		ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this,
		        R.array.currency, android.R.layout.simple_spinner_item);
		adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		spinner2.setAdapter(adapter2);
		spinner2.setOnItemSelectedListener(this);
		spinner2.setSelection(1);
		
		this.updateExchangeRate(spinner1.getSelectedItem().toString(), spinner2.getSelectedItem().toString());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/**
	 *  copy to ClipBoard
	 */
	private void copyToClipBoard() {

		String copy = "";
		if (value2.getText() != null) {
			copy = value2.getText().toString();
		}

		ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		ClipData clip = ClipData.newPlainText("Currency", copy);
		clipboard.setPrimaryClip(clip);

	}

	/**
	 *  paste from Clipboard 
	 */
	private void pasteFromClipBoard() {

		ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		String paste = "";
		
		ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);

		paste = item.getText().toString();
		
		if(paste.matches("[0-9]*[,.]?[0-9]*")) {
			// replace ,
			paste.replace(',', '.');
			value1.setText(paste);
		}
	}
	
	/**
	 * update Exchange Rate
	 */
	public void updateExchangeRate(String currency1, String currency2) {
		Log.d(TAG,"update ExchangeRate");
		
		// extract ISO Code of Selected item
		currency1 = getISO(currency1);
		currency2 = getISO(currency2);
		
		URL exchangeRateURL = null;
		
		try {
			exchangeRateURL = new URL(url + currency1 + "&to=" + currency2);
			Log.d(TAG, "URL: " + exchangeRateURL);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		if (exchangeRateURL != null) {
			new GetterTask(this).execute(exchangeRateURL);
		}
	}

	
	/**
	 * updates the Display
	 */
	public void updateDisplay() {
		Log.d(TAG,"update Currency");
		
		if(!value1.getText().toString().isEmpty()){
			float v1 = Float.parseFloat(value1.getText().toString());
			v1 *= this.exchangeRate;
			value2.setText(Float.toString(v1));
		}
		
	}
	
	/**
	 * Gives the first three letter back
	 * 
	 * Example EUR - Euro -> return EUR
	 * If EUR dosnt match [A-Z]{3} return EUR
	 * 
	 * @param s the given sting
	 * @return EUR if wrong spelling
	 */
	public String getISO(String s){
		s.trim();
		s = s.substring(0, 3);
		if(s.matches("[A-Z]{3}")) {
			return s;
		}
		Log.e(TAG, "given String is not valid: " + s);
		return "EUR";
	}
	
	/* Listener */

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		Log.d(TAG, "item selection change!");
		this.updateExchangeRate(spinner1.getSelectedItem().toString(), spinner2.getSelectedItem().toString());
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		Log.e(TAG, "Nothing Selected .... WhY?");
	}

	@Override
	public void setExchangeRate(Float exchangeRate) {
		this.exchangeRate = exchangeRate;
		this.updateDisplay();
	}

	@Override
	public void callExcaption(String error) {
		Toast.makeText(this, error, Toast.LENGTH_LONG).show();
	}
	

	
	/**
	 * 
	 * Listen to Text Changes
	 * 
	 * @author Ludwig Biermann
	 * @version 1.0
	 *
	 */
	class TextUpdateListener implements TextWatcher {

		@Override
		public void afterTextChanged(Editable arg0) {
			updateDisplay();
		}

		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
			
		}

		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
		}
		
	}
	

	/**
	 * Listen for Paste Action
	 * 
	 * 
	 * @author Ludwig Biermann
	 * @version 1.0
	 * 
	 */
	class PasteToClipboard implements OnClickListener {

		@Override
		public void onClick(View v) {
			pasteFromClipBoard();
			
		}

	}

	/**
	 * 
	 * 
	 * @author Ludwig Biermann
	 * @version 1.0
	 * 
	 */
	class HelpListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			Intent in = new Intent(getApplicationContext(), HelpActivity.class);
			startActivity(in);
		}

	}
	
	/**
	 *  
	 * Listen for Copy Action
	 * 
	 * 
	 * @author Ludwig Biermann
	 * @version 1.0
	 * 
	 */
	class CopyToClipboard implements OnClickListener {

		@Override
		public void onClick(View v) {
			copyToClipBoard();
		}
	}
}
