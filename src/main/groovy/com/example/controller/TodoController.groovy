package com.example.controller

import com.example.model.Todo
import com.example.repository.TodoRepository
import com.example.service.TodoService
import com.example.service.exception.ConflictException
import io.micronaut.core.annotation.Nullable
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpResponseFactory
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Error
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.http.annotation.QueryValue
import io.micronaut.security.annotation.Secured
import jakarta.inject.Inject

import javax.validation.Valid

@Controller('/todo')
class TodoController {

	@Inject
	TodoService todoService

	@Inject
	TodoRepository todoRepository

	@Post('/')
	@Secured(['MEMBER', 'ADMIN'])
	Todo save(@Valid Todo todo) {
		return todoService.save(todo)
	}

	@Put('/{id}')
	@Secured(['MEMBER', 'ADMIN'])
	Todo update(@PathVariable Long id, @Valid @Body Todo todo) {
		todo.id = id
		return todoService.save(todo)
	}

	@Get('/')
	@Secured(['MEMBER', 'ADMIN'])
	Iterable<Todo> list(@QueryValue @Nullable String title) {
		if (title) {
			return todoRepository.findAllByTitleLike(title + '%')
		} else {
			return todoRepository.findAll()
		}
	}

	@Get('/{id}')
	@Secured(['MEMBER', 'ADMIN'])
	Optional<Todo> show(@PathVariable Long id) {
		return todoRepository.findById(id)
	}

	@Delete('/{id}')
	@Secured(['ADMIN'])
	void delete(@PathVariable Long id) {
		todoRepository.deleteById(id)
	}

	@Error(ConflictException)
	HttpResponse onError(ConflictException exception) {
		return HttpResponseFactory.INSTANCE.status(HttpStatus.CONFLICT).body(new ApiError(message: exception.message))
	}
}
