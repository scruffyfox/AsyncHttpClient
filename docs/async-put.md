#AsyncHttpClient Example PUT

###Example PUT - Single Entity

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

###Example PUT - Headers & Params

```java
	AsyncHttpClient client = new AsyncHttpClient("http://example.com");
	List<NameValuePair> params = new ArrayList<NameValuePair>();
	params.add(new BasicNameValuePair("key", "value"));
	
	List<Header> headers = new ArrayList<Header>();
	headers.add(new BasicHeader("1", "2"));

	client.put("api/v1/", params, headers, new JsonResponseHandler()
	{
		@Override public void onSuccess()
		{
			JsonElement result = getContent();
		}
	});
```

###Example PUT - Single Entity

```java
	AsyncHttpClient client = new AsyncHttpClient("http://example.com");
	
	JsonEntity data = new JsonEntity("{\"key\":\"value\"}");
	GzippedEntity entity = new GzippedEntity(data);
	
	client.put("api/v1/", entity, new JsonResponseHandler()
	{
		@Override public void onSuccess()
		{
			JsonElement result = getContent();
		}
	});
```

###Example PUT - URL Encoded post data

```java
	AsyncHttpClient client = new AsyncHttpClient("http://example.com");
	
	RequestEntity entity = new RequestEntity();
	entity.add("key", "value");
	
	client.put("api/v1/", entity, new JsonResponseHandler()
	{
		@Override public void onSuccess()
		{
			JsonElement result = getContent();
		}
	});
```

###Example PUT - Multiple entity + file

```java
	AsyncHttpClient client = new AsyncHttpClient("http://example.com");
		
	MultiPartEntity entity = new MultiPartEntity();
	FileBody data1 = new FileBody(new File("/IMG_6614.JPG"), "image/jpeg");
	JsonEntity data2 = new JsonEntity("{\"key\":\"value\"}");
	entity.addPart("image1.jpg", data1);
	entity.addPart("content1", data2);
	
	client.put("api/v1/", entity, new JsonResponseHandler()
	{
		@Override public void onSuccess()
		{
			JsonElement result = getContent();
		}
		
		@Override public void onPublishedDownloadProgressUI(long totalProcessed, long totalLength)
		{
			// Show download progress here.
			// This method is ran on the UI thread
		}
	});
```