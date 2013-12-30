#AsyncHttpClient Example PUT

###Example PUT

```java
	SyncHttpClient<JsonElement> client = new SyncHttpClient<JsonElement>("http://example.com");
	JsonElement response = client.put("api/v1/", new JsonProcessor());
```

###Example PUT - Single Entity

```java
	SyncHttpClient<JsonElement> client = new SyncHttpClient<JsonElement>("http://example.com");
	
	JsonEntity data = new JsonEntity("{\"key\":\"value\"}");
	GzippedEntity entity = new GzippedEntity(data);
	
	JsonElement response = client.put("api/v1/", headers, new JsonProcessor());
```

###Example PUT - URL Encoded post data

```java
	SyncHttpClient<JsonElement> client = new SyncHttpClient<JsonElement>("http://example.com");
	
	RequestEntity entity = new RequestEntity();
	entity.add("key", "value");
	
	JsonElement response = client.put("api/v1/", params, entity, headers, new JsonProcessor());
	});
```

###Example PUT - Multiple Entity + file

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
	
	JsonElement response = client.put("api/v1/", params, entity, headers, new JsonProcessor());
```
