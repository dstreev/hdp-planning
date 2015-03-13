package com.hdp.planning.cluster.layout
/**
 * Created by dstreev on 12/5/14.
 */
class HDPComponents {
    enum MASTER_COMPONENT {
        JOURNALNODE("JN", "Journal Node"),
        ZOOKEEPER_SERVER("ZKSRV", "ZooKeeper Server"),
        APP_TIMELINE_SERVER("ATS", "Application Timeline Server"),
        HISTORYSERVER("HS", "History Server"),
        RESOURCEMANAGER("RM", "Resource Manager"),
        NAMENODE("NN", "Namenode"),
        ZKFC("ZKFC", "ZooKeeper Fence Controller")
        final String abbreviation;
        final String description;
        MASTER_COMPONENT(String abbr, String desc) {
            this.abbreviation = abbr;
            this.description = desc;
        }
    }
    enum ECO_MASTER_COMPONENT {
        HBASE_MASTER("HBM","HBase Master"),
        OOZIE_SERVER("OZ", "Oozie Server"),
        HIVE_METASTORE("HM", "Hive Metastore"),
        HIVE_SERVER("HS2", "Hive Server2"),
        WEBHCAT_SERVER("WHCAT", "WebHCat"),
        FALCON_SERVER("FLCN", "Falcon Server")
        final String abbreviation;
        final String description;
        ECO_MASTER_COMPONENT(String abbr, String desc) {
            this.abbreviation = abbr;
            this.description = desc;
        }
    }
    enum CLIENT_COMPONENT {
        FALCON_CLIENT("FC", "Falcon Client"),
        HBASE_CLIENT("HBC", "HBase Client"),
        HDFS_CLIENT("HC", "HDFS Client"),
        HIVE_CLIENT("HCLI\\n", "Hive CLI"),
        MAPREDUCE2_CLIENT("MRC", "MapReduce Client"),
        OOZIE_CLIENT("OZC", "Oozie Client"),
        PIG("PIG", "Pig"),
        SQOOP("SQP", "Sqoop"),
        TEZ_CLIENT("TC", "Tez Client"),
        YARN_CLIENT("YC", "Yarn Client"),
        ZOOKEEPER_CLIENT("ZC", "ZooKeeper Client"),
        HCAT("HCAT", "HCat")
        final String abbreviation;
        final String description;
        CLIENT_COMPONENT(String abbr, String desc) {
            this.abbreviation = abbr;
            this.description = desc;
        }
    }
    enum CORE_COMPONENT {
        DATANODE("DN", "Datanode"),
        NODEMANAGER("NM", "NodeManager"),
        HBASE_REGIONSERVER("HBR", "HBase Region Server")
        final String abbreviation;
        final String description;
        CORE_COMPONENT(String abbr, String desc) {
            this.abbreviation = abbr;
            this.description = desc;
        }
    }
    enum MANAGEMENT_COMPONENT {
        GANGLIA_MONITOR("GM", "Ganglia Monitor"),
        GANGLIA_SERVER("GS", "Ganglia Server"),
        NAGIOS_SERVER("NS", "Nagios Server")
        final String abbreviation;
        final String description;
        MANAGEMENT_COMPONENT(String abbr, String desc) {
            this.abbreviation = abbr;
            this.description = desc;
        }
    }


    static Map componentTranslationMap = [
            "JOURNALNODE":["Journal Node","JN"],
    "ZOOKEEPER_SERVER":["Zookeeper Server","ZKSRV"],
    "APP_TIMELINE_SERVER":["Application Timeline Server","ATS"],
    "HISTORYSERVER":["History Server","HS"],
    "RESOURCEMANAGER":["Resource Manager","RM"],
    "NAMENODE":["Namenode","NN"],
    "ZKFC":["ZKFC","ZKFC"],
    "HCAT":["HCAT","HC"],
    "HBASE_MASTER":["HBase Master","HBM"],
    "OOZIE_SERVER":["Oozie","OZ"],
    "HIVE_METASTORE":["Hive Metastore","HM"],
    "HIVE_SERVER":["Hive Server2","HS2"],
    "WEBHCAT_SERVER":["WebHCat","WHCAT"],
    "FALCON_SERVER":["Falcon","FLCN"],
    "DATANODE":["Datanode","DN"],
    "NODEMANAGER":["Node Manager","NM"],
    "HBASE_REGIONSERVER":["Region Server","RS"],
    "GANGLIA_MONITOR":["Ganglia Monitor","GM"],
    "GANGLIA_SERVER":["Ganglia","GS"],
    "NAGIOS_SERVER":["Nagios","NS"],
    "FALCON_CLIENT":["Falcon Client","FC"],
    "HBASE_CLIENT":["HBase Client","HBC"],
    "HDFS_CLIENT":["HDFS Client","HC"],
    "HIVE_CLIENT":["Hive Client","HCLI"],
    "MAPREDUCE2_CLIENT":["MR2 Client","MC"],
    "OOZIE_CLIENT":["Oozie Client","OC"],
    "PIG":["PIG","PIG"],
    "SQOOP":["SQOOP","SQP"],
    "TEZ_CLIENT":["Tez Client","TC"],
    "YARN_CLIENT":["Yarn Client","YC"],
    "ZOOKEEPER_CLIENT":["ZooKeeper Client","ZC"]
    ]

    static List CORE_MASTER_COMPONENTS = ["JOURNALNODE","ZOOKEEPER_SERVER","APP_TIMELINE_SERVER","HISTORYSERVER",
                                          "RESOURCEMANAGER","NAMENODE","ZKFC"]
    static List ECO_MASTER_COMPONENTS = ["HBASE_MASTER","OOZIE_SERVER","HIVE_METASTORE","HIVE_SERVER",
                                         "WEBHCAT_SERVER","FALCON_SERVER"]
    static List HDP_COMPONENTS = ["DATANODE","NODEMANAGER","HBASE_REGIONSERVER"]
    static List MANAGEMENT_COMPONENTS = ["GANGLIA_MONITOR","GANGLIA_SERVER","NAGIOS_SERVER"]
    static List CLIENT_COMPONENTS = ["FALCON_CLIENT","HBASE_CLIENT","HDFS_CLIENT","HIVE_CLIENT","MAPREDUCE2_CLIENT",
                                     "OOZIE_CLIENT","PIG","SQOOP","TEZ_CLIENT","YARN_CLIENT","ZOOKEEPER_CLIENT","HCAT"]

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

    static String[] getComponents(type, host) {
        def components = []
        host.components.each { component ->
            try {
                def componentEnum = type.valueOf(component)
                components.add(componentEnum.abbreviation)
            } catch (IllegalArgumentException iae) {
                // Nothing
            }
        }
        return components;
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
        componentTranslationMap[componentName][1]
    }

    static String toSimpleDot(component) {
        def dotRtn
        dotRtn = component + " [shape=box,style=filled,color=palegreen,fontsize=9,label=\""
        dotRtn = dotRtn + friendlyComponentName(component) + "\"];"
    }

}
