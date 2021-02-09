package convex.java;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;

import convex.core.Init;
import convex.core.data.Address;

public class RemoteClientTest {
	
	static final String TEST_PEER="http://34.89.82.154:3000";
	
	@Test public void testQuery() {
		Convex convex=Convex.connect(TEST_PEER, Init.HERO, Init.HERO_KP);
		Map<String,Object> result=convex.query ("*address*");
		assertNotNull(result);
		assertEquals(Init.HERO,Address.parse("#"+result.get("value")));
	}
	
	@Test public void testQueryAccount() {
		Convex convex=Convex.connect(TEST_PEER, Init.HERO, Init.HERO_KP);
		Map<String,Object> result=convex.queryAccount(Init.HERO);
		assertNotNull(result);
		assertTrue(result.containsKey("sequence"));
		assertTrue(result.containsKey("memory_size"));
	}
	
	
	@Test public void testQueryAsync() throws InterruptedException, ExecutionException {
		Convex convex=Convex.connect(TEST_PEER, Init.HERO, Init.HERO_KP);
		Future<Map<String,Object>> f=convex.queryAsync ("(+ 1 2)");
		Map<String,Object> result=f.get();
		assertNotNull(result);
		assertEquals(3L,result.get("value"));
	}
	
	@Test public void testTransact() {
		Convex convex=Convex.connect(TEST_PEER, Init.VILLAIN, Init.VILLAIN_KP);
		Map<String,Object> result=convex.transact ("(* 3 4)");
		assertNotNull(result);
		assertEquals(12L,result.get("value"),"Unexpected:"+JSON.toPrettyString(result));
	}
	
	@Test public void testNewAccount() throws InterruptedException, ExecutionException {
		Convex convex=Convex.connect(TEST_PEER);
		Address addr=convex.useNewAccount(1000666);
		assertNotNull(addr);
		Map<String,Object> acc1=convex.queryAccount();
		assertEquals(1000666,((Number)acc1.get("balance")).longValue());
		
		Future<Map<String,Object>> r=null;
		for (int i=0; i<10; i++) {
			r=convex.transactAsync("(def a "+i+")");
		}
		Map<String,Object> result=r.get();
		assertFalse(result.containsKey("errorCode"));
		assertEquals(9L,result.get("value"));
		
		// check we have consumed some balance for transactions
		long finalBalance=convex.queryBalance();
		assertTrue(finalBalance<1000666);
	}
	
	@Test public void testFaucet() {
		Convex convex=Convex.connect(TEST_PEER);
		Address addr=convex.useNewAccount();
		Map<String,Object> acc1=convex.queryAccount();
		Map<String,Object> freq=convex.faucet(addr,999);
		assertTrue(freq.containsKey("amount"),"Unexpected: "+freq);
		Map<String,Object> acc2=convex.queryAccount(addr);
		long bal1=((Number)acc1.get("balance")).longValue();
		long bal2=((Number)acc2.get("balance")).longValue();
		
		assertEquals(999,bal2-bal1);
	}
}
