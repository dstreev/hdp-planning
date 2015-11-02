# HDP Cluster Planning

## Build

    gradle fatJar
    
Or get the built version [here](https://github.com/dstreev/hdp-planning/releases)
    
## [Pre-Load Ambari Rack Info](./src/main/groovy/CfgHostRacksByTopology.groovy)
    
When you upgrade an HDP stack to Ambari 2.1+, the rack definitions from the rack topology file are not picked up.  The only way to get this information into Ambari is either thru the Web UI or the REST API.

This script is designed to build the REST API calls in a bash script, that are needed to translate the rack definitions in a topology file, into Ambari.

NOTE: This process is driven via "Host" and not from the IP (currently).  That's because the Host is looked up in Ambari, via the "name" and not by IP.

### Input

#### App Parameters

    usage: 
     -a,--host-alias <arg>              Identify if the Host Name in the
                                        Topology File is an Alias
     -d,--domain <arg>                  If the Host name in the Topology file
                                        is an Alias, specify the domain.
     -h,--ambari-host:port <arg>        Ambari Host:Port
     -hdr,--header                      Header in Topology File
     -hf,--topology-host-field <arg>    Topology Host Field (fqdn)
     -n,--cluster-name <arg>            Ambari Cluster Name
     -o,--output <arg>                  Output File
     -rf,--topology-rack-fields <arg>   Comma separated list of positions for
                                        rack designation
     -rp,--rack-prefix <arg>            Rack Prefix
     -t,--topology-file <arg>           Original Topology File

#### Example Topology File #1

    IP Address    Rack       Server Name (fqdn)
    
    10.0.0.160    rack1      m1.hdp.local
    10.0.0.161    rack1      m2.hdp.local
    10.0.0.162    rack1      m3.hdp.local
    10.0.0.165    rack2      d1.hdp.local
    10.0.0.166    rack2      d2.hdp.local
    10.0.0.167    rack2      d3.hdp.local

#### Example Topology File #2

    IP Address      Rack    Location        Server Name(alias)
    
    10.0.0.160    Home      1      m1
    10.0.0.161    Home      2      m2
    10.0.0.162    Home      3      m3
    10.0.0.165    Home      1      d1
    10.0.0.166    Home      1      d2
    10.0.0.167    Home      2      d3

#### Run
 
    # For example Topology File #1
    java -cp build/libs/hdp-planning-all.jar CfgHostRacksByTopology -t "/home/test/host_topology_test.data" -hf 2 -rf 1 -n Home -h m3.hdp.local:8080 -o /home/test/set_racks.sh -hdr

    # For example Topology File #2
    java -cp build/libs/hdp-planning-all.jar CfgHostRacksByTopology -t "/home/test/host_topology_test.data" -hf 3 -rf 1,2 -n Home -h m3.hdp.local:8080 -o /home/test/set_racks.sh -d hdp.local -hdr

## [Build Cluster Layout](./src/main/groovy/BuildClusterLayout.groovy)

Is a script that uses the json output from Ambari and a rack topology (optional), to build a Rack Diagram of the cluster using an Extract of the cluster from Ambari.

Use the following query against the Ambari REST API to build the needed cluster input file.

Sample Query issued to Ambari for output:

```
http://<ambari_host>:8080/api/v1/clusters/<cluster>/hosts?fields=Hosts/host_name,host_components,Hosts/ip,Hosts/total_mem,Hosts/os_arch,Hosts/os_type,Hosts/rack_info,Hosts/cpu_count,Hosts/disk_info,metrics/disk,Hosts/ph_cpu_count
```

### Input

#### VM Option
    -DgraphExecutable=<dot_exec>
    
#### App Parameters 
    usage: 
        -rf,--rack-fields <arg>       Comma separated of positions for ip
                                            and rack in topology file
        -c,--cluster <arg>            Cluster Input File from Ambari REST API
        -o,--output-directory <arg>   Output Directory
        -r,--rack-file <arg>          Rack Topology File
                                            
### Run

java -cp build/libs/hdp-planning-all.jar -DgraphExecutable=/usr/local/bin/dot BuildClusterLayout -json cluster.json -rack host_topology.data -fields 0,2 -output . 

## [Generate Template from Blueprint](./src/main/groovy/BuildBlueprintTemplate.groovy) 

Is a script that is designed to build a Blueprint Environment Template File from a Cluster Blueprint and a Cluster Configuration retrieved from Ambari.

Used to help with upgrades from 2.1 to 2.2+ of HDP.

    
### Input

#### App Parameters
    usage:
        -bp,--blueprint <arg>          Cluster Blueprint File
        -name,--blueprint-name <arg>   Target Blueprint Name
        -o,--output-file <arg>         Output File
        -c,--cluster <arg>             Source Cluster File (Ambari REST API
                                           Output of Cluster)

### Run

java -cp build/libs/hdp-planning-all.jar BuildBlueprintTemplate -bp cluster-bp.json -name dev-bp -src cluster.json -output ./dev-template.json