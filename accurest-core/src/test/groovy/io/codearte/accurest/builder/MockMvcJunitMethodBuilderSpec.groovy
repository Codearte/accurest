package io.codearte.accurest.builder

import io.codearte.accurest.dsl.GroovyDsl
import io.codearte.accurest.dsl.WireMockStubStrategy
import io.codearte.accurest.dsl.WireMockStubVerifier
import spock.lang.Issue
import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Olga Maciaszek-Sharma
 * @since 2015-08-07
 */
class MockMvcJunitMethodBuilderSpec extends Specification implements WireMockStubVerifier {

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
			blockBuilder.toString().contains('assertThat(parsedJson.read("$[?(@.property1 == \'a\')]", JSONArray.class)).isNotEmpty();')
			blockBuilder.toString().contains('assertThat(parsedJson.read("$[?(@.property2 == \'b\')]", JSONArray.class)).isNotEmpty();')
		and:
			stubMappingIsValidWireMockStub(new WireMockStubStrategy(contractDsl).toWireMockClientStub())
	}

	@Issue("#187")
	def "should generate assertions for null and boolean values"() {
		given:
			GroovyDsl contractDsl = GroovyDsl.make {
				request {
					method "GET"
					url "test"
				}
				response {
					status 200
					body """{
	"property1": "true",
	"property2": null,
	"property3": false
}"""
				}
			}
			MockMvcJUnitMethodBodyBuilder builder = new MockMvcJUnitMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
		then:
			blockBuilder.toString().contains("\$[?(@.property1 == 'true')]")
			blockBuilder.toString().contains("\$[?(@.property2 == null)]")
			blockBuilder.toString().contains("\$[?(@.property3 == false)]")
		and:
			stubMappingIsValidWireMockStub(new WireMockStubStrategy(contractDsl).toWireMockClientStub())
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
			blockBuilder.toString().contains("\$[?(@.property1 == 'a')]")
			blockBuilder.toString().contains("\$.property2[*][?(@.a == 'sth')]")
			blockBuilder.toString().contains("\$.property2[*][?(@.b == 'sthElse')]")
		and:
			stubMappingIsValidWireMockStub(new WireMockStubStrategy(contractDsl).toWireMockClientStub())
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
			blockBuilder.toString().contains('.body("{\\"items\\":[\\"HOP\\"]}")')
		and:
			stubMappingIsValidWireMockStub(new WireMockStubStrategy(contractDsl).toWireMockClientStub())
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
			blockBuilder.toString().contains('.body("\\"property1=VAL1\\"")')
		and:
			stubMappingIsValidWireMockStub(new WireMockStubStrategy(contractDsl).toWireMockClientStub())
	}

	@Issue("185")
	def "should generate assertions for a response body containing map with integers as keys"() {
		given:
			GroovyDsl contractDsl = GroovyDsl.make {
				request {
					method "GET"
					url "test"
				}
				response {
					status 200
					body(
							property: [
									14: 0.0,
									7 : 0.0
							]
					)
				}
			}
			MockMvcJUnitMethodBodyBuilder builder = new MockMvcJUnitMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
		then:
			blockBuilder.toString().contains("\$.property[?(@.7 == 0.0)]")
			blockBuilder.toString().contains("\$.property[?(@.14 == 0.0)]")
		and:
			stubMappingIsValidWireMockStub(new WireMockStubStrategy(contractDsl).toWireMockClientStub())
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
			blockBuilder.toString().contains("\$[*][?(@.property1 == 'a')]")
			blockBuilder.toString().contains("\$[*][?(@.property2 == 'b')]")
		and:
			stubMappingIsValidWireMockStub(new WireMockStubStrategy(contractDsl).toWireMockClientStub())
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
			blockBuilder.toString().contains("\$.property1[*][?(@.property3 == 'test2')]")
			blockBuilder.toString().contains("\$.property1[*][?(@.property2 == 'test1')]")
		and:
			stubMappingIsValidWireMockStub(new WireMockStubStrategy(contractDsl).toWireMockClientStub())
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
			blockBuilder.toString().contains("\$.property2[?(@.property3 == 'b')]")
			blockBuilder.toString().contains("\$[?(@.property1 == 'a')]")
		and:
			stubMappingIsValidWireMockStub(new WireMockStubStrategy(contractDsl).toWireMockClientStub())
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
			blockBuilder.toString().contains("\$[?(@.property2 =~ /[0-9]{3}/)]")
			blockBuilder.toString().contains("\$[?(@.property1 == 'a')]")
		and:
			stubMappingIsValidWireMockStub(new WireMockStubStrategy(contractDsl).toWireMockClientStub())
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
			blockBuilder.toString().contains("\$[?(@.property2 =~ /[0-9]{3}/)]")
			blockBuilder.toString().contains("\$[?(@.property1 == 'a')]")
		and:
			stubMappingIsValidWireMockStub(new WireMockStubStrategy(contractDsl).toWireMockClientStub())
	}

	@Issue(["#126", "#143"])
	// TODO: verify entire generated test class
	def "should generate escaped regex assertions for string objects in response body"() {
		given:
			GroovyDsl contractDsl = GroovyDsl.make {
				request {
					method "GET"
					url "test"
				}
				response {
					status 200
					body("""{"property":"  ${value(client('123'), server(regex('\\d+')))}"}""")
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
			blockBuilder.toString().contains("\$[?(@.property =~ /\\d+/)]")
		and:
			stubMappingIsValidWireMockStub(new WireMockStubStrategy(contractDsl).toWireMockClientStub())
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
			def jUnitTest = blockBuilder.toString()
		then:
			jUnitTest.contains('get("/users?limit=10&offset=20&filter=email&sort=name&search=55&age=99&name=Denis.Stepanov&email=bob@email.com")')
			jUnitTest.contains('$[?(@.property2 == \'b\')]')
			jUnitTest.contains('$[?(@.property1 == \'a\')]')
		and:
			stubMappingIsValidWireMockStub(new WireMockStubStrategy(contractDsl).toWireMockClientStub())
	}

	@Issue('#169')
	def "should generate a call with an url path and query parameters with url containing a pattern"() {
		given:
			GroovyDsl contractDsl = GroovyDsl.make {
				request {
					method 'GET'
					url($(stub(regex('/foo/[0-9]+')), test('/foo/123456'))) {
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
			def jUnitTest = blockBuilder.toString()
		then:
			jUnitTest.contains('get("/foo/123456?limit=10&offset=20&filter=email&sort=name&search=55&age=99&name=Denis.Stepanov&email=bob@email.com")')
			jUnitTest.contains('$[?(@.property2 == \'b\')]')
			jUnitTest.contains('$[?(@.property1 == \'a\')]')
		and:
			stubMappingIsValidWireMockStub(new WireMockStubStrategy(contractDsl).toWireMockClientStub())
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
			def jUnitTest = blockBuilder.toString()
		then:
			jUnitTest.contains('"\\"\\""')
		and:
			stubMappingIsValidWireMockStub(new WireMockStubStrategy(contractDsl).toWireMockClientStub())
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
			def jUnitTest = blockBuilder.toString()
		then:
			jUnitTest.contains('Object responseBody = (response.getBody().asString())')
			jUnitTest.contains('assertThat(responseBody).isEqualTo("test")')
		and:
			stubMappingIsValidWireMockStub(new WireMockStubStrategy(contractDsl).toWireMockClientStub())
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
			def jUnitTest = blockBuilder.toString()
		then:
			jUnitTest.contains('response.header("Location")).matches("http://localhost/partners/[0-9]+/users/[0-9]+")')
		and:
			stubMappingIsValidWireMockStub(new WireMockStubStrategy(contractDsl).toWireMockClientStub())
	}

	@Issue('115')
	def "should generate regex with helper method"() {
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
						header 'Location': $(client('http://localhost/partners/1000/users/1001'), server(regex("^${hostname()}/partners/[0-9]+/users/[0-9]+")))
					}
				}
			}
			MockMvcJUnitMethodBodyBuilder builder = new MockMvcJUnitMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def jUnitTest = blockBuilder.toString()
		then:
			jUnitTest.contains('assertThat(response.header("Location")).matches("^((http[s]?|ftp):\\/)\\/?([^:\\/\\s]+)(:[0-9]{1,5})?/partners/[0-9]+/users/[0-9]+")')
		and:
			stubMappingIsValidWireMockStub(new WireMockStubStrategy(contractDsl).toWireMockClientStub())
	}

	def "should work with more complex stuff and jsonpaths"() {
		given:
			GroovyDsl contractDsl = GroovyDsl.make {
				priority 10
				request {
					method 'POST'
					url '/validation/client'
					headers {
						header 'Content-Type': 'application/json'
					}
					body(
							bank_account_number: '0014282912345698765432161182',
							email: 'foo@bar.com',
							phone_number: '100299300',
							personal_id: 'ABC123456'
					)
				}

				response {
					status 200
					body(errors: [
							[property: "bank_account_number", message: "incorrect_format"]
					])
				}
			}
			MockMvcJUnitMethodBodyBuilder builder = new MockMvcJUnitMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def jUnitTest = blockBuilder.toString()
		then:
			jUnitTest.contains('''$.errors[*][?(@.property == 'bank_account_number')]''')
			jUnitTest.contains('''$.errors[*][?(@.message == 'incorrect_format')]''')
		and:
			stubMappingIsValidWireMockStub(new WireMockStubStrategy(contractDsl).toWireMockClientStub())
	}

	def "should work properly with GString url"() {
		given:
			GroovyDsl contractDsl = GroovyDsl.make {

				request {
					method 'PUT'
					url "/partners/${value(client(regex('^[0-9]*$')), server('11'))}/agents/11/customers/09665703Z"
					headers {
						header 'Content-Type': 'application/json'
					}
					body(
							first_name: 'Josef',
					)
				}
				response {
					status 422
				}
			}
			MockMvcJUnitMethodBodyBuilder builder = new MockMvcJUnitMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def jUnitTest = blockBuilder.toString()
		then:
			jUnitTest.contains('''/partners/11/agents/11/customers/09665703Z''')
		and:
			stubMappingIsValidWireMockStub(new WireMockStubStrategy(contractDsl).toWireMockClientStub())
	}

	def "should resolve properties in GString with regular expression"() {
		given:
			GroovyDsl contractDsl = GroovyDsl.make {
				priority 1
				request {
					method 'POST'
					url '/users/password'
					headers {
						header 'Content-Type': 'application/json'
					}
					body(
							email: $(client(regex(email())), server('not.existing@user.com')),
							callback_url: $(client(regex(hostname())), server('http://partners.com'))
					)
				}
				response {
					status 404
					headers {
						header 'Content-Type': 'application/json'
					}
					body(
							code: 4,
							message: "User not found by email = [${value(server(regex(email())), client('not.existing@user.com'))}]"
					)
				}
			}
			MockMvcJUnitMethodBodyBuilder builder = new MockMvcJUnitMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def jUnitTest = blockBuilder.toString()
		then:
			jUnitTest.contains('''$[?(@.message =~ /User not found by email = \\\\[[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\\\.[a-zA-Z]{2,4}\\\\]/)]''')
	}


	@Issue('42')
	@Unroll
	def "should not omit the optional field in the test creation"() {
		given:
			MockMvcJUnitMethodBodyBuilder builder = new MockMvcJUnitMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def jUnitTest = blockBuilder.toString()
		then:
			jUnitTest.contains('\\"email\\":\\"abc@abc.com\\"')
			jUnitTest.contains('parsedJson.read("$[?(@.code =~ /(123123)?/)]"')
			!jUnitTest.contains('''REGEXP''')
			!jUnitTest.contains('''OPTIONAL''')
			!jUnitTest.contains('''OptionalProperty''')
		where:
			contractDsl << [
					GroovyDsl.make {
						priority 1
						request {
							method 'POST'
							url '/users/password'
							headers {
								header 'Content-Type': 'application/json'
							}
							body(
									email: $(stub(optional(regex(email()))), test('abc@abc.com')),
									callback_url: $(stub(regex(hostname())), test('http://partners.com'))
							)
						}
						response {
							status 404
							headers {
								header 'Content-Type': 'application/json'
							}
							body(
									code: value(stub("123123"), test(optional("123123"))),
									message: "User not found by email = [${value(test(regex(email())), stub('not.existing@user.com'))}]"
							)
						}
					},
					GroovyDsl.make {
						priority 1
						request {
							method 'POST'
							url '/users/password'
							headers {
								header 'Content-Type': 'application/json'
							}
							body(
									""" {
								"email" : "${value(stub(optional(regex(email()))), test('abc@abc.com'))}",
								"callback_url" : "${value(client(regex(hostname())), server('http://partners.com'))}"
								}
							"""
							)
						}
						response {
							status 404
							headers {
								header 'Content-Type': 'application/json'
							}
							body(
									""" {
								"code" : "${value(stub(123123), test(optional(123123)))}",
								"message" : "User not found by email = [${value(server(regex(email())), client('not.existing@user.com'))}]"
								}
							"""
							)
						}
					}
			]
	}

	@Issue('72')
	def "should make the execute method work"() {
		given:
			GroovyDsl contractDsl = GroovyDsl.make {
				request {
					method """PUT"""
					url """/fraudcheck"""
					body("""
                        {
                        "clientPesel":"${value(client(regex('[0-9]{10}')), server('1234567890'))}",
                        "loanAmount":123.123
                        }
                    """
					)
					headers {
						header("""Content-Type""", """application/vnd.fraud.v1+json""")

					}

				}
				response {
					status 200
					body( """{
    "fraudCheckStatus": "OK",
    "rejectionReason": ${value(client(null), server(execute('assertThatRejectionReasonIsNull($it)')))}
}""")
					headers {
						header('Content-Type': 'application/vnd.fraud.v1+json')

					}

				}

			}
			MockMvcJUnitMethodBodyBuilder builder = new MockMvcJUnitMethodBodyBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def jUnitTest = blockBuilder.toString()
		then:
			jUnitTest.contains('''assertThatRejectionReasonIsNull(parsedJson.read('$.rejectionReason'))''')
	}


}
