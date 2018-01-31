package net.callumtaylor.asynchttp.obj

import android.util.Pair
import okhttp3.Headers

/**
 * // TODO: Add class description
 */
data class Request(
	var path: String = "",
	var queryParams: List<Pair<String, String>>? = null,
	var headers: Headers? = null,

	var followRedirects: Boolean = true,
	var allowAllSSl: Boolean = false,

	// request params populated by request
	var time: Long = 0,
	var length: Long = 0
)
