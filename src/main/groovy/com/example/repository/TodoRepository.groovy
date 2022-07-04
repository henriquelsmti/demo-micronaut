package com.example.repository

import com.example.model.Todo
import io.micronaut.context.annotation.Executable
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.CrudRepository

@JdbcRepository(dialect = Dialect.H2)
interface TodoRepository extends CrudRepository<Todo, Long> {
	@Executable
	Optional<Todo> find(String title)

	@Executable
	List<Todo> findAllByTitleLike(String title)
}
