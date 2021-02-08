package convex.java;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;

import convex.core.Init;
import convex.core.data.Address;

public class RemoteClientTest {
	
	@Test public void testQuery() {
		Convex convex=Convex.connect("http://34.89.82.154:3000", Init.HERO, Init.HERO_KP);
		Map<String,Object> result=convex.query ("*address*");
		assertNotNull(result);
		assertEquals(Init.HERO,Address.parse("#"+result.get("value")));
	}
	
	@Test public void testQueryAccount() {
		Convex convex=Convex.connect("http://34.89.82.154:3000", Init.HERO, Init.HERO_KP);
		Map<String,Object> result=convex.queryAccount(Init.HERO);
		assertNotNull(result);
		assertTrue(result.containsKey("sequence"));
		assertTrue(result.containsKey("memory_size"));
	}
	
	@Test public void testQueryAsync() throws InterruptedException, ExecutionException {
		Convex convex=Convex.connect("http://34.89.82.154:3000", Init.HERO, Init.HERO_KP);
		Future<Map<String,Object>> f=convex.queryAsync ("(+ 1 2)");
		Map<String,Object> result=f.get();
		assertNotNull(result);
		assertEquals(3L,result.get("value"));
	}
	
	@Test public void testTransact() {
		Convex convex=Convex.connect("http://34.89.82.154:3000", Init.VILLAIN, Init.VILLAIN_KP);
		Map<String,Object> result=convex.transact ("(* 3 4)");
		assertNotNull(result);
		assertEquals(12L,result.get("value"),"Unexpected:"+JSON.toPrettyString(result));
	}
}
