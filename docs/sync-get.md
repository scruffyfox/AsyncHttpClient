# AsyncHttpClient Example GET

### Example GET

```java
	SyncHttpClient<JsonElement> client = new SyncHttpClient<JsonElement>("http://example.com");
	JsonElement response = client.get("api/v1/", new JsonResponseHandler());
```

### Example GET with parameters and headers

```java
	SyncHttpClient<JsonElement> client = new SyncHttpClient<JsonElement>("http://example.com");

	List<NameValuePair> params = new ArrayList<>();
	params.add(new NameValuePair("key", "value"));

	Headers headers = Headers.of("Header", "value");

	JsonElement response = client.get("api/v1/", params, headers, new JsonResponseHandler());

	// Get the connection info
	ConnectionInfo connectionInfo = client.getConnectionInfo();
```
