# HDP Cluster Planning

## Build

    gradle fatJar

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