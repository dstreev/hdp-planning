package com.hdp.planning.cluster.layout

import groovy.json.JsonSlurper
import groovyjarjarcommonscli.Option

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
 *  TODO: Could we use the dfsadmin report as a source for rack information? Of, course... worth the effort?
 *
 */

def cli = new CliBuilder()
cli.j(longOpt: 'json', args: 1, required: false, 'json Input File from Ambari REST API')
cli.url(longOpt: 'ambari-url', args: 1, required: false, 'url of the Ambari Server, should include port')
cli.user(longOpt: 'ambari-username', args: 1, required: false, 'Ambari Username')
cli.pw(longOpt: 'ambari-password', args: 1, required: false, 'Ambari Password')
cli.rack(longOpt: 'rack-file', args: 1, required: false, 'com.hdp.planning.cluster.layout.Rack Topology File')
cli.fields(longOpt: 'rack-fields', args: Option.UNLIMITED_VALUES, valueSeparator: ',', required: false, 'Comma separated of positions for ip and rack in topology file')

def options = cli.parse(this.args)

def slurper = new JsonSlurper()
def cluster
// If "j" is specified, then the url, user and pw are not required.
if (options.j) {
    def json = new File(options.j)
    cluster = slurper.parse(json)

} else if (option.url) {

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

cluster.items.each { item ->
    println item.Hosts.host_name
    println item.Hosts.ip
    println item.Hosts.cpu_count
    println item.Hosts.total_mem

    item.host_components.each { component ->
        println "\t-" + component.HostRoles.component_name
    }

}

def hostBuilder = new HostBuilder();

all_racks = new File("/tmp/all_racks.dot")
all_racks.withWriter { ar ->
    ar.writeLine("digraph rack_view {")
    ar.writeLine("\trankdir=LR;")
    ar.writeLine("\tranksep=0.1;")
    ar.writeLine("\tlabel=\"com.hdp.planning.cluster.layout.Rack View\";")
    ar.writeLine("\tfontsize=14;")

    def rhierarchy
    racks.each { rack, iplist ->
        ar.writeLine("\t" + rack + " [shape=Mdiamond,fontcolor=grey,label=\"com.hdp.planning.cluster.layout.Rack\\n[" + rack + "]\",fontsize=10];")
        if (rhierarchy != null) {
                rhierarchy = rhierarchy + " -> " + rack
        } else {
          rhierarchy = "\t" +  rack
        }
    }
    ar.writeLine(rhierarchy + " [color=white];")

    cluster.items.each { hostItem ->
        host = HostBuilder.fromAmbariJson(hostItem)
        ar.writeLine(HostBuilder.dotExtended(host) + ";")
    }

    // TODO: rank = same for each rack.
    def sameranks = [:]

    racks.each { rack, hostlist ->
        sameranks.put(rack,[rack])
    }
    sameranks.put("default",[])

    cluster.items.each { hostItem ->
        host = HostBuilder.fromAmbariJson(hostItem)
        def rack = ips[host.ip]
        if ( rack != null) {
            rankHosts = sameranks[rack]
            if (rankHosts == null)
                rankHosts = sameranks["default"]
            rankHosts.add(HostBuilder.SafeEntityName(host.name))
        } else {
            rankHosts = sameranks["default"]
            rankHosts.add(HostBuilder.SafeEntityName(host.name))
        }
    }

    sameranks.each { rack, hostlist ->
        ar.writeLine("\t{ rank = same;")
        def hostsamerank
        hostlist.each { host ->
            if (hostsamerank != null) {
                hostsamerank = hostsamerank + ";" + host
            } else {
                hostsamerank = host
            }

        }
        ar.writeLine("\t\t" + hostsamerank + ";")
        ar.writeLine("\t};")
    }

    ar.writeLine("}")
    ar.flush()
    ar.close()

    def command = "/usr/local/bin/dot -Tpng -o /tmp/all_racks.png /tmp/all_racks.dot"
    def proc = command.execute()
    proc.waitFor()

    println "return code: ${ proc.exitValue()}"
    println "stderr: ${proc.err.text}"
    println "stdout: ${proc.in.text}"

}