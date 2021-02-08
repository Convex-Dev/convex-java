package convex.java;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;

import convex.core.crypto.AKeyPair;
import convex.core.crypto.ASignature;
import convex.core.crypto.Hash;
import convex.core.data.Address;
import convex.core.util.Utils;

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
	
	public long getSequence() {
		return sequence;
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
	public Map<String,Object> query(String code) {
		String json=buildJsonQuery(code);
		return doPost(url+"/api/v1/query",json);
	}
	
	/**
	 * Query account details on the network.
	 * @param code Account Address to query
	 * @return Result of query, as parsed JSON Object from query response
	 */
	public Map<String,Object> queryAccount(Address address) {
		return doGet(url+"/api/v1/accounts/"+address.longValue());
	}
	
	/**
	 * Query account details on the network asynchronously.
	 * @param code Account Address to query
	 * @return Result of query, as Future for parsed JSON Object from query response
	 */
	public CompletableFuture<Map<String,Object>> queryAccountAsync(Address address) {
		return doGetAsync(url+"/api/v1/accounts/"+address.longValue());
	}
	
	/**
	 * Query using specific source code
	 * @param code Source code in Convex Lisp
	 * @return Result of query, as parsed JSON Object from query response
	 */
	public Map<String,Object> transact(String code) {
		try {
			return transactAsync(code).get();
		} catch (Throwable e) {
			throw Utils.sneakyThrow(e);
		}
	}
	
	public CompletableFuture<Map<String,Object>> transactAsync(String code) {
		String json=buildJsonQuery(code);
		CompletableFuture<Map<String,Object>> prep=doPostAsync(url+"/api/v1/transaction/prepare",json);
		return prep.thenComposeAsync(r->{
			Map<String,Object> result=r;
			Hash hash=Hash.fromHex((String) result.get("hash"));
			
			try {
				CompletableFuture<Map<String,Object>> tr = submitAsync(hash);
				return tr;
			} catch (Throwable e) {
				throw Utils.sneakyThrow(e);
			}
			
		});
	}
	
	private CompletableFuture<Map<String,Object>> submitAsync(Hash hash) {
		ASignature sd=getKeyPair().sign(hash);
		HashMap<String,Object> req=new HashMap<>();
		req.put("address", getAddress().longValue());
		req.put("hash", hash.toHexString());
		req.put("account_key", getKeyPair().getAccountKey().toHexString());
		req.put("sig", sd.toHexString());
		String json=JSON.toPrettyString(req);
		// System.out.println("Submitting:\n "+json);
		return doPostAsync(url+"/api/v1/transaction/submit",json);
	}

	/**
	 * Query using specific source code
	 * @param code Source code in Convex Lisp
	 * @return Future to be completed with result of query, as parsed JSON Object from query response
	 */
	public CompletableFuture<Map<String,Object>> queryAsync(String code) {
		String json=buildJsonQuery(code);
		return doPostAsync(url+"/api/v1/query",json);
	}
	
	private String buildJsonQuery(String code) {
		HashMap<String,Object> req=new HashMap<>();
		req.put("address", address.toString());
		req.put("source", code);
		String json=JSON.toPrettyString(req);
		return json;
	}
	
	private Map<String,Object> doPost(String endPoint, String json) {
		try {
			return doPostAsync(endPoint,json).get();
		} catch (Throwable  e) {
			throw Utils.sneakyThrow(e);
		}
	}
	
	private Map<String,Object> doGet(String endPoint) {
		try {
			return doGetAsync(endPoint).get();
		} catch (Throwable  e) {
			throw Utils.sneakyThrow(e);
		}
	}
	
	private CompletableFuture<Map<String,Object>> doPostAsync(String endPoint, String json) {
		HttpPost post=new HttpPost(endPoint);
		return doRequest(post,json);
	}
	
	private CompletableFuture<Map<String,Object>> doGetAsync(String endPoint) {
		HttpGet post=new HttpGet(endPoint);
		return doRequest(post,null);
	}
	
	private CompletableFuture<Map<String,Object>> doRequest(HttpUriRequest request, String json) {
		try {
			if (json!=null) {
				request.addHeader("content-type", "application/json");
				StringEntity entity;
				entity = new StringEntity(json);
				((HttpPost)request).setEntity(entity);
			}
			CompletableFuture<HttpResponse> future=toCompletableFuture(fc -> httpasyncclient.execute(request, (FutureCallback<HttpResponse>) fc));
			return future.thenApply(response->{
				try {
					return JSON.parse(response.getEntity().getContent());
				} catch (Throwable e) {
					throw new Error("Error handling response:" +response,e);
				}
			});
			
		} catch (Throwable e) {
			throw Utils.sneakyThrow(e);
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
