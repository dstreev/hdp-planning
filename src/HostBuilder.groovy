/**
 * Created by dstreev on 12/4/14.
 */
class HostBuilder {

    static final String HOST_MASTER_COLOR = "darkgreen"
    static final String HOST_MASTER_FONT_COLOR = "white"
    static final String HOST_MASTER_FONT_SIZE = 10

    static final String HOST_COLOR = "lightgray"
    static final String HOST_FONT_COLOR = "black"
    static final String HOST_FONT_SIZE = 10
    /*
    With the cluster object, build a dot entity for the host and master components.
     */

    String dotSimple() {
        def dotRtn = HostBuilder.SafeEntityName(this.name)
        dotRtn = dotRtn + " [shape=box,style=filled,color="
        dotRtn = dotRtn + HOST_COLOR + ",label=\""
        dotRtn = dotRtn + this.name + "\\n" + this.ip + "\","
        dotRtn = dotRtn + "fontcolor=" + HOST_FONT_COLOR + "];"
    }

    static String dotBasic (host) {
        def hostname = host.name
        def hostip = host.ip

        def hostEntity = SafeEntityName(hostname) + " [shape=record,style=filled,color="
        if (host.isMaster())
            hostEntity = hostEntity + HOST_MASTER_COLOR
        else
            hostEntity = hostEntity + HOST_COLOR

        hostEntity = hostEntity + ",label=\"" + hostname + "\\n" + hostip + " | " + host.osType + " * " + host.cpuCount + " * " + host.totalMemory + ""

        hostEntity = hostEntity + "\",fontcolor="
        if (host.isMaster())
            hostEntity = hostEntity + HOST_MASTER_FONT_COLOR
        else
            hostEntity = hostEntity + HOST_FONT_COLOR

        hostEntity = hostEntity + ",fontsize="
        if (host.isMaster())
            hostEntity = hostEntity + HOST_MASTER_FONT_SIZE
        else
            hostEntity = hostEntity + HOST_FONT_SIZE

        hostEntity = hostEntity + "]"
    }

    static String dotExtended (host) {
        def hostname = host.name
        def hostip = host.ip

        def hostEntity = SafeEntityName(hostname) + " [shape=record,style=filled,color="
        if (host.isMaster())
            hostEntity = hostEntity + HOST_MASTER_COLOR
        else
            hostEntity = hostEntity + HOST_COLOR

//        hostEntity = hostEntity + ",label=\"" + hostname + "\\n" + hostip
        hostEntity = hostEntity + ",label=\"" + hostname + "\\n" + hostip + " | " + host.osType + "\\n" + host.cpuCount + " cpus " + host.totalMemory + " mem "

        if (host.isMaster()) {
            hostEntity = hostEntity + " | {"
            host.getMasterComponents().each { component ->
                hostEntity = hostEntity + HDPComponents.friendlyComponentName(component) + "\\n"
            }
            hostEntity = hostEntity + "}"
        }

        hostEntity = hostEntity + "\",fontcolor="
        if (host.isMaster())
            hostEntity = hostEntity + HOST_MASTER_FONT_COLOR
        else
            hostEntity = hostEntity + HOST_FONT_COLOR

        hostEntity = hostEntity + ",fontsize="
        if (host.isMaster())
            hostEntity = hostEntity + HOST_MASTER_FONT_SIZE
        else
            hostEntity = hostEntity + HOST_FONT_SIZE

        hostEntity = hostEntity + "]"
    }

    static String dotFull (host) {
        def hostname = host.name
        def hostip = host.ip

        def hostEntity = SafeEntityName(hostname) + " [shape=record,style=filled,color="
        if (host.isMaster())
            hostEntity = hostEntity + HOST_MASTER_COLOR
        else
            hostEntity = hostEntity + HOST_COLOR

        hostEntity = hostEntity + ",label=\" {"  + hostname + "\\n" + hostip + " | " + host.osType + "\\n" + host.cpuCount + " cpus " + host.totalMemory + " mem} | "

        if (host.isMaster()) {
            hostEntity = hostEntity + " | { "
            host.getMasterComponents().each { component ->
                hostEntity = hostEntity + HDPComponents.friendlyComponentName(component) + "\\n"
            }
            hostEntity = hostEntity + "  "
        }

        hostEntity = hostEntity + "| "
        host.getNonMasterComponents().each { component ->
            hostEntity = hostEntity + HDPComponents.friendlyComponentName(component) + "\\n"
        }
        hostEntity = hostEntity + " }}"

        hostEntity = hostEntity + "\",fontcolor="
        if (host.isMaster())
            hostEntity = hostEntity + HOST_MASTER_FONT_COLOR
        else
            hostEntity = hostEntity + HOST_FONT_COLOR

        hostEntity = hostEntity + ",fontsize="
        if (host.isMaster())
            hostEntity = hostEntity + HOST_MASTER_FONT_SIZE
        else
            hostEntity = hostEntity + HOST_FONT_SIZE

        hostEntity = hostEntity + "]"
    }

    static String SafeEntityName(value) {
        value.replaceAll("\\.","").replaceAll("-","")
    }

    static Host fromAmbariJson(json) {
        Host host = new Host()

        host.ip = json.Hosts.ip
        host.name = json.Hosts.host_name
        host.osType = json.Hosts.os_type
        host.osArch = json.Hosts.os_arch
        host.cpuCount = json.Hosts.cpu_count
        host.totalMemory = json.Hosts.total_mem

        // Not currently supported in Ambari 1.7 and below
        // Need to get from rack topology or dfsadmin report
        // host.rackName = json.Hosts.rack_info

        json.host_components.each { component ->
            host.components.add(component.HostRoles.component_name)
        }

        return host
    }


}

