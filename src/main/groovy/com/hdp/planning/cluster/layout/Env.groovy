package com.hdp.planning.cluster.layout

/**
 * Created by dstreev on 3/12/15.
 */
class Env {
    static GRAPH_EXECUTABLE = 'graph.executable';

    def graphExecutable;

    Env() {
        Properties props = new Properties();
        URL url = this.class.getResource('/default-env.properties')
        if (url != null) {
            URI filePath = url.toURI()
            File file = new File(filePath)
            if (file.exists()) {
                file.withInputStream {
                    props.load(it)

                }
            }
        } else {
            println 'Default Env properties file not found.'
        }
        graphExecutable = props.getProperty(GRAPH_EXECUTABLE, '/usr/local/bin/dot')
    }

}
