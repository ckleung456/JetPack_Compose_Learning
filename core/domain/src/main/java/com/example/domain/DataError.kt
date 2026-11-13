package com.example.domain

/**
 * Identifies the kind of failure behind a [DataError].
 */
enum class Kind {
    /** An I/O error occurred while communicating with the server. */
    NETWORK,

    /** An exception was thrown while (de)serializing a body. */
    CONVERSION,

    /** A non-2xx HTTP status code was received from the server. */
    HTTP,

    /** An internal error occurred while attempting to execute a request. */
    UNEXPECTED
}

/**
 * An [Error] originating from a data source, carrying a [kind] and an optional human-readable [message].
 */
interface DataError : Error {
    val kind: Kind
    val message: String?
}

data class UnexpectedError(override val message: String?) : DataError {
    override val kind: Kind = Kind.UNEXPECTED
}
