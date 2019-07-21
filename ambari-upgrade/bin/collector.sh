#!/bin/bash

# Collect all the values required to build an environment document.

# Artifacts
# 1. Ambari Cluster Layout.
#       http://<ambari_host>:8080/api/v1/clusters/<cluster>/hosts?fields=Hosts/host_name,host_components,Hosts/ip,Hosts/total_mem,Hosts/os_arch,Hosts/os_type,Hosts/rack_info,Hosts/cpu_count,Hosts/disk_info,metrics/disk,Hosts/ph_cpu_count
# 2. DFSAdmin Report with com.hdp.planning.cluster.layout.Rack Information.
# 3. Output from hdp-configuration-utils.

# Parameters Needed.
# 1. Ambari com.hdp.planning.cluster.layout.Host:Port
# 2. Cluster Name
# 3. Ambari Username / password
# 4. Cluster conf reference
#   (See hdp-mac-utils for a script that will install client configs and switch them to default for hdfs calls)
# 5. For hdp-configuration-utils


cd `dirname $0`

if [ -f $1-env.sh ]; then
    . ./$1-env.sh
else
    . default-env.sh
fi

# Look for hdp-mac-utils to switch environments.
if [ $# == 1 ]; then
    if [ -d ../../hdp-mac-utils ]; then
        echo "hdp-mac-utils found, switching environments"

    fi
fi
