package com.example.service

import com.example.model.Todo
import com.example.repository.TodoRepository
import com.example.service.exception.ConflictException
import jakarta.inject.Inject
import jakarta.inject.Singleton

import javax.transaction.Transactional

@Singleton
@Transactional
class TodoService {

	@Inject
	TodoRepository todoRepository

	Todo save(Todo todo) {
		if (todoRepository.find(todo.title).present) {
			throw new ConflictException("Duplicated title ${todo.title}")
		}
		if (todo.id != null) {
			return todoRepository.update(todo)
		}
		return todoRepository.save(todo)
	}
}
