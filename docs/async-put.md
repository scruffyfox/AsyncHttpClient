# AsyncHttpClient Example PUT

### Example PUT - Single Entity

```java
	AsyncHttpClient client = new AsyncHttpClient("http://example.com");
	client.put("api/v1/", new JsonResponseHandler()
	{
		@Override public void onSuccess()
		{
			JsonElement result = getContent();
		}
	});
```

### Example PUT - Headers & Params

```java
	AsyncHttpClient client = new AsyncHttpClient("http://example.com");

	List<NameValuePair> params = new ArrayList<>();
	params.add(new NameValuePair("key", "value"));

	Headers headers = Headers.of("Header", "value");

	client.put("api/v1/", params, headers, new JsonResponseHandler()
	{
		@Override public void onSuccess()
		{
			JsonElement result = getContent();
		}
	});
```

### Example PUT - Single Entity

```java
	AsyncHttpClient client = new AsyncHttpClient("http://example.com");

	RequestBody putBody = RequestBody.create(MediaType.parse("application/json"), "{\"test\":\"hello world\"}");

	client.put("api/v1/", putBody, new JsonResponseHandler()
	{
		@Override public void onSuccess()
		{
			JsonElement result = getContent();
		}
	});
```

### Example PUT - Multiple entity + file

```java
	AsyncHttpClient client = new AsyncHttpClient("http://example.com");

	RequestBody putBody = new MultipartBody.Builder().addFormDataPart("test", "test.json", RequestBody.create(MediaType.parse("application/json"), "{\"test\":\"hello world\"}"));

	client.put("api/v1/", putBody, new JsonResponseHandler()
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
