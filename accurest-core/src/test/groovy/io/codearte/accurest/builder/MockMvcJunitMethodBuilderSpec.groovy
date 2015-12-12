package io.codearte.accurest.builder

import io.codearte.accurest.dsl.GroovyDsl
import spock.lang.Issue
import spock.lang.Specification

/**
 * @author Olga Maciaszek-Sharma
 * @since 2015-08-07
 */
class MockMvcJunitMethodBuilderSpec extends Specification {

	def "should generate assertions for simple response body"() {
		given:
			GroovyDsl contractDsl = GroovyDsl.make {
				request {
					method "GET"
					url "test"
				}
				response {
					status 200
					body """{
    "property1": "a",
    "property2": "b"
}"""
				}
			}
			MockMvcJUnitMethodBodyBuilder builder = new MockMvcJUnitMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
		then:
			blockBuilder.toString().contains('assertThat(((Map<String, Object>) responseBody).get("property1")).isEqualTo("a");')
			blockBuilder.toString().contains('assertThat(((Map<String, Object>) responseBody).get("property2")).isEqualTo("b");')
	}

	@Issue("#79")
	def "should generate assertions for simple response body constructed from map with a list"() {
		given:
			GroovyDsl contractDsl = GroovyDsl.make {
				request {
					method "GET"
					url "test"
				}
				response {
					status 200
					body(
							property1: 'a',
							property2: [
									[a: 'sth'],
									[b: 'sthElse']
							]
					)
				}
			}
			MockMvcJUnitMethodBodyBuilder builder = new MockMvcJUnitMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
		then:
			blockBuilder.toString().contains('((Map<String, Object>) responseBody).get("property1")).isEqualTo("a");')
			blockBuilder.toString().contains('((Map<String, List<Map<String, Object>>>) responseBody).get("property2").get(0).get("a")).isEqualTo("sth");')
			blockBuilder.toString().contains('((Map<String, List<Map<String, Object>>>) responseBody).get("property2").get(1).get("b")).isEqualTo("sthElse");')
	}

	@Issue("#82")
	def "should generate proper request when body constructed from map with a list"() {
		given:
			GroovyDsl contractDsl = GroovyDsl.make {
				request {
					method "GET"
					url "test"
					body(
							items: ['HOP']
					)
				}
				response {
					status 200
				}
			}
			MockMvcJUnitMethodBodyBuilder builder = new MockMvcJUnitMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
		then:
			blockBuilder.toString().contains(".body('{\"items\":[\"HOP\"]}')")
	}

	@Issue("#88")
	def "should generate proper request when body constructed from GString"() {
		given:
			GroovyDsl contractDsl = GroovyDsl.make {
				request {
					method "GET"
					url "test"
					body(
							"property1=VAL1"
					)
				}
				response {
					status 200
				}
			}
			MockMvcJUnitMethodBodyBuilder builder = new MockMvcJUnitMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
		then:
			blockBuilder.toString().contains(".body('property1=VAL1')")
	}

	def "should generate assertions for array in response body"() {
		given:
			GroovyDsl contractDsl = GroovyDsl.make {
				request {
					method "GET"
					url "test"
				}
				response {
					status 200
					body """[
{
    "property1": "a"
},
{
    "property2": "b"
}]"""
				}
			}
			MockMvcJUnitMethodBodyBuilder builder = new MockMvcJUnitMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
		then:
			blockBuilder.toString().contains('((List<Map<String, Object>>) responseBody).get(0).get("property1")).isEqualTo("a");')
			blockBuilder.toString().contains('((List<Map<String, Object>>) responseBody).get(1).get("property2")).isEqualTo("b");')
	}

	def "should generate assertions for array inside response body element"() {
		given:
			GroovyDsl contractDsl = GroovyDsl.make {
				request {
					method "GET"
					url "test"
				}
				response {
					status 200
					body """{
    "property1": [
    { "property2": "test1"},
    { "property3": "test2"}
    ]
}"""
				}
			}
			MockMvcJUnitMethodBodyBuilder builder = new MockMvcJUnitMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
		then:
			blockBuilder.toString().contains('((Map<String, List<Map<String, Object>>>) responseBody).get("property1").get(0).get("property2")).isEqualTo("test1");')
			blockBuilder.toString().contains('((Map<String, List<Map<String, Object>>>) responseBody).get("property1").get(1).get("property3")).isEqualTo("test2");')
	}

	def "should generate assertions for nested objects in response body"() {
		given:
			GroovyDsl contractDsl = GroovyDsl.make {
				request {
					method "GET"
					url "test"
				}
				response {
					status 200
					body '''\
{
    "property1": "a",
    "property2": {"property3": "b"}
}
'''
				}
			}
			MockMvcJUnitMethodBodyBuilder builder = new MockMvcJUnitMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
		then:
			blockBuilder.toString().contains('((Map<String, Object>) responseBody).get("property1")).isEqualTo("a");')
			blockBuilder.toString().contains('((Map<String, Map<String, Object>>) responseBody).get("property2").get("property3")).isEqualTo("b");')
	}

	def "should generate regex assertions for map objects in response body"() {
		given:
			GroovyDsl contractDsl = GroovyDsl.make {
				request {
					method "GET"
					url "test"
				}
				response {
					status 200
					body(
							property1: "a",
							property2: value(
									client('123'),
									server(regex('[0-9]{3}'))
							)
					)
					headers {
						header('Content-Type': 'application/json')

					}

				}
			}
			MockMvcJUnitMethodBodyBuilder builder = new MockMvcJUnitMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
		then:
			blockBuilder.toString().contains('assertThat(((Map<String, Object>) responseBody).get("property1")).isEqualTo("a");')
			blockBuilder.toString().contains('assertThat(((Map<String, Object>) responseBody).get("property2")).matches("[0-9]{3}");')
	}

	def "should generate regex assertions for string objects in response body"() {
		given:
			GroovyDsl contractDsl = GroovyDsl.make {
				request {
					method "GET"
					url "test"
				}
				response {
					status 200
					body("""{"property1":"a","property2":"${value(client('123'), server(regex('[0-9]{3}')))}"}""")
					headers {
						header('Content-Type': 'application/json')

					}

				}
			}
			MockMvcJUnitMethodBodyBuilder builder = new MockMvcJUnitMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
		then:
			blockBuilder.toString().contains('assertThat(((Map<String, Object>) responseBody).get("property1")).isEqualTo("a");')
			blockBuilder.toString().contains('assertThat(((Map<String, Object>) responseBody).get("property2")).matches("[0-9]{3}");')
	}

	def "should generate a call with an url path and query parameters"() {
		given:
			GroovyDsl contractDsl = GroovyDsl.make {
				request {
					method 'GET'
					urlPath('/users') {
						queryParameters {
							parameter 'limit': $(client(equalTo("20")), server(equalTo("10")))
							parameter 'offset': $(client(containing("20")), server(equalTo("20")))
							parameter 'filter': "email"
							parameter 'sort': equalTo("name")
							parameter 'search': $(client(notMatching(~/^\/[0-9]{2}$/)), server("55"))
							parameter 'age': $(client(notMatching("^\\w*\$")), server("99"))
							parameter 'name': $(client(matching("Denis.*")), server("Denis.Stepanov"))
							parameter 'email': "bob@email.com"
							parameter 'hello': $(client(matching("Denis.*")), server(absent()))
							parameter 'hello': absent()
						}
					}
				}
				response {
					status 200
					body """
					{
						"property1": "a",
						"property2": "b"
					}
					"""
				}
			}
			MockMvcJUnitMethodBodyBuilder builder = new MockMvcJUnitMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def junitTest = blockBuilder.toString()
		then:
			junitTest.contains('get("/users?limit=10&offset=20&filter=email&sort=name&search=55&age=99&name=Denis.Stepanov&email=bob@email.com")')
			junitTest.contains('assertThat(((Map<String, Object>) responseBody).get("property1")).isEqualTo("a");')
			junitTest.contains('assertThat(((Map<String, Object>) responseBody).get("property2")).isEqualTo("b");')
	}

	def "should generate test for empty body"() {
		given:
			GroovyDsl contractDsl = GroovyDsl.make {
				request {
					method('POST')
					url("/ws/payments")
					body("")
				}
				response {
					status 406
				}
			}
			MockMvcJUnitMethodBodyBuilder builder = new MockMvcJUnitMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def junitTest = blockBuilder.toString()
		then:
			junitTest.contains(".body('')")
	}

	def "should generate test for String in response body"() {
		given:
			GroovyDsl contractDsl = GroovyDsl.make {
				request {
					method "POST"
					url "test"
				}
				response {
					status 200
					body "test"
				}
			}
			MockMvcJUnitMethodBodyBuilder builder = new MockMvcJUnitMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def junitTest = blockBuilder.toString()
		then:
			junitTest.contains('String responseBody = (response.getBody().asString()')
			junitTest.contains('assertThat(responseBody).isEqualTo("test")')
	}

	@Issue('113')
	def "should generate regex test for String in response header"() {
		given:
			GroovyDsl contractDsl = GroovyDsl.make {
				request {
					method 'POST'
					url $(client(regex('/partners/[0-9]+/users')), server('/partners/1000/users'))
					headers { header 'Content-Type': 'application/json' }
					body(
							first_name: 'John',
							last_name: 'Smith',
							personal_id: '12345678901',
							phone_number: '500500500',
							invitation_token: '00fec7141bb94793bfe7ae1d0f39bda0',
							password: 'john'
					)
				}
				response {
					status 201
					headers {
						header 'Location': $(client('http://localhost/partners/1000/users/1001'), server(regex('http://localhost/partners/[0-9]+/users/[0-9]+')))
					}
				}
			}
			MockMvcJUnitMethodBodyBuilder builder = new MockMvcJUnitMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def junitTest = blockBuilder.toString()
		then:
			junitTest.contains('assertThat(response.header("Location")).matches("http://localhost/partners/[0-9]+/users/[0-9]+");')
	}
}
