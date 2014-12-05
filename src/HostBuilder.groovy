/**
 * Created by dstreev on 12/4/14.
 */
class HostBuilder {

    static MASTERS = ["NAMENODE","SECONDARYNAMENODE","RESOURCEMANAGER","HISTORYSERVER","APP_TIMELINE_SERVER","HBASE_MASTER"]

    /*
    With the cluster object, build a dot entity for the host and master components.
     */
    String buildVersion1 (host) {
        def hostname = host.Hosts.host_name
        def hostip = host.Hosts.ip
        def components = []
        host.host_components.each { component ->
            if (MASTERS.contains(component.HostRoles.component_name))
                components.add(component.HostRoles.component_name)
        }
        def mcomp
        components.each { component ->
            if (mcomp == null) {
                mcomp = "\\n" + component;
            } else {
                mcomp = mcomp + "\\n" + component
            }
        }
        // m2 [shape=box,style=filled,color=seagreen,
        // label="m1.hdp.com\n10.0.23.10\n----------\nNamenode\nWebHCat\nHBase Mstr\nStorm Nimbus",fontcolor=white];

        def hostEntity = SafeEntityName(hostname) + " [shape=box,style=filled,color="
        if (mcomp != null)
            hostEntity = hostEntity + "seagreen"
        else
            hostEntity = hostEntity + "lightblue"

        hostEntity = hostEntity + ",label=\"" + hostname + "\\n" + hostip

        if (mcomp != null)
            hostEntity = hostEntity + mcomp

        hostEntity = hostEntity + "\",fontcolor="
        if (mcomp != null)
            hostEntity = hostEntity + "white"
        else
            hostEntity = hostEntity + "black"

        hostEntity = hostEntity + ",fontsize=9]"
    }

    static String SafeEntityName(value) {
        value.replaceAll("\\.","").replaceAll("-","")
    }
}

