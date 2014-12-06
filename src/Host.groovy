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

    private Boolean master

    Boolean isMaster() {
        if (master == null) {
            masterComponents = HDPComponents.getMasterComponents(this)
            nonMasterComponents = HDPComponents.getNonMasterComponents(this)
            if (masterComponents.size() > 0)
                master = true
            else
                master = false
        }
        return master
    }

    private List masterComponents = []
    private List nonMasterComponents = []

    List getMasterComponents() {
        if (master == null) {
            isMaster()
        }
        return masterComponents
    }

    List getNonMasterComponents() {
        if (master == null) {
            isMaster()
        }
        return nonMasterComponents
    }

}
