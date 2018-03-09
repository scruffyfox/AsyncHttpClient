package net.callumtaylor.asynchttp.obj

import okhttp3.RequestBody

/**
 * // TODO: Add class description
 */
data class Request(
	var path: String = "",
	var queryParams: MutableMap<String, String> = mutableMapOf(),
	var headers: MutableMap<String, String> = mutableMapOf(),
	var body: RequestBody? = null,
	var type: String = "",

	// request options
	var followRedirects: Boolean = true,
	var allowAllSSl: Boolean = false
)
{
	// request params populated by request
	var time: Long = 0
	var length: Long = 0
}
