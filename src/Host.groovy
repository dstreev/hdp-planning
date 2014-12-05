import groovy.transform.ToString

/**
 * Created by dstreev on 12/5/14.
 */
@ToString(ignoreNulls = true, includeFields = true, includeNames = true)
class Host {
    static final String HOST_COLOR = "seagreen"
    static final String HOST_FONT_COLOR = "white"

    String ip
    String name
    String osType
    String osArch
    Integer cpuCount
    String totalMemory
    String rackName
    List components = []

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

    String toSimpleDot() {
        def dotRtn = HostBuilder.SafeEntityName(this.name)
        dotRtn = dotRtn + " [shape=box,style=filled,color="
        dotRtn = dotRtn + HOST_COLOR + ",label=\""
        dotRtn = dotRtn + this.name + "\\n" + this.ip + "\","
        dotRtn = dotRtn + "fontcolor=" + HOST_FONT_COLOR + "];"
    }
}
