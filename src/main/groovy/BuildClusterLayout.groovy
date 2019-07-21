import com.hdp.planning.cluster.layout.AmbariRest
import com.hdp.planning.cluster.layout.Env
import com.hdp.planning.cluster.layout.Host
import com.hdp.planning.cluster.layout.HostBuilder
import com.hdp.planning.cluster.layout.RackBuilder
import groovy.json.JsonSlurper
import groovyjarjarcommonscli.Option

import groovyx.net.http.*

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

def cluster

// If "j" is specified, then the url, user and pw are not required.
if (options.c) {
    def slurper = new JsonSlurper()
    def json = new File(options.c)
    cluster = slurper.parse(json)
} else if (options.url) {
    if (options.user == null || options.pw == null || options.c == null) {
        println 'When using Ambari the username, password and Clustername must be specified'
        return -1
    }

    def ambariRest = new AmbariRest(options.url, options.cn, options.user, options.pw)

    cluster = ambariRest.getClusterInfo()

} else {
    println "Either a cluster file (-c) or an Ambari URL (-u) is required"
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

    // If a Rack File was supplied, use it.  Otherwise, use the rack
    // data in Ambari.
    if (options.f) {
        // Set Rackname
        host.rackName = ips.get(host.ip)
    }

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
        w.writeLine("subgraph cluster" + HostBuilder.SafeEntityName(rack) + " {")
        w.writeLine("\tnode[shape=box]")
        w.writeLine("\t" + HostBuilder.SafeEntityName(rack) + "[label=\"Rack " + rack + "\", shape=none]")

        // TODO Host Details.
        hostlist.each { host ->
            w.writeLine(HostBuilder.dotBasicComponents(host))
            w.write()
        }

        w.write("\t{ rank = same;" + HostBuilder.SafeEntityName(rack) + ";")
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
        if (rack != rackHosts.keySet().last()) {
            w.write(HostBuilder.SafeEntityName(rack) + " -> ")
        } else {
            w.write(HostBuilder.SafeEntityName(rack) + "[style=none,color=white]")
        }

    }

    w.writeLine("}")
    w.writeLine("label=\"Full Cluster - View\"")
    w.writeLine("}")

    w.flush()
    w.close()
}

if (env.graphExecutable != null) {
    def command = ["$env.graphExecutable", "-Tsvg", "-o", options.o + System.getProperty("file.separator") + clusterName + ".svg", options.o + System.getProperty("file.separator") + clusterName + ".dot"]
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
