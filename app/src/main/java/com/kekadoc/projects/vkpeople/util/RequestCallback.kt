package com.kekadoc.projects.vkpeople.util

interface RequestCallback<T> {

    fun onFail(error: Throwable) {}
    fun onStart() {}
    fun onSuccess(result: T) {}

}