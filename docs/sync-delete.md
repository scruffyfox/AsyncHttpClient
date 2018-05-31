# AsyncHttpClient Example DELETE

### Example DELETE

```java
	SyncHttpClient<JsonElement> client = new SyncHttpClient<JsonElement>("http://example.com");
	JsonElement response = client.delete("api/v1/", new JsonResponseHandler());
```

### Example DELETE with parameters and headers

```java
	SyncHttpClient<JsonElement> client = new SyncHttpClient<JsonElement>("http://example.com");

	List<NameValuePair> params = new ArrayList<>();
	params.add(new NameValuePair("key", "value"));

	Headers headers = Headers.of("Header", "value");

	JsonElement response = client.delete("api/v1/", params, headers, new JsonResponseHandler());

	// Delete the connection info
	ConnectionInfo connectionInfo = client.getConnectionInfo();
```
