package example.trader;

import java.io.InputStream;

public class Trader {

	public static void main(String [] args) {
		// Read API key for https://free.currencyconverterapi.com/
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
	    try  {
	    	InputStream is=classLoader.getResourceAsStream("example/trader/APIKEY");
	    	String text = new String(is.readAllBytes());
	    	System.out.println("Using API key: "+text);
	    } catch (Throwable e) {
			e.printStackTrace();
			throw new Error(e);
	    	
	    }
	}
}
