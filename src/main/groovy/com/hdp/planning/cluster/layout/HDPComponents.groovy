package com.hdp.planning.cluster.layout
/**
 * Created by dstreev on 12/5/14.
 */
class HDPComponents {
    static enum MASTER_COMPONENT {
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
    static enum ECO_MASTER_COMPONENT {
        HBASE_MASTER("HBM","HBase Master"),
        OOZIE_SERVER("OZ", "Oozie Server"),
        HIVE_METASTORE("HM", "Hive Metastore"),
        HIVE_SERVER("HS2", "Hive Server2"),
        WEBHCAT_SERVER("WHCAT", "WebHCat"),
        FALCON_SERVER("FLCN", "Falcon Server"),
        KAFKA_BROKER("KFKB", "Kafka Broker"),
        SUPERVISOR("STRM_SPRV", "Storm Supervisor"),
        NIMBUS("STMR_N", "Storm Nimbus Server"),
        DRPC_SERVER("STRM_DRPC", "Storm DPRC Server")
        final String abbreviation;
        final String description;
        ECO_MASTER_COMPONENT(String abbr, String desc) {
            this.abbreviation = abbr;
            this.description = desc;
        }
    }
    static enum CLIENT_COMPONENT {
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

    static enum CORE_COMPONENT {
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

    static enum MANAGEMENT_COMPONENT {
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

    static boolean isMasterComponent(component) {

        try {
            MASTER_COMPONENT.valueOf(component);
            return true

        } catch (IllegalArgumentException iae) {
            try {
                ECO_MASTER_COMPONENT.valueOf(component)
                return true
            } catch (IllegalArgumentException iae2) {
                return false;
            }
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

}
