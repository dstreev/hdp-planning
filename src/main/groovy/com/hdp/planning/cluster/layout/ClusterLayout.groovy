package com.hdp.planning.cluster.layout

import groovy.json.JsonSlurper
import groovyjarjarcommonscli.Option

import groovyx.net.http.*
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*

/**
 * Created by dstreev on 12/4/14.
 *
 * Inputs:
 *  json - configuration file built through the Ambari REST API call:
 *  http://<ambari-host>:8080/api/v1/clusters/<clustername>/hosts?fields=Hosts/host_name,host_components,Hosts/ip,Hosts/total_mem,Hosts/os_arch,Hosts/rack_info,Hosts/cpu_count
 *  url - Ambari interface URL
 *  aname - Ambari Server username
 *  apass - Ambari Server password
 *
 *  Ambari doesn't current support com.hdp.planning.cluster.layout.Rack Awareness.  Supply com.hdp.planning.cluster.layout.Rack Information.
 *  rack - file with rack topology  default handling will consider the first element the ip address and the 2 as the rack.
 *  rack-fields - comma separated list of the field positions to use to extract the ip and rack info.  0 based.
 *
 *  TODO: Could we use the dfsadmin report as a source for rack information? Of course... worth the effort?
 *
 */

def cli = new CliBuilder()
cli.json(longOpt: 'json', args: 1, required: false, 'json Input File from Ambari REST API')
cli.url(longOpt: 'ambari-url', args: 1, required: false, 'url of the Ambari Server, should include port')
cli.user(longOpt: 'ambari-username', args: 1, required: false, 'Ambari Username')
cli.pw(longOpt: 'ambari-password', args: 1, required: false, 'Ambari Password')
cli.rack(longOpt: 'rack-file', args: 1, required: false, 'com.hdp.planning.cluster.layout.Rack Topology File')
cli.fields(longOpt: 'rack-fields', args: Option.UNLIMITED_VALUES, valueSeparator: ',', required: false, 'Comma separated of positions for ip and rack in topology file')
cli.output(longOpt: 'output-directory', args: 1, required: true, 'Output Directory')

def options = cli.parse(this.args)

def env = new Env();

def slurper = new JsonSlurper()
def cluster
// If "j" is specified, then the url, user and pw are not required.
if (options.json) {
    def json = new File(options.json)
    cluster = slurper.parse(json)

} else if (options.url) {

    // WIP Still unable to get the RESTClient to validate...  Arg...
    def http = new RESTClient(options.url.toString())

    def user = 'admin';
    def pw = 'admin';
    if (options.user & options.pw) {
        user = options.user
        pw = options.pw
    }
//    print "$user : $pw"
//    http.auth.basic user, pw
    http.headers.'X-Requested-By' = 'ambari'
    http.headers.'User-Agent' = 'Mozilla/5.0 Ubuntu/8.10 Firefox/3.0.4'

//    def authCred = ('admin:admin').bytes.encodeBase64();
//        auth.basic(user,pw)
    http.headers['Authorization'] = 'Basic YWRtaW46YWRtaW4='.bytes
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

} else {
    // Issue need one or the other.
    return -1
}

def rackPositions = []

if (options.fields) {
    options.fieldss.unique(false).each { field ->
        rackPositions.add(Integer.parseInt(field))
    }
} else {
    rackPositions.add(0) // ip
    rackPositions.add(1) // rack
}

def racks = [:]
def ips = [:]

if (options.rack) {
    rackResult = RackBuilder.rackMap(new File(options.rack), rackPositions)
    racks = rackResult[0]
    ips = rackResult[1]
}

println "Racks: " + racks
println "IPs: " + ips

def hosts = []
def rackHosts = [:]

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

targetDirectory = new File(options.output)
if (!targetDirectory.exists()) {
    if (!targetDirectory.mkdirs()) {
        println "Couldn't create target output directory: $options.output"
        return -1;
    }
}

full_graph = new File(options.output + "/cluster.dot")
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
        print("Rack: "+ rack)
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

// TODO: Get the cluster name from the JSON.
def command = "$env.graphExecutable -Tpng -o $options.output/cluster.png $options.output/cluster.dot"
println "Command: ${command}"

def proc = command.execute()
proc.waitFor()

