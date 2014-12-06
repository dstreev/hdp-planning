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
 *  Ambari doesn't current support Rack Awareness.  Supply Rack Information.
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
cli.rack(longOpt: 'rack-file', args: 1, required: false, 'Rack Topology File')
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

def hosts = []
def rackHosts = [:]

cluster.items.each { item ->
    Host host = HostBuilder.fromAmbariJson(item)
    hosts.add(host)
    // Set Rackname
    host.rackName = ips.get(host.ip)

    rackHostList = rackHosts[host.rackName]
    if (rackHostList == null) {
        rackHostList = []
        rackHosts.put(host.rackName, rackHostList)
    }

    rackHostList.add(host)
}

rackHosts.each { rack,hostlist ->
    one_rack = new File("/tmp/" + rack + ".dot")
    def lclRackComponents = [:] // Categoy:ComponentList
    one_rack.withWriter { w ->
        w.writeLine("digraph one_rack {")
        w.writeLine("\trankdir=TB;")
        w.writeLine("\trankspe=0.5;")
        w.writeLine("\tnodesep=0.1;")
        w.writeLine("\tlabel=\"Individual View for Rack ["+rack+"]\";")

//        w.writeLine("\tclnts [shape=none,label=\"Clients\"];")
//        w.writeLine("\tcmstrs [shape=none,label=\"Core\\nMasters\"];")
//        w.writeLine("\tmhosts [shape=none,label=\"Master Hosts\"];")
//        w.writeLine("\thosts [shape=none,label=\"Hosts\"];")
//        w.writeLine("\temstrs [shape=none,label=\"Eco-System\\nMasters\"];")
//        w.writeLine("\tmngt [shape=none,label=\"Management\\nComponents\"];")
//        w.writeLine("\tcomp [shape=none,label=\"HDP Components\"];")

        // Rack Hierarchy
//        w.writeLine("\tclnts -> cmstrs -> comp -> mhosts -> hosts -> emstrs -> mngt;")
        w.writeLine("\t" + rack + " [shape=Mdiamond,fontcolor=grey,label=\"Rack\\n[" + rack + "]\",fontsize=20];")

        hostlist.each { host ->
            w.writeLine("\t" + HostBuilder.dotFull(host))
            host.components.each { hostComponent ->
                def category = HDPComponents.whichCategoryDot(hostComponent)
                def clist = lclRackComponents[category]
                if (clist == null) {
                    clist = []
                    lclRackComponents.put(category,clist)
                }
                if (!clist.contains(hostComponent)) {
                    clist.add(hostComponent)
                }
            }
//            w.writeLine("\t" + HostBuilder.SafeEntityName(host.name) + " -> " + rack + ";")
        }


        // Component Ranking
//        lclRackComponents.each { category, componentlist ->
//            // DOUBLES UP, MAYBE...
//            componentlist.each { component ->
//                w.writeLine("\t" + HDPComponents.toSimpleDot(component))
//            }
//
//        }

        w.writeLine("}")

    }


    println "Rack: " + rack
    println "\tHosts: "
    hostlist.each { host ->
        println "\t\t" + host.name
    }
}
//println hosts
println rackHosts

//def hostBuilder = new HostBuilder();
//
//all_racks = new File("/tmp/all_racks.dot")
//all_racks.withWriter { ar ->
//    ar.writeLine("digraph rack_view {")
//    ar.writeLine("\trankdir=TB;")
//    ar.writeLine("\tranksep=0.5")
//    ar.writeLine("\tlabel=\"Rack View\"")
//    ar.writeLine("\tfontsize=14")
//
//    def rhierarchy
//    racks.each { rack, iplist ->
//        ar.writeLine("\t" + rack + " [shape=Mdiamond,fontcolor=grey,label=\"Rack\\n[" + rack + "]\",fontsize=10];")
//        if (rhierarchy != null) {
////            if (rack == rack.last())
////                rhierarchy = rhierarchy + rack
////            else
//                rhierarchy = rhierarchy + " -> " + rack
//        } else {
//          rhierarchy = "\t" +  rack
//        }
//    }
//    ar.writeLine(rhierarchy)
//
//    cluster.items.each { hostItem ->
//        ar.writeLine(hostBuilder.buildVersion1(hostItem))
//    }
//    // TODO: rank = same for each rack.
//    def sameranks = [:]
//
//    racks.each { rack, hostlist ->
//        sameranks.put(rack,[rack])
//    }
//    sameranks.put("default",[])
//
//    cluster.items.each { hostItem ->
//        def hostip = hostItem.Hosts.ip
//        def rack = ips[hostip]
//        if ( rack != null) {
//            rankHosts = sameranks[rack]
//            if (rankHosts == null)
//                rankHosts = sameranks["default"]
//            rankHosts.add(HostBuilder.SafeEntityName(hostItem.Hosts.host_name))
//        } else {
//            rankHosts = sameranks["default"]
//            rankHosts.add(HostBuilder.SafeEntityName(hostItem.Hosts.host_name))
//        }
//        ar.writeLine(hostBuilder.buildVersion1(hostItem))
//    }
//
//    sameranks.each { rack, hostlist ->
//        ar.writeLine("\t{ rank = same;")
//        def hostsamerank
//        hostlist.each { host ->
//            if (hostsamerank != null) {
//                hostsamerank = hostsamerank + ";" + host
//            } else {
//                hostsamerank = host
//            }
//
//        }
//        ar.writeLine("\t\t" + hostsamerank)
//        ar.writeLine("\t}")
//    }
//
//    ar.writeLine("}")

//}