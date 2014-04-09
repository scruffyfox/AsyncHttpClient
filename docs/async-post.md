#AsyncHttpClient Example POST

###Example POST - Single Entity

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

###Example POST - Headers & Params

```java
	AsyncHttpClient client = new AsyncHttpClient("http://example.com");
	List<NameValuePair> params = new ArrayList<NameValuePair>();
	params.add(new BasicNameValuePair("key", "value"));
	
	List<Header> headers = new ArrayList<Header>();
	headers.add(new BasicHeader("1", "2"));

	client.post("api/v1/", params, headers, new JsonResponseHandler()
	{
		@Override public void onSuccess()
		{
			JsonElement result = getContent();
		}
	});
```

###Example POST - Single Entity

```java
	AsyncHttpClient client = new AsyncHttpClient("http://example.com");
	
	JsonEntity data = new JsonEntity("{\"key\":\"value\"}");
	GzippedEntity entity = new GzippedEntity(data);
	
	client.post("api/v1/", entity, new JsonResponseHandler()
	{
		@Override public void onSuccess()
		{
			JsonElement result = getContent();
		}
	});
```

###Example POST - URL Encoded post data

```java
	AsyncHttpClient client = new AsyncHttpClient("http://example.com");
	
	RequestEntity entity = new RequestEntity();
	entity.add("key", "value");
	
	client.post("api/v1/", entity, new JsonResponseHandler()
	{
		@Override public void onSuccess()
		{
			JsonElement result = getContent();
		}
	});
```

###Example POST - Multiple entity + file

```java
	AsyncHttpClient client = new AsyncHttpClient("http://example.com");
		
	MultiPartEntity entity = new MultiPartEntity();
	FileBody data1 = new FileBody(new File("/IMG_6614.JPG"), "image/jpeg");
	JsonEntity data2 = new JsonEntity("{\"key\":\"value\"}");
	entity.addPart("image1.jpg", data1);
	entity.addPart("content1", data2);
	
	client.post("api/v1/", entity, new JsonResponseHandler()
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