/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package software.betamax.compatibility

import software.betamax.junit.*
import software.betamax.util.server.*
import com.google.common.io.Files
import org.apache.commons.httpclient.*
import org.apache.commons.httpclient.methods.GetMethod
import org.junit.ClassRule
import software.betamax.ProxyConfiguration
import software.betamax.junit.Betamax
import software.betamax.junit.RecorderRule
import software.betamax.util.server.HelloHandler
import software.betamax.util.server.SimpleServer
import spock.lang.*
import static software.betamax.Headers.X_BETAMAX
import static software.betamax.TapeMode.READ_WRITE
import static java.net.HttpURLConnection.HTTP_OK
import static com.google.common.net.HttpHeaders.VIA

@Betamax(mode = READ_WRITE)
@Timeout(10)
class HttpClient3Spec extends Specification {

    @Shared @AutoCleanup("deleteDir") def tapeRoot = Files.createTempDir()
    @Shared def configuration = ProxyConfiguration.builder().tapeRoot(tapeRoot).build()
    @Shared @ClassRule RecorderRule recorder = new RecorderRule(configuration)

    @Shared @AutoCleanup("stop") def endpoint = new SimpleServer(HelloHandler)

    void setupSpec() {
        endpoint.start()
    }

    void "proxy intercepts HTTPClient 3.x connections"() {
        given:
        def client = new HttpClient()
        client.hostConfiguration.proxyHost = new ProxyHost(configuration.proxyHost.hostName, configuration.proxyPort)

        and:
        def request = new GetMethod(endpoint.url)

        when:
        def status = client.executeMethod(request)

        then:
        status == HTTP_OK
        request.getResponseHeader(VIA)?.value == "Betamax"
        request.getResponseHeader(X_BETAMAX)?.value == "REC"
    }

}
