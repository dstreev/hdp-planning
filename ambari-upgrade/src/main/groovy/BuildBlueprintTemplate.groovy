import com.hdp.planning.cluster.layout.AmbariRest
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

/**
 * Created by dstreev on 10/30/2015.
 *
 * Use this to build an Ambari Template file that matches the Cluster and the Blueprint.
 * Helpful for recreating a cluster over the current implementation.
 *
 */

def cli = new CliBuilder()
cli.c(longOpt: 'cluster', args: 1, required: true, 'Cluster File (Ambari REST API Output of Cluster)')

//cli.url(longOpt: 'ambari-url', args: 1, required: false, 'url of the Ambari Server, should include port (Currently not working)')
cli.cn(longOpt: 'cluster-name', args: 1, required: false, 'Cluster Name')
//cli.user(longOpt: 'ambari-username', args: 1, required: false, 'Ambari Username')
//cli.pw(longOpt: 'ambari-password', args: 1, required: false, 'Ambari Password')


cli.bp(longOpt: 'blueprint', args: 1, required: true, 'Cluster Blueprint File')
cli.name(longOpt: 'blueprint-name', args: 1, required: true, 'Target Blueprint Name')
cli.o(longOpt: 'output-directory', args: 1, required: true, 'Output Directory')

def options = cli.parse(this.args)

if (options == null)
    System.exit(-1);

def cluster
def slurper

if (options.c) {
    slurper = new JsonSlurper()
    def json = new File(options.c)
    cluster = slurper.parse(json)
} else if (options.url) {
    if (options.user == null || options.pw == null || options.c == null) {
        log.info 'When using Ambari the username, password and Clustername must be specified'
        return -1
    }

    def ambariRest = new AmbariRest(options.url, options.cn, options.user, options.pw)

    cluster = ambariRest.getClusterInfo()

} else {
    println "Either a cluster file (-c) or an Ambari URL (-u) is required"
}

def bp

def bpsrc = new File(options.bp)
bp = slurper.parse(bpsrc)

def obsoleteComponents = ["GANGLIA_MONITOR", "GANGLIA_SERVER", "AMBARI_SERVER", "NAGIOS", "STORM_REST_API"]
def hostgroups = [:]
def template = [:]

/*
    Build a List of Host Groups and the components they include.
 */
bp.host_groups.each { host_group ->
    def name = host_group.name;
    def components = []

    host_group.components.each { component ->
        if (!obsoleteComponents.contains(component.name))
            components.add(component.name)
    }

    hostgroups.put(name, components)
}

def hosts = [:]

/*
    Build Hosts and the components install on them.
 */
cluster.items.each { item ->
    def name = item.Hosts.host_name;
    def components = []
    item.host_components.each { component ->
        if (!obsoleteComponents.contains(component.HostRoles.component_name))
            components.add(component.HostRoles.component_name)
    }
    hosts.put(name, components)
}

/*
    Match Hosts to Host Groups
*/
hosts.each { host, components ->
    println host
    println components
    println "---------------"

    hostgroups.each { hostgroup, hgcomponents ->
        def commons = components.intersect(hgcomponents)
        def difference = components.plus(hgcomponents)
        difference.removeAll(commons)
        if (difference.size() == 0) {
            println "WINNER is " + hostgroup
            def templateHosts = []
            if (template.containsKey(hostgroup)) {
                templateHosts = template.get(hostgroup)
            } else {
                template.put(hostgroup, templateHosts)
            }
            templateHosts.add(host)
        }
        println hostgroup + ":" + difference
    }
    println "==============="
}


def builder = new JsonBuilder();
def configs = []
def hgs = []

builder blueprint: options.name,
        default_password: "admin",
        configurations: configs,
        host_groups: hgs

template.each { hostgroup, hostList ->
    def configsLcl = []
    def hostsLcl = []

    hostList.each { host ->
        hostsLcl.add fqdn: host
    }

    hgs.add name: hostgroup,
            configurations: configsLcl,
            hosts: hostsLcl
}

println builder.toPrettyString()

def clusterName = cluster.items[0].Hosts.cluster_name

targetDirectory = new File(options.o)
if (targetDirectory.exists() && targetDirectory.isFile()) {
    println "Output Directory: " + options.o + " is a file. Needs to be a directory."
    return -1;
}

if (!targetDirectory.exists()) {
    if (!targetDirectory.mkdirs()) {
        println "Couldn't create target output directory: $options.o"
        return -1;
    }
}

new File(options.o + System.getProperty("file.separator") + clusterName + "_template.json").write(builder.toPrettyString())