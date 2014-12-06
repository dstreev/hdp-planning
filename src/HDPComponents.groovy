import org.omg.CORBA.UNKNOWN

/**
 * Created by dstreev on 12/5/14.
 */
class HDPComponents {

    static Map componentTranslationMap = [
            "JOURNALNODE":"Journal Node",
    "ZOOKEEPER_SERVER":"Zookeeper Server",
    "APP_TIMELINE_SERVER":"Application Timeline Server",
    "HISTORYSERVER":"History Server",
    "RESOURCEMANAGER":"Resource nManager",
    "NAMENODE":"Namenode",
    "ZKFC":"ZKFC",
    "HCAT":"HCAT",
    "HBASE_MASTER":"HBase Master",
    "OOZIE_SERVER":"Oozie",
    "HIVE_METASTORE":"Hive Metastore",
    "HIVE_SERVER":"Hive Server2",
    "WEBHCAT_SERVER":"WebHCat",
    "FALCON_SERVER":"Falcon",
    "DATANODE":"Datanode",
    "NODEMANAGER":"Node Manager",
    "HBASE_REGIONSERVER":"Region Server",
    "GANGLIA_MONITOR":"Ganglia Monitor",
    "GANGLIA_SERVER":"Ganglia",
    "NAGIOS_SERVER":"Nagios",
    "FALCON_CLIENT":"Falcon Client",
    "HBASE_CLIENT":"HBase Client",
    "HDFS_CLIENT":"HDFS Client",
    "HIVE_CLIENT":"Hive Client",
    "MAPREDUCE2_CLIENT":"MR2 Client",
    "OOZIE_CLIENT":"Oozie Client",
    "PIG":"PIG",
    "SQOOP":"SQOOP",
    "TEZ_CLIENT":"Tez Client",
    "YARN_CLIENT":"Yarn Client",
    "ZOOKEEPER_CLIENT":"ZooKeeper Client"
    ]

    static List CORE_MASTER_COMPONENTS = ["JOURNALNODE","ZOOKEEPER_SERVER","APP_TIMELINE_SERVER","HISTORYSERVER",
                                          "RESOURCEMANAGER","NAMENODE","ZKFC"]
    static List ECO_MASTER_COMPONENTS = ["HCAT","HBASE_MASTER","OOZIE_SERVER","HIVE_METASTORE","HIVE_SERVER",
                                         "WEBHCAT_SERVER","FALCON_SERVER"]
    static List HDP_COMPONENTS = ["DATANODE","NODEMANAGER","HBASE_REGIONSERVER"]
    static List MANAGEMENT_COMPONENTS = ["GANGLIA_MONITOR","GANGLIA_SERVER","NAGIOS_SERVER"]
    static List CLIENT_COMPONENTS = ["FALCON_CLIENT","HBASE_CLIENT","HDFS_CLIENT","HIVE_CLIENT","MAPREDUCE2_CLIENT",
                                     "OOZIE_CLIENT","PIG","SQOOP","TEZ_CLIENT","YARN_CLIENT","ZOOKEEPER_CLIENT"]

    static final String CORE_MASTER = "Core Master"
    static final String ECO_MASTER = "Eco Master"
    static final String COMPONENT = "Component"
    static final String MANAGEMENT = "Management"
    static final String CLIENT = "Client"
    static final String UNKNOWN = "UnKnown"
    static final String CORE_MASTER_DOT = "cmstrs"
    static final String ECO_MASTER_DOT = "emstrs"
    static final String COMPONENT_DOT = "comp"
    static final String MANAGEMENT_DOT = "mngt"
    static final String CLIENT_DOT = "clnts"
    static final String UNKNOWN_DOT = "UnKnown"

    static List CATEGORIES = [CORE_MASTER,ECO_MASTER,COMPONENT,MANAGEMENT,CLIENT,UNKNOWN]

    static boolean isMasterComponent(component) {
        if (CORE_MASTER_COMPONENTS.contains(component) | ECO_MASTER_COMPONENTS.contains(component)) {
            return true
        } else {
            return false
        }

    }

    static boolean isMasterHost(host) {
        boolean rtn = false
        host.components.each { component ->
            if (isMasterComponent(component))
                rtn = true
        }
        return rtn
    }

    static String[] getMasterComponents(host) {
        def masterComponents = []
        host.components.each { component ->
            if (CORE_MASTER_COMPONENTS.contains(component) | ECO_MASTER_COMPONENTS.contains(component))
                masterComponents.add(component)

        }
        return masterComponents
    }

    static String[] getNonMasterComponents(host) {
        def masterComponents = []
        host.components.each { component ->
            if (HDP_COMPONENTS.contains(component) | MANAGEMENT_COMPONENTS.contains(component) | CLIENT_COMPONENTS.contains(component))
                masterComponents.add(component)

        }
        return masterComponents
    }

    static String whichCategory(componentName) {
        if (CORE_MASTER_COMPONENTS.contains(componentName)) {
            return CORE_MASTER
        }
        if (ECO_MASTER_COMPONENTS.contains(componentName)) {
            return ECO_MASTER
        }
        if (HDP_COMPONENTS.contains(componentName)) {
            return COMPONENT
        }
        if (MANAGEMENT_COMPONENTS.contains(componentName)) {
            return MANAGEMENT
        }
        if (CLIENT_COMPONENTS.contains(componentName)) {
            return CLIENT
        }
        return UNKNOWN
    }

    static String whichCategoryDot(componentName) {
        if (CORE_MASTER_COMPONENTS.contains(componentName)) {
            return CORE_MASTER_DOT
        }
        if (ECO_MASTER_COMPONENTS.contains(componentName)) {
            return ECO_MASTER_DOT
        }
        if (HDP_COMPONENTS.contains(componentName)) {
            return COMPONENT_DOT
        }
        if (MANAGEMENT_COMPONENTS.contains(componentName)) {
            return MANAGEMENT_DOT
        }
        if (CLIENT_COMPONENTS.contains(componentName)) {
            return CLIENT_DOT
        }
        return UNKNOWN_DOT
    }

    static String friendlyComponentName(componentName) {
        componentTranslationMap[componentName]
    }

    static String toSimpleDot(component) {
        def dotRtn
        dotRtn = component + " [shape=box,style=filled,color=palegreen,fontsize=9,label=\""
        dotRtn = dotRtn + friendlyComponentName(component) + "\"];"
    }

}
