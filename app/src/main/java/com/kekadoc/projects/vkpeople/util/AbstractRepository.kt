package com.kekadoc.projects.vkpeople.util

import android.util.Log
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

abstract class AbstractRepository : CoroutineScope {

    companion object {
        private const val TAG: String = "AbstractRepository-TAG"
    }

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        Log.e(TAG, "vkAuthCallbackHandler: $exception")
        onCoroutineFail(exception)
    }
    override val coroutineContext: CoroutineContext = Job() + coroutineExceptionHandler + Dispatchers.IO

    protected open fun onCoroutineFail(fail: Throwable) {  }

    protected fun <T> execute(callback: RequestCallback<T>?, block: suspend CoroutineScope.() -> T): Job {
        return if (callback == null) {
            async(Dispatchers.IO, block = block)
        } else {
            val exceptionHandler = CoroutineExceptionHandler { _, throwable -> callback.onFail(throwable) }
            callback.onStart()
            async(Dispatchers.IO + exceptionHandler) {
                val result: T = block.invoke(this)
                withContext(Dispatchers.Main) { callback.onSuccess(result) }
            }
        }
    }

}