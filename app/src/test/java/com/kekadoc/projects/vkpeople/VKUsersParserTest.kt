package com.kekadoc.projects.vkpeople

import com.kekadoc.projects.vkpeople.vkapi.VKUsersParser
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class VKUsersParserTest {

    @Test
    fun parseLastId(): Unit = runBlockingTest {
        launch {
            println("Complete! Last Id: ${VKUsersParser.parseLastId()}")
        }
    }

}