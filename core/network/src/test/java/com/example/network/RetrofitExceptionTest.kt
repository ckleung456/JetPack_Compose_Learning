package com.example.network

import com.example.domain.Kind
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.Response
import retrofit2.Retrofit
import java.io.IOException

class RetrofitExceptionTest {

    @Test
    fun `network error uses the IOException message`() {
        val error = RetrofitException.NetworkError(IOException("offline"))

        assertEquals(Kind.NETWORK, error.kind)
        assertEquals("offline", error.message)
        assertEquals("", error.url)
    }

    @Test
    fun `http error message combines status code and message`() {
        val response = Response.error<String>(503, "".toResponseBody())
        val error = RetrofitException.HttpError(
            url = "http://localhost/x",
            response = response,
            retrofit = Retrofit.Builder().baseUrl("http://localhost/").build(),
            successType = String::class.java
        )

        assertEquals(Kind.HTTP, error.kind)
        assertEquals("503 - ${response.message()}", error.message)
        assertEquals("http://localhost/x", error.url)
    }

    @Test
    fun `unexpected error with null message has empty message`() {
        val error = RetrofitException.UnexpectedError(IllegalStateException())

        assertEquals(Kind.UNEXPECTED, error.kind)
        assertEquals("", error.message)
    }
}
