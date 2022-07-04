package com.example.model

import io.micronaut.data.annotation.GeneratedValue
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity

import javax.validation.constraints.NotBlank

@MappedEntity
class Todo {

	@Id
	@GeneratedValue
	Long id

	@NotBlank
	String title
}
