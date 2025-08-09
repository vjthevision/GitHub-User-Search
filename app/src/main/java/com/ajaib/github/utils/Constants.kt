package com.ajaib.github.utils

object Constants {
    const val BASE_URL = "https://api.github.com/"

    // Cache expiration times
    const val CACHE_TIMEOUT_MINUTES = 30
    const val CACHE_TIMEOUT_MS = CACHE_TIMEOUT_MINUTES * 60 * 1000L

    // Pagination
    const val DEFAULT_PAGE_SIZE = 30
    const val FIRST_PAGE = 1

    // Search
    const val SEARCH_DELAY_MS = 500L
    const val MIN_SEARCH_QUERY_LENGTH = 1

    // Rate limiting
    const val MAX_REQUESTS_PER_HOUR = 60
    const val RATE_LIMIT_RESET_TIME_MS = 60 * 60 * 1000L // 1 hour
}