package com.hdp.planning.oozie

import groovy.io.FileType
import groovyjarjarcommonscli.Option

/**
 * Created by dstreev on 2/26/15.
 *
 * The script is intended to create DAG graphs of Oozie Workflows in a directory tree.
 *
 *
 * Input Parameters:
 *  - Base Directory
 */

def cli = new CliBuilder()
cli.input(longOpt: 'input', args: 1, required: true, 'Base Input Directory')
cli.output(longOpt: 'output', args: 1, required: true, 'Output Directory')

def options = cli.parse(this.args)

def File baseDirectory;
if (options.input) {
    baseDirectory = new File(options.input);
    if (!(baseDirectory.exists() && baseDirectory.isDirectory())) {
        println("Provided input should be an existing Directory");
        return -1;
    }
}

def targetDirectory = new File(options.output);

if (targetDirectory.exists()) {
    if (!targetDirectory.isDirectory()) {
        println("Target Directory: " + targetDirectory.absolutePath + " is a file.")
        return;
    }
} else {
    println("Target Directory ("+ targetDirectory.absolutePath +") doesn't exist, creating.");
    targetDirectory.mkdirs();
}

def fileSeparator = System.properties.get('file.separator');

def baseDirectoryAbsolute = baseDirectory.absolutePath;

println("Abs Dir " + baseDirectoryAbsolute);

def buildDag(workflow, targetDirectory) {
    def wrkflw = new XmlSlurper().parse(workflow);
    def outputname;
    if (workflow.parentFile.name.equalsIgnoreCase("subworkflow")) {
        outputname = workflow.parentFile.parentFile.name + "_sub";
    } else {
        outputname = workflow.parentFile.name;
    }

    println(targetDirectory.name + System.properties.get('file.separator') + outputname);

    // Workflow Name
    println(wrkflw.@name);

    def outputFile = new File(targetDirectory.absolutePath + System.properties.get('file.separator') + outputname + ".dot");

    outputFile.withWriter { cout ->
        cout.writeLine("digraph "+ outputname +" {")

        // These should be either 'action' or 'decision' nodes.
        for (item in wrkflw.childNodes()) {
            if (item.name == 'action') {
                println("Action: " + item.name);
                cout.writeLine("")
            } else if (item.name == 'start') {
                println("Start: " + item.name);
                cout.writeLine("start;");
                cout.writeLine("start -> " + item.@to + ";" )
            } else if (item.name == 'decision') {
                println("Decision: " + item.name);
            } else if (item.name == 'kill') {
                println("Kill: " + item.name);
            } else if (item.name == 'end') {
                println("End: " + item.name);
            } else {
                println("Unknown: " + item.name);
            }
        }


        cout.writeLine("}")

    }


}

baseDirectory.eachDirRecurse { directory ->
    directory.eachFile(FileType.FILES, {
        file ->
            if (file.name.equals("workflow.xml")) {
//                println("File Parent: " + file.parentFile.name);
                buildDag(file, targetDirectory);
            }
    })
}