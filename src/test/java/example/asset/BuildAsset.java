package example.asset;

import java.util.Map;

import convex.core.crypto.AKeyPair;
import convex.core.data.Address;
import convex.java.Convex;
import convex.java.asset.TokenBuilder;

public class BuildAsset {
	/**
	 * URL for the Peer API server
	 */
	static final String TEST_PEER="https://convex.world";
	
	/**
	 * A new Ed25519 key to use for this example. 
	 */
	static final AKeyPair KP=AKeyPair.generate();

	public static void main(String [] args) {
		// Make a Convex connection to test network
		Convex convex=Convex.connect(TEST_PEER);
		System.out.println("Hello Convex! Connected to "+TEST_PEER);
		
		Address address = convex.createAccount(KP);
		System.out.println("Created account "+address+" with public key "+KP.getAccountKey());
		
		convex.faucet(address, 100000000);
		
		convex.setAddress(address);
		convex.setKeyPair(KP);
		
		
		TokenBuilder tBuilder=new TokenBuilder().withSupply(1000000000); 
		Map<String,Object> result=tBuilder.deploy(convex);
		
		System.out.println(result);
		
		System.exit(0);
	}
}
