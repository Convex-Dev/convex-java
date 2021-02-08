package convex.java;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;

import org.junit.jupiter.api.Test;

import convex.core.Init;

public class TestRemoteClient {
	
	@Test public void testQuery() {
		Convex convex=Convex.connect("http://34.89.82.154:3000", Init.HERO, Init.HERO_KP);
		Map<String,Object> result=convex.query ("(+ 1 2)");
		assertNotNull(result);
		assertEquals(3L,result.get("value"));
	}
}
