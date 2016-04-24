package io.codearte.accurest.wiremock

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import io.codearte.accurest.dsl.GroovyDsl
import io.codearte.accurest.util.GroovyShellBuilder
import spock.lang.Specification

class WireMockToDslConverterSpec extends Specification {

	def 'should produce a Groovy DSL from WireMock stub'() {
		given:
			String wireMockStub = '''\
{
	"request": {
		"method": "GET",
		"url": "/path",
		"headers" : {
			"Accept": {
				"matches": "text/.*"
			},
			"X-Custom-Header": {
				"contains": "2134"
			}
		}
	},
	"response": {
		"status": 200,
		"body": '{"id": { "value": "132" }, "surname": "Kowalsky", "name": "Jan", "created": "2014-02-02 12:23:43" }',
		"headers": {
			"Content-Type": "text/plain"
		}
	}
}
'''
		and:
			stubMappingIsValidWireMockStub(wireMockStub)
		and:
			GroovyDsl expectedGroovyDsl = GroovyDsl.make {
				request {
					method 'GET'
					url '/path'
					headers {
						header('Accept': $(
								client(regex('text/.*')),
								server('text/plain')
						))
						header('X-Custom-Header': $(
								client(regex('^.*2134.*$')),
								server('121345')
						))

					}
				}
				response {
					status 200
					body(
							id: [value: '132'],
							surname: 'Kowalsky',
							name: 'Jan',
							created: '2014-02-02 12:23:43'
					)
					headers {
						header 'Content-Type': 'text/plain'

					}
				}
			}
		when:
			String groovyDsl = WireMockToDslConverter.fromWireMockStub(wireMockStub)
		then:
			GroovyShellBuilder.build(this.class.classLoader).evaluate(
					""" io.codearte.accurest.dsl.GroovyDsl.make {
				$groovyDsl
			}""") == expectedGroovyDsl
	}


	def 'should convert WireMock stub with response body containing JSON with escaped double quotes'() {
		given:
			String wireMockStub = '''\
{
	"request": {
		"method": "DELETE",
		"urlPattern": "/credit-card-verification-data/[0-9]+",
		"headers": {
			"Content-Type": {
				"equalTo": "application/vnd.mymoid-adapter.v2+json; charset=UTF-8"
			}
		}
	},
	"response": {
		"status": 200,
		"body": "{\\"status\\": \\"OK\\"}",
		"headers": {
			"Content-Type": "application/json"
		}
	}
}
'''
		and:
			stubMappingIsValidWireMockStub(wireMockStub)
		and:
			GroovyDsl expectedGroovyDsl = GroovyDsl.make {
				request {
					method 'DELETE'
					url $(client(~/\/credit-card-verification-data\/[0-9]+/), server('/credit-card-verification-data/1'))
					headers {
						header('Content-Type': 'application/vnd.mymoid-adapter.v2+json; charset=UTF-8')
					}
				}
				response {
					status 200
					body("""{
	"status": "OK"
}""")
					headers {
						header 'Content-Type': 'application/json'

					}
				}
			}
		when:
			String groovyDsl = WireMockToDslConverter.fromWireMockStub(wireMockStub)
		then:
			GroovyShellBuilder.build(this.class.classLoader).evaluate(
					""" io.codearte.accurest.dsl.GroovyDsl.make {
				$groovyDsl
			}""") == expectedGroovyDsl
	}

	def 'should convert WireMock stub with response body containing integer'() {
		given:
			String wireMockStub = '''\
{
  "request": {
	"method": "POST",
	"url": "/charge/count",
	"headers": {
	  "Content-Type": {
		"equalTo": "application/vnd.creditcard-reporter.v1+json"
	  }
	}
  },
  "response": {
	"status": 200,
	"body": 200,
	"headers": {
	  "Content-Type": "application/json"
	}
  }
}
'''
		and:
			stubMappingIsValidWireMockStub(wireMockStub)
		and:
			GroovyDsl expectedGroovyDsl = GroovyDsl.make {
				request {
					method 'POST'
					url '/charge/count'
					headers {
						header('Content-Type': 'application/vnd.creditcard-reporter.v1+json')
					}
				}
				response {
					status 200
					body(200)
					headers {
						header 'Content-Type': 'application/json'

					}
				}
			}
		when:
			String groovyDsl = WireMockToDslConverter.fromWireMockStub(wireMockStub)
		then:
			GroovyShellBuilder.build(this.class.classLoader).evaluate(
					""" io.codearte.accurest.dsl.GroovyDsl.make {
				$groovyDsl
			}""") == expectedGroovyDsl
	}

	def 'should convert WireMock stub with response body as a list'() {
		given:
			String wireMockStub = '''\
{
  "request": {
	"method": "POST",
	"url": "/charge/count",
	"headers": {
	  "Content-Type": {
		"equalTo": "application/vnd.creditcard-reporter.v1+json"
	  }
	}
  },
  "response": {
	"status": 200,
	"body": '[ {"a":1, "c":"3"}, "b", "a" ]',
	"headers": {
	  "Content-Type": "application/json"
	}
  }
}
'''
		and:
			stubMappingIsValidWireMockStub(wireMockStub)
		and:
			GroovyDsl expectedGroovyDsl = GroovyDsl.make {
				request {
					method 'POST'
					url '/charge/count'
					headers {
						header('Content-Type': 'application/vnd.creditcard-reporter.v1+json')
					}
				}
				response {
					status 200
					body([
							[a: 1, c: '3'],
							'b',
							'a'
					])
					headers {
						header 'Content-Type': 'application/json'
					}
				}
			}
		when:
			String groovyDsl = WireMockToDslConverter.fromWireMockStub(wireMockStub)
		then:
			GroovyShellBuilder.build(this.class.classLoader).evaluate(
					""" io.codearte.accurest.dsl.GroovyDsl.make {
				$groovyDsl
			}""") == expectedGroovyDsl
	}

	def 'should convert WireMock stub with response body containing a nested list'() {
		given:
			String wireMockStub = '''\
{
  "request": {
	"method": "POST",
	"url": "/charge/search?pageNumber=0&size=2147483647",
	"headers": {
	  "Content-Type": {
		"equalTo": "application/vnd.creditcard-reporter.v1+json"
	  }
	}
  },
  "response": {
	"status": 200,
	"body": '[{"amount":1.01, "name":"Name", "info":{"title":"title1", "payload":null}, "booleanvalue":true, "user":null}, {"amount":2.01, "name":"Name2", "info":{"title":"title2", "payload":null}, "booleanvalue":true, "user":null}]'
	}
}
'''
		and:
			stubMappingIsValidWireMockStub(wireMockStub)
		and:
			GroovyDsl expectedGroovyDsl = GroovyDsl.make {
				request {
					method 'POST'
					url '/charge/search?pageNumber=0&size=2147483647'
					headers {
						header('Content-Type': 'application/vnd.creditcard-reporter.v1+json')
					}
				}
				response {
					status 200
					body("""[
	{
		"amount": 1.01,
		"name": "Name",
		"info": {
			"title": "title1",
			"payload": null
		},
		"booleanvalue": true,
		"user": null
	},
	{
		"amount": 2.01,
		"name": "Name2",
		"info": {
			"title": "title2",
			"payload": null
		},
		"booleanvalue": true,
		"user": null
	}
]""")
				}
			}
		when:
			String groovyDsl = WireMockToDslConverter.fromWireMockStub(wireMockStub)
		then:
			GroovyShellBuilder.build(this.class.classLoader).evaluate(
					""" io.codearte.accurest.dsl.GroovyDsl.make {
				$groovyDsl
			}""") == expectedGroovyDsl
	}

	def 'should convert WireMock stub with request body checking equality to Json'() {
		given:
			String wireMockStub = '''\
{
  "request": {
	"method": "POST",
	"url": "/test",
	"bodyPatterns": [{
		"equalTo": '{"property1":"abc", "property2":"2017-01", "property3":"666", "property4":1428566412}'
	}]
  },
  "response": {
	"status": 200
	}
}
'''
		and:
			stubMappingIsValidWireMockStub(wireMockStub)
		and:
			GroovyDsl expectedGroovyDsl = GroovyDsl.make {
				request {
					method 'POST'
					url '/test'
					body ('''{"property1":"abc","property2":"2017-01","property3":"666","property4":1428566412}''')
				}
				response {
					status 200
				}
			}
		when:
			String groovyDsl = WireMockToDslConverter.fromWireMockStub(wireMockStub)
		then:
			GroovyDsl evaluatedGroovyDsl = GroovyShellBuilder.build(this.class.classLoader).evaluate(
					""" io.codearte.accurest.dsl.GroovyDsl.make {
				$groovyDsl
			}""")
		and:
			evaluatedGroovyDsl == expectedGroovyDsl
	}

	def 'should convert WireMock stub with request body checking matching to Json'() {
		given:
			String wireMockStub = '''\
{
  "request": {
	"method": "POST",
	"url": "/test",
	"bodyPatterns": [{
		"matches": "[0-9]{5}"
	}]
  },
  "response": {
	"status": 200
	}
}
'''
		and:
			stubMappingIsValidWireMockStub(wireMockStub)
		and:
			GroovyDsl expectedGroovyDsl = GroovyDsl.make {
				request {
					method 'POST'
					url '/test'
					body $(client(~/[0-9]{5}/), server('12345'))
				}
				response {
					status 200
				}
			}
		when:
			String groovyDsl = WireMockToDslConverter.fromWireMockStub(wireMockStub)
		then:
			GroovyDsl evaluatedGroovyDsl = GroovyShellBuilder.build(this.class.classLoader).evaluate(
					""" io.codearte.accurest.dsl.GroovyDsl.make {
				$groovyDsl
			}""")
		and:
			evaluatedGroovyDsl == expectedGroovyDsl
	}

	def 'should convert WireMock stub with request body with equalToJson'() {
		given:
			String wireMockStub = '''\
{
  "request" : {
	"url" : "/test",
	"method" : "POST",
	"bodyPatterns" : [ {
	  "equalToJson" : '{"pan":"4855141150107894", "expirationDate":"2017-01", "dcvx":"178"}',
	  "jsonCompareMode" : "LENIENT"
	} ]
  },
  "response" : {
	"status" : 200
  }
}
'''
		and:
			stubMappingIsValidWireMockStub(wireMockStub)
		and:
			GroovyDsl expectedGroovyDsl = GroovyDsl.make {
				request {
					method 'POST'
					url '/test'
					body '''{"pan":"4855141150107894","expirationDate":"2017-01","dcvx":"178"}'''
				}
				response {
					status 200
				}
			}
		when:
			String groovyDsl = WireMockToDslConverter.fromWireMockStub(wireMockStub)
		then:
			GroovyDsl evaluatedGroovyDsl = GroovyShellBuilder.build(this.class.classLoader).evaluate(
					""" io.codearte.accurest.dsl.GroovyDsl.make {
				$groovyDsl
			}""")
		and:
			evaluatedGroovyDsl == expectedGroovyDsl
	}

	def 'should convert WireMock stub with request body with equalTo'() {
		given:
			String wireMockStub = '''\
			{
			  "request" : {
				"url" : "/test",
				"method" : "POST",
				"bodyPatterns" : [ {
				  "equalTo" : '{"pan":"4855141150107894", "expirationDate":"2017-01", "dcvx":"178"}'
				} ]
			  },
			  "response" : {
				"status" : 200
			  }
			}
			'''
		and:
			stubMappingIsValidWireMockStub(wireMockStub)
		and:
			GroovyDsl expectedGroovyDsl = GroovyDsl.make {
				request {
					method 'POST'
					url '/test'
					body '''{"pan":"4855141150107894","expirationDate":"2017-01","dcvx":"178"}'''
				}
				response {
					status 200
				}
			}
		when:
			String groovyDsl = WireMockToDslConverter.fromWireMockStub(wireMockStub)
		then:
			GroovyDsl evaluatedGroovyDsl = GroovyShellBuilder.build(this.class.classLoader).evaluate(
					""" io.codearte.accurest.dsl.GroovyDsl.make {
					$groovyDsl
				}""")
		and:
			evaluatedGroovyDsl == expectedGroovyDsl
	}

	def 'should convert WireMock stub with request body with matches'() {
		given:
			String wireMockStub = '''\
			{
			  "request" : {
				"url" : "/test",
				"method" : "POST",
				"bodyPatterns" : [ {
				  "matches" : "[0-9]{2}"
				} ]
			  },
			  "response" : {
				"status" : 200
			  }
			}
			'''
		and:
			stubMappingIsValidWireMockStub(wireMockStub)
		and:
			GroovyDsl expectedGroovyDsl = GroovyDsl.make {
				request {
					method 'POST'
					url '/test'
					body $(client(~/[0-9]{2}/), server('12'))
				}
				response {
					status 200
				}
			}
		when:
			String groovyDsl = WireMockToDslConverter.fromWireMockStub(wireMockStub)
		then:
			GroovyDsl evaluatedGroovyDsl = GroovyShellBuilder.build(this.class.classLoader).evaluate(
					""" io.codearte.accurest.dsl.GroovyDsl.make {
					$groovyDsl
				}""")
		and:
			evaluatedGroovyDsl == expectedGroovyDsl
	}

	def 'should convert WireMock stub with priorities'() {
		given:
			String wireMockStub = '''\
			{
			  "priority" : 2,
			  "request" : {
				"url" : "/test",
				"method" : "POST"
			  },
			  "response" : {
				"status" : 200
			  }
			}
			'''
		and:
			stubMappingIsValidWireMockStub(wireMockStub)
		and:
			GroovyDsl expectedGroovyDsl = GroovyDsl.make {
				priority 2
				request {
					method 'POST'
					url '/test'
				}
				response {
					status 200
				}
			}
		when:
			String groovyDsl = WireMockToDslConverter.fromWireMockStub(wireMockStub)
		then:
			GroovyDsl evaluatedGroovyDsl = GroovyShellBuilder.build(this.class.classLoader).evaluate(
					""" io.codearte.accurest.dsl.GroovyDsl.make {
					$groovyDsl
				}""")
		and:
			evaluatedGroovyDsl == expectedGroovyDsl
	}

	void stubMappingIsValidWireMockStub(String mappingDefinition) {
		StubMapping.buildFrom(mappingDefinition)
	}
}
