import groovy.json.JsonSlurper
import groovyjarjarcommonscli.Option

/**
 * Created by dstreev on 3/23/15.
 *
 */

class Constants {
    public static final String YARN_CP_PREFIX = "yarn.scheduler.capacity."
    public static final String YARN_CAPACITY = "capacity"
    public static final String YARN_MAX_CAPACITY = "maximum-capacity";
    public static final String YARN_QUEUE_MAPPINGS = "queue-mappings";
    public static final String USERS = "users";
    public static final String GROUPS = "groups";
    public static final String USER_LIMIT_FACTOR = "user-limit-factor";
}

def cli = new CliBuilder()
cli.input(longOpt: 'input', args: 1, required: true, 'Input Source File (txt,xml or json)')
cli.output(longOpt: 'output', args: 1, required: true, 'Output Filename. Text file that can be loaded through the Ambari Scheduler Interface.')

def options = cli.parse(this.args)

def slurper = new JsonSlurper();

def File jsonFile = new File(options.input);

def queueMappings = [];

def json = slurper.parse(jsonFile)

assert json.name == 'root'

class QueueResolver {
    def processQueue(parent, queue, writer, queueMappings) {
        def parentPath;
        if (parent != null) {
            parentPath = parent + '.' + queue.name;
        } else {
            parentPath = queue.name;
        }
        println parentPath;
        // Track that the sub queues add upto 100 percent.
        def capSum = 0;
        def queues = [];
        writer.writeLine(Constants.YARN_CP_PREFIX + parentPath + "."+ Constants.YARN_CAPACITY+ "=" + queue.capacity)
        if ( queue[Constants.YARN_MAX_CAPACITY] != null)
            writer.writeLine(Constants.YARN_CP_PREFIX + parentPath + "."+ Constants.YARN_MAX_CAPACITY+"=" + queue[Constants.YARN_MAX_CAPACITY])
        if ( queue[Constants.USER_LIMIT_FACTOR] != null)
            writer.writeLine(Constants.YARN_CP_PREFIX + parentPath + "."+ Constants.USER_LIMIT_FACTOR+"=" + queue[Constants.USER_LIMIT_FACTOR])
        queue.queues.each { subQueue ->
            queues.add(subQueue.name);
            processQueue(parentPath, subQueue, writer, queueMappings);
            if (subQueue.capacity != null) {
                capSum += subQueue.capacity
            }
        }
        if (queue[Constants.YARN_QUEUE_MAPPINGS]) {
            queue[Constants.YARN_QUEUE_MAPPINGS][Constants.USERS].each { user ->
                queueMappings.add("u:"+user+":"+queue.name);
            }
            queue[Constants.YARN_QUEUE_MAPPINGS][Constants.GROUPS].each { group ->
                queueMappings.add("g:"+group+":"+queue.name);
            }
//            ) {
//                def users = queue[Constants.YARN_QUEUE_MAPPINGS][Constants.USERS].join(",")
//                if (users != null && users.trim().length() > 0){
//                    queueMappings.add("u:"+users+":"+queue.name);
////                    writer.writeLine(Constants.YARN_CP_PREFIX + parentPath + "." + Constants.YARN_QUEUE_MAPPINGS + "=")
//                }
//                println parentPath + " Users: " + users;
//            }
        }
        if (queues.size() > 0)
            writer.writeLine(Constants.YARN_CP_PREFIX + parentPath + ".queues=" + queues.join(","));

        if (capSum != 0)
            assert capSum == 100, "The sub-queues for '" + parentPath + "' are defined with only " + capSum + " capacity.";
    }
}

def queueResolver = new QueueResolver();

capacity_text = new File(options.output)
capacity_text.withWriter { w ->

    w.writeLine(Constants.YARN_CP_PREFIX + "default.minimum-user-limit-percent=100")
    w.writeLine(Constants.YARN_CP_PREFIX + "maximum-am-resource-percent=0.2")
    w.writeLine(Constants.YARN_CP_PREFIX + "maximum-applications=10000")
    w.writeLine(Constants.YARN_CP_PREFIX + "node-locality-delay=40")
    w.writeLine(Constants.YARN_CP_PREFIX + "resource-calculator=org.apache.hadoop.yarn.util.resource.DefaultResourceCalculator")
    w.writeLine(Constants.YARN_CP_PREFIX + "root.accessible-node-labels=*")
    w.writeLine(Constants.YARN_CP_PREFIX + "root.accessible-node-labels.default.capacity=-1")
    w.writeLine(Constants.YARN_CP_PREFIX + "root.accessible-node-labels.default.maximum-capacity=-1")
    w.writeLine(Constants.YARN_CP_PREFIX + "root.acl_administer_queue=*")
//    w.writeLine(YARN_CP_PREFIX + "root.capacity=100")
//    def path = Constants.YARN_CP_PREFIX + json.name;

//    w.writeLine(path + "." + Constants.YARN_CAPACITY + "=" + json.capacity)

    queueResolver.processQueue(null, json, w, queueMappings);
    w.writeLine(Constants.YARN_CP_PREFIX + Constants.YARN_QUEUE_MAPPINGS + "=" + queueMappings.join(","))

//    json.queues.each { queue ->
//        queueResolver.processQueue('root', queue, w);
//    }

    w.flush()
    w.close()
}