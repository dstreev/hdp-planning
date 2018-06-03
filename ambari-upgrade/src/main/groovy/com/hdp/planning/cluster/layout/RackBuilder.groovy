package com.hdp.planning.cluster.layout
/**
 * Created by dstreev on 12/5/14.
 */
class RackBuilder {

    static List rackMap(rackTopologyFile, rackPositions) {
        def racks = [:]
        def ips = [:]

        rackTopologyFile.eachLine { line ->
            // TODO: parameterize delimiter
            if (line.startsWith("#") || line.trim().length() == 0) {
                // Skip line

            } else {
                def parts = line.split();
                def lclip = parts[rackPositions[0]]
                def lclrack = parts[rackPositions[1]]
                def ipsList = racks.get(lclrack)
                if (ipsList == null) {
                    ipsList = []
                    ipsList.add(lclip)
                    racks.put(lclrack, ipsList)
                } else {
                    ipsList.add(lclip)
                }
                ips.put(lclip, lclrack)
            }
        }

        def rtn = [racks, ips]
    }

}
