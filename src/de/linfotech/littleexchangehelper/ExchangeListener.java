package de.linfotech.littleexchangehelper;

public interface ExchangeListener {

	public void setExchangeRate(Float exchangeRate);
	
	public void callExcaption(String error);
}
