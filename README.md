# AsyncHttpClient by Callum Taylor

[![Build Status](https://travis-ci.org/scruffyfox/AsyncHttpClient.svg?branch=develop)](https://travis-ci.org/scruffyfox/AsyncHttpClient)

Follow me on [Twitter](http://twitter.com/scruffyfox) | [Website](http://callumtaylor.net)

This is the new and improved version of `AsyncHttpClient` taken from X-Library. It was long due a re-write.

In this version it allows a more flexible usage of posting files, http entities and GZIP handling.

The library uses Square's OKHTTP library.

It consists of 2 different classes, `AsyncHttpClient` and `SyncHttpClient`. Obviously by the name, `AsyncHttpClient` is for asynchronous requests which uses the `AsyncTask` paradigm, and `SyncHttpClient` is for synchronous requests which should be handled **by yourself in a thread outside of the UI thread**.

# Usage

Simply import the gradle file into Android Studio and reference the dependancy in your `settings.gradle` and project's `build.gradle`

Alternatively you can add the maven dependancy `net.callumtaylor:asynchttpclient:2.1.1` **N.B. Do NOT use `net.callumtaylor.asynchttp` for the group ID. This is an old ID and will NOT be updated**

# Table of contents

- AsyncHttpClient
	1. [Example GET](docs/async-get.md)
	2. [Example POST](docs/async-post.md)
	3. [Example PUT](docs/async-put.md)
	4. [Example DELETE](docs/async-delete.md)
	5. [Example custom handler](docs/async-custom.md)

- SyncHttpClient
	1. [Example GET](docs/sync-get.md)
	2. [Example POST](docs/sync-post.md)
	3. [Example PUT](docs/sync-put.md)
	4. [Example DELETE](docs/sync-delete.md)
	5. [Example custom handler](docs/sync-custom.md)


# Other notes
### Downloading large files

In order to download large files, you will need to subclass `AsyncHttpResponseHandler` and override the `onByteChunkReceived()` method to write directly to cache instead of appending to a `ByteArrayOutputStream` which is what the standard `BinaryResponseHandler` does. This is to stop OOM due to a over-sized output stream.

### AsyncHttpClient

**Note:** Because `AsyncHttpClient` uses `AsyncTask`, only one instance can be created at a time. If one client makes 2 requests, the first request is canceled for the new request. You can either wait for the first to finish before making the second, or you can create two seperate instances. See: [Example custom handler](docs/async-custom.md) for more.

### SyncHttpClient

`SyncHttpClient` is a paramitized class which means the type you infer to it, is the type that gets returned when calling the method. When supplying a ResponseHandler, that ResponseHandler must also paramitized with the same type as the `SyncHttpClient` instance.

You can also get the info of the request by calling your `SyncHttpClient` instance and `getConnectionInfo()`. This can only be called after the response has been completed.

**Note:** Because Android requires all network requests to be performed outside the UI thread, you must use SyncHttpClient sparingly and make sure you handle the operaion **OFF** the UI thread.

Because of the nature of REST, `GET` and `DELETE` requests behave in the same
way, `POST` and `PUT` requests also behave in the same way.

# License

See [library/LICENSE](library/LICENSE)
