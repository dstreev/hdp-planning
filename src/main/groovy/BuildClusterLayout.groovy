import com.hdp.planning.cluster.layout.Env
import com.hdp.planning.cluster.layout.Host
import com.hdp.planning.cluster.layout.HostBuilder
import com.hdp.planning.cluster.layout.RackBuilder
import groovy.json.JsonSlurper
import groovyjarjarcommonscli.Option

import groovyx.net.http.*
import org.apache.commons.codec.binary.Base64

import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*

/**
 * Created by dstreev on 12/4/14.
 *
 */

def cli = new CliBuilder()
cli.c(longOpt: 'cluster', args: 1, required: false, 'Cluster Input File from Ambari REST API')
cli.url(longOpt: 'ambari-url', args: 1, required: false, 'url of the Ambari Server, should include port')
cli.cn(longOpt: 'cluster-name', args: 1, required: false, 'Cluster Name')
cli.user(longOpt: 'ambari-username', args: 1, required: false, 'Ambari Username')
cli.pw(longOpt: 'ambari-password', args: 1, required: false, 'Ambari Password')
cli.r(longOpt: 'rack-file', args: 1, required: false, 'Rack Topology File')
cli.rf(longOpt: 'rack-fields', args: Option.UNLIMITED_VALUES, valueSeparator: ',', required: false, 'Comma separated of positions for ip and rack in topology file')
cli.o(longOpt: 'output-directory', args: 1, required: true, 'Output Directory')

def options = cli.parse(this.args)

if (options == null)
    System.exit(-1);

def env = new Env();

def slurper = new JsonSlurper()
def cluster
// If "j" is specified, then the url, user and pw are not required.
if (options.c) {
    def json = new File(options.c)
    cluster = slurper.parse(json)

}

if (options.url) {

    // WIP Still unable to get the RESTClient to validate...  Arg...
    def apiURL =options.url + "/api/v1/clusters/" + options.cn + "/hosts?fields=Hosts/host_name,host_components,Hosts/ip,Hosts/total_mem,Hosts/os_arch,Hosts/os_type,Hosts/rack_info,Hosts/cpu_count,Hosts/disk_info,metrics/disk,Hosts/ph_cpu_count"
    def http = new RESTClient(apiURL)

//    def authString = "admin:admin"
//    if (options.user != null && options.pw != null) {
//        authString = options.user + ":" + options.pw
//    }

//    def String authStringEnc = Base64.encodeBase64(authString.getBytes());
//    authString.decodeBase64()

//    println authString

//    http.auth.basic(options.user, options.pw)
//    http.headers["Authorization"] = "Basic " + authString.decodeBase64()
//    print "$user : $pw"
//    http.auth.basic user, pw
    http.headers.'X-Requested-By' = 'ambari'
    http.headers.'X-Requested-With' = 'XMLHttpRequest'
    http.headers.'User-Agent' = 'Mozilla/5.0 Ubuntu/8.10 Firefox/3.0.4'

//    def authCred = (options.user + ":" + options.pw).bytes.encodeBase64();
//        auth.basic(user,pw)
    http.headers.'Authorization' = 'Basic YWRtaW46aG9ydG9ud29ya3M='.bytes
//    Basic YWRtaW46aG9ydG9ud29ya3M=
//    http.headers['Authorization'] = 'Basic '+authCred

    http.request(GET,JSON) {
        // Get the Cluster name
        uri.path = '/api/v1/clusters'
//        authConfig.basic(user,pw)

        response.success = { resp, json ->
            println resp.statusLine

            // parse the JSON response object:
            json.responseData.results.each {
                println "  ${it.titleNoFormatting} : ${it.visibleUrl}"
            }
        }
        // Get the results.
        //http://<ambari_host>:8080/api/v1/clusters/<cluster>/hosts?fields=Hosts/host_name,host_components,Hosts/ip,Hosts/total_mem,Hosts/os_arch,Hosts/os_type,Hosts/rack_info,Hosts/cpu_count,Hosts/disk_info,metrics/disk,Hosts/ph_cpu_count

        response.failure = { resp ->
            println "Unexpected error: ${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}"
        }
    }

    return 0;

}


def rackPositions = []

if (options.rf) {
    options.rfs.unique(false).each { field ->
        rackPositions.add(Integer.parseInt(field))
    }
} else {
    rackPositions.add(0) // ip
    rackPositions.add(1) // rack
}

def racks = [:]
def ips = [:]

if (options.r) {
    rackResult = RackBuilder.rackMap(new File(options.r), rackPositions)
    racks = rackResult[0]
    ips = rackResult[1]
}

println "Racks: " + racks
println "IPs: " + ips

def hosts = []
def rackHosts = [:]

def clusterName = cluster.items[0].Hosts.cluster_name

cluster.items.each { item ->
    Host host = HostBuilder.fromAmbariJson(item)
    hosts.add(host)
    // Set Rackname
    host.rackName = ips.get(host.ip)

    if (host.rackName == null) {
        rackHostList = rackHosts["NA"]
    } else {
        rackHostList = rackHosts[host.rackName]
    }
    if (rackHostList == null) {
        rackHostList = []
        if (host.rackName == null) {
            rackHosts.put("NA", rackHostList)
        } else {
            rackHosts.put(host.rackName, rackHostList)
        }
    }

    rackHostList.add(host)
}

targetDirectory = new File(options.o)
if (!targetDirectory.exists()) {
    if (!targetDirectory.mkdirs()) {
        println "Couldn't create target output directory: $options.o"
        return -1;
    }
}

full_graph = new File(options.o + System.getProperty("file.separator") + clusterName + ".dot")
full_graph.withWriter { w ->
    w.writeLine("digraph all {")
    w.writeLine("\trankdir=LR;")
    w.writeLine("\tranksep=0.1")
    w.writeLine("\tnodesep=0.1")

    rackHosts.each { rack, hostlist ->
        w.writeLine("subgraph cluster"+rack+" {")
        w.writeLine("\tnode[shape=box]")
        w.writeLine("\t"+rack+"[label=\"Rack " + rack + "\", shape=none]")

        // TODO Host Details.
        hostlist.each { host ->
            w.writeLine(HostBuilder.dotBasicComponents(host))
            w.write()
        }

        w.write("\t{ rank = same;" + rack + ";")
        hostlist.each { host ->
            if (host != hostlist.last()) {
                w.write(HostBuilder.SafeEntityName(host.name) + ";");
            } else {
                w.writeLine(HostBuilder.SafeEntityName(host.name) + ";}")
            }
        }
        w.writeLine("}")
    }

    w.write("{")
    rackHosts.keySet().each { rack ->
//        print("Rack: "+ rack)
        if ( rack != rackHosts.keySet().last()) {
            w.write(rack + " -> ")
        } else {
            w.write(rack + "[style=none,color=white]")
        }

    }

    w.writeLine("}")
    w.writeLine("label=\"Full Cluster - View\"")
    w.writeLine("}")

    w.flush()
    w.close()
}

if (env.graphExecutable != null) {
    def command = ["$env.graphExecutable", "-Tpng", "-o", options.o + System.getProperty("file.separator") + clusterName + ".png", options.o + System.getProperty("file.separator") + clusterName + ".dot"]
    println "Command: ${command}"

    def Process proc = command.execute()

    def out = new StringBuffer()
    def err = new StringBuffer()
    proc.consumeProcessOutput(out, err)

    def rtn = proc.waitFor()

    if (rtn != 0) {
        println "Command was not successful. Returned: " + rtn
        println "Error:"
        println err
    }
} else {
    println "Graph Executable hasn't been specified.  Run the 'dot' file through GraphViz to produce visual"
}
