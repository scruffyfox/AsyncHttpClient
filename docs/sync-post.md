#AsyncHttpClient Example POST

###Example POST

```java
	SyncHttpClient<JsonElement> client = new SyncHttpClient<JsonElement>("http://example.com");
	JsonElement response = client.post("api/v1/", new JsonResponseHandler());
```

###Example POST - Single Entity

```java
	SyncHttpClient<JsonElement> client = new SyncHttpClient<JsonElement>("http://example.com");

	RequestBody postBody = RequestBody.create(MediaType.parse("application/json"), "{\"test\":\"hello world\"}");

	JsonElement response = client.post("api/v1/", postBody, new JsonResponseHandler());
```

###Example POST - Multiple Entity + file

```java
	SyncHttpClient<JsonElement> client = new SyncHttpClient<JsonElement>("http://example.com");

	List<NameValuePair> params = new ArrayList<>();
	params.add(new NameValuePair("key", "value"));

	Headers headers = Headers.of("Header", "value");

	RequestBody postBody = new MultipartBody.Builder().addFormDataPart("test", "test.json", RequestBody.create(MediaType.parse("application/json"), "{\"test\":\"hello world\"}"));

	JsonElement response = client.post("api/v1/", params, postBody, headers, new JsonResponseHandler());
```
