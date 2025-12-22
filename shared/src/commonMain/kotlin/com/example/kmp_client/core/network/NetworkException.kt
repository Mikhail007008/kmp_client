package com.example.kmp_client.core.network

sealed class NetworkException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class TimeoutException(message: String) : NetworkException(message)
    class ConnectionException(message: String) : NetworkException(message)
    class ServerException(message: String, val statusCode: Int) : NetworkException(message)
    class ParsingException(message: String, cause: Throwable?) : NetworkException(message, cause)
    class UnknownException(message: String, cause: Throwable?) : NetworkException(message, cause)
    class NotFoundException(message: String, cause: Throwable? = null) : NetworkException(message, cause)
}