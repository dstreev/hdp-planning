import com.hdp.planning.cluster.layout.Env
import com.hdp.planning.cluster.layout.Host
import com.hdp.planning.cluster.layout.HostBuilder
import com.hdp.planning.cluster.layout.RackBuilder
import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovyjarjarcommonscli.Option

import groovyx.net.http.*
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*

/**
 * Created by dstreev on 11/2/2015.
 *
 * Use this to build an Ambari Template file that matches the Cluster and the Blueprint.
 * Helpful for recreating a cluster over the current implementation.
 *
 */

def cli = new CliBuilder()
cli.t(longOpt: 'topology-file', args: 1, required: true, 'Original Topology File')
cli.hf(longOpt: 'topology-host-field', args: 1, required: true, 'Topology Host Field (fqdn)')

cli.rf(longOpt: 'topology-rack-fields', args: Option.UNLIMITED_VALUES, valueSeparator: ',', required: false, 'Comma separated list of positions for rack designation')

cli.rp(longOpt: 'rack-prefix', args: 1, required: false, 'Rack Prefix')
cli.a(longOpt: 'host-alias', args: 1, required: false, 'Identify if the Host Name in the Topology File is an Alias')
cli.d(longOpt: 'domain', args: 1, required: false, 'If the Host name in the Topology file is an Alias, specify the domain.')

cli.n(longOpt: 'cluster-name', args: 1, required: true, 'Ambari Cluster Name')
cli.h(longOpt: 'ambari-host:port', args: 1, required: true, 'Ambari Host:Port')
cli.o(longOpt: 'output', args: 1, required: true, 'Output File')
cli.hdr(longOpt: 'header', args: 0, required: false, 'Header in Topology File')

def options = cli.parse(this.args)

if (options == null)
    System.exit(-1);

def rackScript = "#!/bin/bash\n"

rackScript <<= "AMBARI_HOST=" + options.h + "\n"
rackScript <<= "USER=\$1\n"
rackScript <<= "PASSWORD=\$2\n"

def rackPrefix
if (options.rp)
    rackPrefix = options.rp
else
    rackPrefix = ""


def topologyFile = new File(options.t)

def hostRack = [:]
topologyFile.withReader { reader ->
    def lineNum = 0
    while ((line = reader.readLine()) != null) {
        if (!line.startsWith("#") && line.trim().length() > 0) {
            try {
//                println(line);
                def header = false
                if (options.hdr)
                    header = true
                if ((header && lineNum != 0) || (!header)) {
                    def items = line.split()
                    def rack = rackPrefix
                    if (options.rf) {
                        def f = options.rfs
                        f.each { field ->
                            rack <<= "/" + items[Integer.parseInt(field)]
                        }
                    }
                    if (options.d) {
//                    println "Host: " + items[Integer.parseInt(options.hf)] + "." + options.d + " is in rack: " + rack
                        hostRack.put(items[Integer.parseInt(options.hf)] + "." + options.d, rack)
                    } else {
//                    println "Host: " + items[Integer.parseInt(options.hf)] + " is in rack: " + rack
                        hostRack.put(items[Integer.parseInt(options.hf)], rack)
                    }
                }
                lineNum += 1
            } catch (Exception e) {
                println e.message
            }
        }
    }
}

//println hostRack
def API_PATH = "http://\${AMBARI_HOST}/api/v1/clusters/Home/hosts"
def CURL_CMD = "curl -u \${USER}:\${PASSWORD} -i -H \"X-Requested-By:ambari\" -X PUT -d"

hostRack.each { host, rack ->
    def HOST_CMD = CURL_CMD
    def reqBody = "{\"RequestInfo\":{\"context\":\"Set Rack\",\"query\":\"Hosts/host_name.in(" + host + ")\"},\"Body\":{\"Hosts\":{\"rack_info\":\"" + rack + "\"}}}"


    HOST_CMD <<= " '" + reqBody + "' " + API_PATH

    rackScript <<= HOST_CMD + "\n"
}

def rackSetupF = new File(options.o)

rackSetupF.setExecutable(true, false)

rackSetupF.write(rackScript.toString())