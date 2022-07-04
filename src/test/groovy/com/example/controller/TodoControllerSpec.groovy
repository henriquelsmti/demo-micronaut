package com.example.controller

import com.example.model.Todo
import io.micronaut.core.type.Argument
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.http.uri.UriBuilder
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.token.generator.TokenGenerator
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification
import spock.lang.Stepwise

@MicronautTest
@Stepwise
class TodoControllerSpec extends Specification {

	@Inject
	@Client('/todo')
	HttpClient httpClient

	@Inject
	TokenGenerator tokenGenerator

	void "Test add"() {
		setup:
		Todo todo = new Todo().tap {
			title = 'Make code'
		}
		HttpRequest<Todo> request = HttpRequest.POST('/', todo)
		request.bearerAuth(currentAccessTokenForMember)
		when:
		Todo result = httpClient.toBlocking().retrieve(request, Argument.of(Todo))
		then:
		result.id > 0
		result.title == todo.title
	}

	void "Test add duplicated"() {
		setup:
		Todo todo = new Todo().tap {
			title = 'Make code'
		}
		HttpRequest<Todo> request = HttpRequest.POST('/', todo)
		request.bearerAuth(currentAccessTokenForMember)
		when:
		httpClient.toBlocking().retrieve(request, Argument.of(Todo))
		then:
		HttpClientResponseException exception = thrown()
		exception.response.code() == HttpStatus.CONFLICT.code
		exception.response.getBody(ApiError).get().message == "Duplicated title ${todo.title}"
	}

	void "Test update"() {
		setup:
		Todo todo = new Todo().tap {
			title = 'Make code always'
		}
		HttpRequest<Todo> request = HttpRequest.PUT("/1", todo)
		request.bearerAuth(currentAccessTokenForMember)
		when:
		Todo result = httpClient.toBlocking().retrieve(request, Argument.of(Todo))
		then:
		result.id > 0
		result.title == todo.title
	}

	void "Test get"() {
		setup:
		HttpRequest<Todo> request = HttpRequest.GET("/1")
		request.bearerAuth(currentAccessTokenForMember)
		when:
		Todo result = httpClient.toBlocking().retrieve(request, Argument.of(Todo))
		then:
		result.id > 0
		result.title == 'Make code always'
	}

	void "Test List all"() {
		setup:
		Todo todo = new Todo().tap {
			title = 'Codding'
		}
		HttpRequest<Todo> request = HttpRequest.POST('/', todo)
		request.bearerAuth(currentAccessTokenForMember)
		httpClient.toBlocking().retrieve(request, Argument.of(Todo))

		request = HttpRequest.GET("/")
		request.bearerAuth(currentAccessTokenForMember)
		when:
		List<Todo> result = httpClient.toBlocking().retrieve(request, Argument.listOf(Todo))
		then:
		result.size() == 2
		result.title == ['Make code always', 'Codding']
	}

	void "Test by title"() {
		setup:
		final UriBuilder builder = UriBuilder.of("/")
		builder.queryParam('title', 'Make')
		HttpRequest request = HttpRequest.GET(builder.build())
		request.bearerAuth(currentAccessTokenForMember)
		when:
		List<Todo> result = httpClient.toBlocking().retrieve(request, Argument.listOf(Todo))
		then:
		result.size() == 1
		result.title == ['Make code always']
	}

	void "Test delete with MEMBER"() {
		setup:
		HttpRequest<Todo> request = HttpRequest.DELETE("/2")
		request.bearerAuth(currentAccessTokenForMember)
		when: 'try delete id 2'
		httpClient.toBlocking().exchange(request)
		then:
		HttpClientResponseException exception = thrown()
		exception.response.code() == HttpStatus.FORBIDDEN.code
	}

	void "Test delete"() {
		setup:
		HttpRequest<Todo> request = HttpRequest.DELETE("/2")
		request.bearerAuth(currentAccessTokenForAdmin)
		when: 'Delete id 2'
		HttpResponse response = httpClient.toBlocking().exchange(request)
		then:
		response.code() == HttpStatus.OK.code
		when: 'List'
		request = HttpRequest.GET("/")
		request.bearerAuth(currentAccessTokenForMember)
		List<Todo> result = httpClient.toBlocking().retrieve(request, Argument.listOf(Todo))
		then:
		result.size() == 1
		result.title == ['Make code always']
	}

	String getCurrentAccessTokenForMember() {
		Authentication authentication = Authentication.build(
				'member',
				['MEMBER']
		)
		return tokenGenerator.generateToken(authentication, 3600).orElse('')
	}

	String getCurrentAccessTokenForAdmin() {
		Authentication authentication = Authentication.build(
				'admin',
				['ADMIN']
		)
		return tokenGenerator.generateToken(authentication, 3600).orElse('')
	}
}
