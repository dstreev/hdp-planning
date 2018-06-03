package com.hdp.planning.cluster.layout

import groovy.transform.ToString

/**
 * Created by dstreev on 12/5/14.
 */
@ToString(ignoreNulls = true, includeFields = true, includeNames = true)
class Host {

    String ip
    String name
    String osType
    String osArch
    Integer cpuCount
    String totalMemory
    String rackName
    List components = []

}
