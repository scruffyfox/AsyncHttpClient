# AsyncHttpClient Example PUT

### Example PUT

```java
	SyncHttpClient<JsonElement> client = new SyncHttpClient<JsonElement>("http://example.com");
	JsonElement response = client.put("api/v1/", new JsonResponseHandler());
```

### Example PUT - Single Entity

```java
	SyncHttpClient<JsonElement> client = new SyncHttpClient<JsonElement>("http://example.com");

	RequestBody putBody = RequestBody.create(MediaType.parse("application/json"), "{\"test\":\"hello world\"}");

	JsonElement response = client.put("api/v1/", putBody, new JsonResponseHandler());
```

### Example PUT - Multiple Entity + file

```java
	SyncHttpClient<JsonElement> client = new SyncHttpClient<JsonElement>("http://example.com");

	List<NameValuePair> params = new ArrayList<>();
	params.add(new NameValuePair("key", "value"));

	Headers headers = Headers.of("Header", "value");

	RequestBody putBody = new MultipartBody.Builder().addFormDataPart("test", "test.json", RequestBody.create(MediaType.parse("application/json"), "{\"test\":\"hello world\"}"));

	JsonElement response = client.put("api/v1/", params, putBody, headers, new JsonResponseHandler());
```
