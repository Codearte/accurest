package com.blogspot.toomuchcoding

import com.blogspot.toomuchcoding.frauddetection.Application
import com.blogspot.toomuchcoding.frauddetection.LoanApplicationService
import com.blogspot.toomuchcoding.frauddetection.model.Client
import com.blogspot.toomuchcoding.frauddetection.model.LoanApplication
import com.blogspot.toomuchcoding.frauddetection.model.LoanApplicationResult
import com.blogspot.toomuchcoding.frauddetection.model.LoanApplicationStatus
import com.github.tomakehurst.wiremock.junit.WireMockClassRule
import org.junit.ClassRule
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootContextLoader
import org.springframework.test.context.ContextConfiguration
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

@ContextConfiguration(loader = SpringBootContextLoader, classes = Application)
@Stepwise
class LoanApplicationServiceSpec extends Specification {

	public static int port = org.springframework.util.SocketUtils.findAvailableTcpPort()

	@ClassRule
	@Shared
	WireMockClassRule wireMockRule = new WireMockClassRule(port)

	@Autowired
	LoanApplicationService sut

	def setup() {
		sut.port = port
	}

	def 'should successfully apply for loan'() {
		given:
			LoanApplication application =
					new LoanApplication(client: new Client(pesel: '1234567890'), amount: 123.123)
		when:
			LoanApplicationResult loanApplication = sut.loanApplication(application)
		then:
			loanApplication.loanApplicationStatus == LoanApplicationStatus.LOAN_APPLIED
			loanApplication.rejectionReason == null
	}

	def 'should be rejected due to abnormal loan amount'() {
		given:
			LoanApplication application =
					new LoanApplication(client: new Client(pesel: '1234567890'), amount: 99_999)
		when:
			LoanApplicationResult loanApplication = sut.loanApplication(application)
		then:
			loanApplication.loanApplicationStatus == LoanApplicationStatus.LOAN_APPLICATION_REJECTED
			loanApplication.rejectionReason == 'Amount too high'
	}


}
