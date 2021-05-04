package example.asset;

import convex.core.crypto.AKeyPair;
import convex.core.data.Address;
import convex.java.Convex;

public class BuildAsset {
	static final String TEST_PEER="https://convex.world";
	
	static final AKeyPair KP=AKeyPair.createSeeded(578575);

	public static void main(String [] args) {
		// Make a Convex connection to test network
		Convex convex=Convex.connect(TEST_PEER);
		System.out.println("Hello Convex! Connected to "+TEST_PEER);
		
		Address address = convex.createAccount(KP);
		System.out.println("Created account "+address);
		
		System.exit(0);
	}
}
