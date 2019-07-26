package com.hdp.planning.cluster.layout
/**
 * Created by dstreev on 12/5/14.
 */
class HDPComponentsTS {

    static enum CLASSIFICATION_TYPE {
        MASTER, WORKER, AGENT, CLIENT, MANAGEMENT
    }

    static enum TEESHIRT_SIZE {
        XS, S, M, L, XL, XXL
    }

    static enum RESOURCE_TYPE {
        CPU, LOCAL_IO, NETWORK_IO, RAM
    }

//    static enum ECOSYSTEM_PART {
//        COMPUTE, STORAGE, HBASE, HIVE, INFRASTRUCTURE, SPARK, CONTROL, CUBE
//    }
    // Abbr, Desc, Map [
    //   XS]
    //
    static enum SYSTEM_PART {
        HDFS, YARN
    }

    class ResourceMap {
        TreeMap<TEESHIRT_SIZE, Float> map = new TreeMap<TEESHIRT_SIZE, Float>();
        ResourceMap(Float[] initVals) {
            int i = 0;
            for (TEESHIRT_SIZE ts: TEESHIRT_SIZE.values()) {
                map.put(ts, initVals[i++])
            }
        }
    }

    static enum COMPONENTS {
        JOURNALNODE(
                "JN","Journal Node",
                new ResourceMap((Float[]){1.0,2.0,3.0,4.0,5.0,6.0});
        )
        final SYSTEM_PART system_part;
        final String abbreviation;
        final String description;
        final ResourceMap resourceMap;
    }
    
    static enum MASTER_COMPONENT {
        JOURNALNODE("JN", "Journal Node"),
        ZOOKEEPER_SERVER("ZKSRV", "ZooKeeper Server"),
        HISTORYSERVER("HS", "History Server"),
        RESOURCEMANAGER("RM", "Resource Manager"),
        NAMENODE("NN", "Namenode"),
        APP_TIMELINE_SERVER("APP_TIMELINE_SERVER", "APP_TIMELINE_SERVER"),
        RANGER_ADMIN("RANGER_ADMIN", "RANGER_ADMIN"),
        RANGER_KMS_SERVER("RANGER_KMS_SERVER", "RANGER_KMS_SERVER"),
        RANGER_TAGSYNC("RANGER_TAGSYNC", "RANGER_TAGSYNC"),
        RANGER_USERSYNC("RANGER_USERSYNC", "RANGER_USERSYNC"),
        HIVE_METASTORE("HM", "Hive Metastore"),
        ATLAS_SERVER("ATLAS_SERVER", "ATLAS_SERVER"),
        HST_SERVER("HST", "Smart Sense HST Server"),
        INFRA_SOLR("INFRASOLR", "Infrastructure Solr"),
        METRICS_GRAFANA("GRAFANA", "Grafana"),
        SPARK2_JOBHISTORYSERVER("SHIST2", "Spark2 Job History Server"),
        ZKFC("ZKFC", "ZooKeeper Fence Controller")
        final String abbreviation;
        final String description;

        MASTER_COMPONENT(String abbr, String desc) {
            this.abbreviation = abbr;
            this.description = desc;
        }
    }

    static enum ECO_MASTER_COMPONENT {
        HBASE_MASTER("HBM", "HBase Master"),
        OOZIE_SERVER("OZ", "Oozie Server"),
        HIVE_SERVER("HS2", "Hive Server2"),
        KNOX_GATEWAY("KNOX", "Knox Gateway"),
        WEBHCAT_SERVER("WHCAT", "WebHCat"),
        FALCON_SERVER("FLCN", "Falcon Server"),
        KAFKA_BROKER("KFKB", "Kafka Broker"),
        NIMBUS("STMR_N", "Storm Nimbus Server"),
        DRPC_SERVER("STRM_DRPC", "Storm DPRC Server"),
        ACTIVITY_ANALYZER("ACTIVITY_ANALYZER", "ACTIVITY_ANALYZER"),
        ACTIVITY_EXPLORER("ACTIVITY_EXPLORER", "ACTIVITY_EXPLORER"),
        ZEPPELIN_MASTER("ZEPPELIN_MASTER", "ZEPPELIN_MASTER"),
        HIVE_SERVER_INTERACTIVE("HIVE_SERVER_INTERACTIVE", "HIVE_SERVER_INTERACTIVE"),
        LIVY2_SERVER("LIVY2_SERVER", "LIVY2_SERVER"),
        METRICS_COLLECTOR("METRICS_COLLECTOR", "METRICS_COLLECTOR"),
        PHOENIX_QUERY_SERVER("PHOENIX_QUERY_SERVER", "PHOENIX_QUERY_SERVER")
        , SPARK2_JOBHISTORYSERVER("SPARK2_JOBHISTORYSERVER", "SPARK2_JOBHISTORYSERVER")
        , SPARK2_THRIFTSERVER("SPARK2_THRIFTSERVER", "SPARK2_THRIFTSERVER")
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
        SUPERVISOR("STRM_SPRV", "Storm Supervisor"),
        PIG("PIG", "Pig"),
        SQOOP("SQP", "Sqoop"),
        TEZ_CLIENT("TC", "Tez Client"),
        SPARK_CLIENT("SPARKC", "Spark Client"),
        SPARK2_CLIENT("SPARK2C", "Spark2 Client"),
        YARN_CLIENT("YC", "Yarn Client"),
        ZOOKEEPER_CLIENT("ZC", "ZooKeeper Client"),
        HCAT("HCAT", "HCat"),
        KERBEROS_CLIENT("KERBEROS_CLIENT", "Kerberos Client"),
        ATLAS_CLIENT("ATLAS_CLIENT", "ATLAS_CLIENT"),
        INFRA_SOLR_CLIENT("INFRA_SOLR_CLIENT", "INFRA_SOLR_CLIENT"),
        SLIDER("SLIDER", "SLIDER"),
        final String abbreviation;
        final String description;

        CLIENT_COMPONENT(String abbr, String desc) {
            this.abbreviation = abbr;
            this.description = desc;
        }
    }

    static enum AGENT_COMPONENT {
        HST_AGENT("HST_AGENT", "HST Agent"),
        METRICS_MONITOR("METRICS_MONITOR", "Metrics Monitor")
        final String abbreviation;
        final String description;

        AGENT_COMPONENT(String abbr, String desc) {
            this.abbreviation = abbr;
            this.description = desc;
        }

    }

    static enum CORE_COMPONENT {
        DATANODE("DN", "Datanode"),
        NODEMANAGER("NM", "NodeManager"),
        HBASE_REGIONSERVER("HBR", "HBase Region Server")
        , KAFKA_BROKER("KAFKA_BROKER", "KAFKA_BROKER")
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
        , DP_PROFILER_AGENT("DPA", "DataPlane Profiler Agent")
        , METRICS_GRAFANA("METRICS_GRAFANA", "METRICS_GRAFANA")
        , HST_SERVER("HST_SERVER", "HST_SERVER")
        , INFRA_SOLR("INFRA_SOLR", "INFRA_SOLR")
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
            if (HDP_COMPONENTS.contains(component) | MANAGEMENT_COMPONENTS.contains(component) | CLIENT_COMPONENTS.contains(component) | AGENT_COMPONENTS.contains(component))
                masterComponents.add(component)

        }
        return masterComponents
    }

}
