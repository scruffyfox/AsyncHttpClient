# AsyncHttpClient Example POST

### Example POST - Single Entity

```java
	AsyncHttpClient client = new AsyncHttpClient("http://example.com");
	client.post("api/v1/", new JsonResponseHandler()
	{
		@Override public void onSuccess()
		{
			JsonElement result = getContent();
		}
	});
```

### Example POST - Headers & Params

```java
	AsyncHttpClient client = new AsyncHttpClient("http://example.com");

	List<NameValuePair> params = new ArrayList<>();
	params.add(new NameValuePair("key", "value"));

	Headers headers = Headers.of("Header", "value");

	client.post("api/v1/", params, headers, new JsonResponseHandler()
	{
		@Override public void onSuccess()
		{
			JsonElement result = getContent();
		}
	});
```

### Example POST - Single Entity

```java
	AsyncHttpClient client = new AsyncHttpClient("http://example.com");

	RequestBody postBody = RequestBody.create(MediaType.parse("application/json"), "{\"test\":\"hello world\"}");

	client.post("api/v1/", postBody, new JsonResponseHandler()
	{
		@Override public void onSuccess()
		{
			JsonElement result = getContent();
		}
	});
```

### Example POST - Multiple entity + file

```java
	AsyncHttpClient client = new AsyncHttpClient("http://example.com");

	RequestBody postBody = new MultipartBody.Builder().addFormDataPart("test", "test.json", RequestBody.create(MediaType.parse("application/json"), "{\"test\":\"hello world\"}"));

	client.post("api/v1/", postBody, new JsonResponseHandler()
	{
		@Override public void onSuccess()
		{
			JsonElement result = getContent();
		}

		@Override public void onByteChunkReceivedProcessed(long totalProcessed, long totalLength)
		{
			// Show download progress here.
			// This method is ran on the UI thread
		}
	});
```
