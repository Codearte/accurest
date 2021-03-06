package io.codearte.accurest.stubrunner

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.codearte.accurest.dsl.GroovyDsl
import io.codearte.accurest.messaging.AccurestMessaging
import io.codearte.accurest.messaging.noop.NoOpAccurestMessaging

/**
 * Represents a single instance of ready-to-run stubs.
 * Can run the stubs and then will return the name of the collaborator together with
 * its URI.
 * Can also be queried if the current groupid and artifactid are matching the
 * corresponding running stub.
 */
@Slf4j
@CompileStatic
class StubRunner implements StubRunning {

	private final StubRepository stubRepository
	private final StubConfiguration stubsConfiguration
	private final StubRunnerOptions stubRunnerOptions
	private final StubRunnerExecutor localStubRunner
	private final AccurestMessaging accurestMessaging

	@Deprecated
	StubRunner(Arguments arguments) {
		this(arguments.stubRunnerOptions, arguments.repositoryPath, arguments.stub)
	}

	StubRunner(StubRunnerOptions stubRunnerOptions, String repositoryPath, StubConfiguration stubsConfiguration) {
		this(stubRunnerOptions, repositoryPath, stubsConfiguration, new NoOpAccurestMessaging())
	}

	StubRunner(StubRunnerOptions stubRunnerOptions, String repositoryPath, StubConfiguration stubsConfiguration,
	           AccurestMessaging accurestMessaging) {
		this.stubsConfiguration = stubsConfiguration
		this.stubRunnerOptions = stubRunnerOptions
		this.stubRepository = new StubRepository(new File(repositoryPath))
		AvailablePortScanner portScanner = new AvailablePortScanner(stubRunnerOptions.minPortValue,
				stubRunnerOptions.maxPortValue)
		this.accurestMessaging = accurestMessaging
		this.localStubRunner = new StubRunnerExecutor(portScanner, accurestMessaging)
	}

	@Override
	RunningStubs runStubs() {
		registerShutdownHook()
		return localStubRunner.runStubs(stubRunnerOptions,stubRepository, stubsConfiguration)
	}

	@Override
	URL findStubUrl(String groupId, String artifactId) {
		return localStubRunner.findStubUrl(groupId, artifactId)
	}

	@Override
	URL findStubUrl(String ivyNotation) {
		String[] splitString = ivyNotation.split(":")
		if (splitString.length == 1) {
			// assuming that ivy notation represents artifactId only
			return findStubUrl(null, splitString[0])
		}
		return findStubUrl(splitString[0], splitString[1])
	}

	@Override
	RunningStubs findAllRunningStubs() {
		return localStubRunner.findAllRunningStubs()
	}

	@Override
	Map<StubConfiguration, Collection<GroovyDsl>> getAccurestContracts() {
		return localStubRunner.getAccurestContracts()
	}

	@Override
	boolean trigger(String ivyNotation, String labelName) {
		return localStubRunner.trigger(ivyNotation, labelName)
	}

	@Override
	boolean trigger(String labelName) {
		return localStubRunner.trigger(labelName)
	}

	@Override
	boolean trigger() {
		return localStubRunner.trigger()
	}

	@Override
	Map<String, Collection<String>> labels() {
		return localStubRunner.labels()
	}

	private void registerShutdownHook() {
		Runnable stopAllServers = { this.close() }
		Runtime.runtime.addShutdownHook(new Thread(stopAllServers))
	}

	@Override
	void close() throws IOException {
		localStubRunner?.shutdown()
	}
}