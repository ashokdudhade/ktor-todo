package com.relevant.programmer.service

import com.relevant.programmer.dao.DAOFacade
import com.relevant.programmer.entities.Todo
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import kotlin.test.assertEquals

class TodoServiceTests {
    @Test
    fun getAllTodosTest(){
        val todo = Todo(text = "test todo")
        val dao = mockk<DAOFacade>()
        val testUser = "testUser"
        every { dao.findAllTodos(testUser) } returns listOf(todo)
        val service = TodoService(dao)
        val todos = service.getAllTodos(testUser)
        assertEquals(todos, listOf(todo))
        verify { dao.findAllTodos(testUser) }

    }
}