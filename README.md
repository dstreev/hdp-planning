# HDP Cluster Planning

[ClusterLayout](./src/main/groovy/ClusterLayout.groovy) is a script that uses the json output from Ambari and a rack topology (optional), to build a Rack Diagram of the cluster.

Sample Query issued to Ambari for output:
``http://<ambari_host>:8080/api/v1/clusters/<cluster>/hosts?fields=Hosts/host_name,host_components,Hosts/ip,Hosts/total_mem,Hosts/os_arch,Hosts/os_type,Hosts/rack_info,Hosts/cpu_count,Hosts/disk_info,metrics/disk,Hosts/ph_cpu_count

