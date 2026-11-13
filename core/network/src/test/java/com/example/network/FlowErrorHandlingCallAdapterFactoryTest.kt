package com.example.network

import com.example.domain.Kind
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okhttp3.Request
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Timeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.io.IOException
import java.lang.reflect.Type
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class FlowErrorHandlingCallAdapterFactoryTest {

    @Suppress("unused")
    private interface Api {
        fun body(): Flow<String>
        fun response(): Flow<Response<String>>
        fun plain(): String
    }

    private val retrofit = Retrofit.Builder().baseUrl("http://localhost/").build()
    private val factory = FlowErrorHandlingCallAdapterFactory()

    private fun returnType(method: String): Type = Api::class.java.getMethod(method).genericReturnType

    @Suppress("UNCHECKED_CAST")
    private fun <R> adapter(method: String): CallAdapter<String, R> =
        factory.get(returnType(method), emptyArray(), retrofit) as CallAdapter<String, R>

    /** A [Call] that completes synchronously via [deliver] when enqueued. */
    private class FakeCall(private val deliver: FakeCall.(Callback<String>) -> Unit) : Call<String> {
        var canceled = false
        override fun enqueue(callback: Callback<String>) = deliver(callback)
        override fun execute(): Response<String> = throw UnsupportedOperationException()
        override fun isExecuted() = true
        override fun cancel() { canceled = true }
        override fun isCanceled() = canceled
        override fun clone(): Call<String> = this
        override fun request(): Request = Request.Builder().url("http://localhost/").build()
        override fun timeout(): Timeout = Timeout.NONE
    }

    private fun respond(response: Response<String>) = FakeCall { it.onResponse(this, response) }
    private fun fail(throwable: Throwable) = FakeCall { it.onFailure(this, throwable) }
    private fun errorResponse(code: Int) = Response.error<String>(code, "".toResponseBody())

    private suspend fun Flow<*>.failure(): Throwable? = runCatching { first() }.exceptionOrNull()

    @Test
    fun `ignores non Flow return types`() {
        assertNull(factory.get(returnType("plain"), emptyArray(), retrofit))
    }

    @Test
    fun `reports the inner type as response type`() {
        assertEquals(String::class.java, adapter<Flow<String>>("body").responseType())
        assertEquals(String::class.java, adapter<Flow<Response<String>>>("response").responseType())
    }

    @Test
    fun `body adapter emits the response body`() = runTest {
        val flow = adapter<Flow<String>>("body").adapt(respond(Response.success("hello")))

        assertEquals("hello", flow.first())
    }

    @Test
    fun `body adapter fails with UnexpectedError on empty body`() = runTest {
        val flow = adapter<Flow<String>>("body").adapt(respond(Response.success(null)))

        assertTrue(flow.failure() is RetrofitException.UnexpectedError)
    }

    @Test
    fun `non 2xx response becomes HttpError`() = runTest {
        val flow = adapter<Flow<String>>("body").adapt(respond(errorResponse(404)))

        val error = flow.failure() as RetrofitException.HttpError
        assertEquals(Kind.HTTP, error.kind)
        assertEquals(404, error.response.code())
        assertEquals("http://localhost/", error.url)
    }

    @Test
    fun `response adapter emits the whole response on success`() = runTest {
        val response = Response.success("hello")
        val flow = adapter<Flow<Response<String>>>("response").adapt(respond(response))

        assertSame(response, flow.first())
    }

    @Test
    fun `response adapter turns non 2xx into HttpError`() = runTest {
        val flow = adapter<Flow<Response<String>>>("response").adapt(respond(errorResponse(500)))

        assertEquals(500, (flow.failure() as RetrofitException.HttpError).response.code())
    }

    @Test
    fun `connectivity failures become NetworkError`() = runTest {
        listOf(UnknownHostException(), SocketTimeoutException()).forEach { throwable ->
            val flow = adapter<Flow<String>>("body").adapt(fail(throwable))

            val error = flow.failure()
            assertTrue("$throwable -> $error", error is RetrofitException.NetworkError)
            assertEquals(Kind.NETWORK, (error as RetrofitException).kind)
        }
    }

    @Test
    fun `other IOExceptions become NetworkError wrapping the cause`() = runTest {
        val cause = IOException("reset")
        val flow = adapter<Flow<String>>("body").adapt(fail(cause))

        assertSame(cause, (flow.failure() as RetrofitException.NetworkError).exception)
    }

    @Test
    fun `unknown failures become UnexpectedError`() = runTest {
        val cause = IllegalStateException("boom")
        val flow = adapter<Flow<String>>("body").adapt(fail(cause))

        val error = flow.failure() as RetrofitException.UnexpectedError
        assertSame(cause, error.exception)
        assertEquals(Kind.UNEXPECTED, error.kind)
    }
}
