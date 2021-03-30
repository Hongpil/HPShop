package com.philip.portfolioprogrammershop.util

open class Event<out T>(private val content: T) {
    var hasBeenHandled = false
        private set

    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    /**
     * 이벤트의 처리 여부에 상관 없이 값을 반환한다.
     */
    fun peekContent(): T = content
}