#SyncHttpClient for Java by Callum Taylor

#Usage example

**Note** Because of the way SyncHttpClient works, only one instance can be created at a time. If one client makes 2 requests, the first request is canceled for the new request. You can either wait for the first to finish before making the second, or can create two seperate instances.

SyncHttpClient is a paramitized class which means the type you infer to it, is the type that gets returned when calling the method. When supplying a {@link Processor}, that processor must also paramitized with the same type as the SyncHttpClient instance.

##Example GET

```java
	SyncHttpClient<JsonElement> client = new SyncHttpClient<JsonElement>("http://example.com");
	List<NameValuePair> params = new ArrayList<NameValuePair>();
	params.add(new BasicNameValuePair("key", "value"));
	
	List<Header> headers = new ArrayList<Header>();
	headers.add(new BasicHeader("1", "2"));
	 
	JsonElement response = client.get("api/v1/", params, headers, new JsonProcessor());
```

##Example DELETE

```java
	SyncHttpClient<JsonElement> client = new SyncHttpClient<JsonElement>("http://example.com");
	List<NameValuePair> params = new ArrayList<NameValuePair>();
	params.add(new BasicNameValuePair("key", "value"));
	
	List<Header> headers = new ArrayList<Header>();
	headers.add(new BasicHeader("1", "2"));
	 
	JsonElement response = client.delete("api/v1/", params, headers, new JsonProcessor());
```

##Example POST - Single Entity

```java
	SyncHttpClient<JsonElement> client = new SyncHttpClient<JsonElement>("http://example.com");
	List<NameValuePair> params = new ArrayList<NameValuePair>();
	params.add(new BasicNameValuePair("key", "value"));
	
	List<Header> headers = new ArrayList<Header>();
	headers.add(new BasicHeader("1", "2"));
	
	JsonEntity data = new JsonEntity("{\"key\":\"value\"}");
	GzippedEntity entity = new GzippedEntity(data);
	
	JsonElement response = client.post("api/v1/", params, entity, headers, new JsonProcessor());
```

##Example POST - URL Encoded post data

```java
	SyncHttpClient<JsonElement> client = new SyncHttpClient<JsonElement>("http://example.com");
	
	RequestEntity entity = new RequestEntity();
	entity.add("key", "value");
	
	JsonElement response = client.post("api/v1/", params, entity, headers, new JsonProcessor());
	});
```

##Example POST - Multiple Entity + file

```java
	SyncHttpClient<JsonElement> client = new SyncHttpClient<JsonElement>("http://example.com");
	List<NameValuePair> params = new ArrayList<NameValuePair>();
	params.add(new BasicNameValuePair("key", "value"));
	
	List<Header> headers = new ArrayList<Header>();
	headers.add(new BasicHeader("1", "2"));
	
	MultiPartEntity entity = new MultiPartEntity();
	FileBody data1 = new FileBody(new File("/IMG_6614.JPG"), "image/jpeg");
	JsonEntity data2 = new JsonEntity("{\"key\":\"value\"}");
	entity.addPart("image1.jpg", data1);
	entity.addPart("content1", data2);
	
	JsonElement response = client.post("api/v1/", params, entity, headers, new JsonProcessor());
```

##Example PUT

```java
	SyncHttpClient<JsonElement> client = new SyncHttpClient<JsonElement>("http://example.com");
	List<NameValuePair> params = new ArrayList<NameValuePair>();
	params.add(new BasicNameValuePair("key", "value"));
	
	List<Header> headers = new ArrayList<Header>();
	headers.add(new BasicHeader("1", "2"));
	
	JsonEntity data = new JsonEntity("{\"key\":\"value\"}");
	GzippedEntity entity = new GzippedEntity(data);
	
	JsonElement response = client.put("api/v1/", params, entity, headers, new JsonProcessor());
```

##Example custom processor

```java
SyncHttpClient<String> client = new SyncHttpClient<String>("http://example.com");
List<NameValuePair> params = new ArrayList<NameValuePair>();
params.add(new BasicNameValuePair("key", "value"));

String encodedResponse = client.get("api/v1/", params, new Processor()
{
	private StringBuffer stringBuffer;

	@Override public void onPublishedDownloadProgress(byte[] chunk, int chunkLength, long totalProcessed, long totalLength)
	{
 	if (stringBuffer == null)
 	{
			int total = (int)(totalLength > Integer.MAX_VALUE ? Integer.MAX_VALUE : totalLength);
			stringBuffer = new StringBuffer(Math.max(8192, total));
		}

		if (chunk != null)
		{
			try
			{
			// Shift all the bytes right
				byte tmp = chunk[chunk.length - 1];
			for (int index = chunk.length - 2; index >= 0; index--)
			{
				chunk[index + 1] = chunk[index];
			}

			chunk[0] = tmp;
				stringBuffer.append(new String(chunk, 0, chunkLength, "UTF-8").);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
 	}
	}

	@Override public String getContent()
	{
 	return stringBuffer.toString();
	}
});
```

Because of the nature of REST, GET and DELETE requests behave in the same
way, POST and PUT requests also behave in the same way.