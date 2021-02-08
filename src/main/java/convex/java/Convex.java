package convex.java;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;

import convex.core.crypto.AKeyPair;
import convex.core.data.Address;

public class Convex {
	private static final CloseableHttpAsyncClient httpasyncclient = HttpAsyncClients.createDefault();
	
	static {
		httpasyncclient.start();
	}
	
	private final String url;
	private AKeyPair keyPair;
	private Address address;
	private Long sequence=null;
	
	private Convex(String peerServerURL) {
		this.url=peerServerURL;
	}
	
	public static Convex connect(String peerServerURL, Address address,AKeyPair keyPair) {
		Convex convex=new Convex(peerServerURL);
		convex.setAddress(address);
		convex.setKeyPair(keyPair);
		return convex;
	}
	
	public Address getAddress() {
		return address;
	}
	
	public AKeyPair getKeyPair() {
		return keyPair;
	}
	
	private void setKeyPair(AKeyPair keyPair) {
		this.keyPair=keyPair;
	}

	public void setAddress(Address address) {
		this.address=address;
	}

	/**
	 * Query using specific source code
	 * @param code Source code in Convex Lisp
	 * @return Result of query, as parsed JSON Object from query response
	 */
	public <T> T query(String code) {
		String json=buildJsonQuery(code);
		return doPost(url+"/api/v1/query",json);
	}
	
	/**
	 * Query using specific source code
	 * @param code Source code in Convex Lisp
	 * @return Future to be completed with result of query, as parsed JSON Object from query response
	 */
	@SuppressWarnings("unchecked")
	public <T> CompletableFuture<T> queryAsync(String code) {
		String json=buildJsonQuery(code);
		return (CompletableFuture<T>) doPostAsync(url+"/api/v1/query",json);
	}
	
	private String buildJsonQuery(String code) {
		HashMap<String,Object> req=new HashMap<>();
		req.put("address", address.toString());
		req.put("source", code);
		String json=JSON.toPrettyString(req);
		return json;
	}
	

	@SuppressWarnings("unchecked")
	private <T> T doPost(String endPoint, String json) {
		try {
			return (T) doPostAsync(endPoint,json).get();
		} catch (Throwable  e) {
			throw new Error("Failed to complete HTTP request",e);
		}
	}
	
	private CompletableFuture<Object> doPostAsync(String endPoint, String json) {
		HttpPost post=new HttpPost(endPoint);
		post.addHeader("content-type", "application/json");
		StringEntity entity;
		try {
			entity = new StringEntity(json);
			post.setEntity(entity);
			CompletableFuture<HttpResponse> future=toCompletableFuture(fc -> httpasyncclient.execute(post, (FutureCallback<HttpResponse>) fc));
			return future.thenApply(response->{
				try {
					return JSON.parse(response.getEntity().getContent());
				} catch (Throwable e) {
					throw new Error(e);
				}
			});
			
		} catch (Throwable e) {
			throw new Error(e);
		}
	}
	
	private static <T> CompletableFuture<T> toCompletableFuture(Consumer<FutureCallback<T>> c) {
        CompletableFuture<T> promise = new CompletableFuture<>();

        c.accept(new FutureCallback<T>() {
            @Override
            public void completed(T t) {
                promise.complete(t);
            }

            @Override
            public void failed(Exception e) {
                promise.completeExceptionally(e);
            }

            @Override
            public void cancelled() {
                promise.cancel(true);
            }
        });
        return promise;
    }

}
