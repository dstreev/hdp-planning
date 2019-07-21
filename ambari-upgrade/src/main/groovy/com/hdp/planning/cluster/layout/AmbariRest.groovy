package com.hdp.planning.cluster.layout

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovyx.net.http.RESTClient
//import net.sf.json.JsonConfig
//import net.sf.json.util.CycleDetectionStrategy

//import static groovyx.net.http.Method.GET
//import static groovyx.net.http.Method.PUT

import groovy.util.logging.Slf4j
import net.sf.json.JsonConfig

/**
 * Created by dstreev on 2015-11-08.
 */
@Slf4j
class AmbariRest {
    def url
    def clusterName
    def authString

    AmbariRest(String url, String clusterName, String user, String password) {
        this.url = url
        this.clusterName = clusterName

        def String up = user + ":" + password
        authString = up.getBytes().encodeBase64().toString()

    }

    Object getClusterInfo() {
        def apiUrl = url + "/api/v1/clusters/" + clusterName + "/hosts?fields=Hosts/host_name,host_components,Hosts/ip,Hosts/total_mem,Hosts/os_arch,Hosts/os_type,Hosts/rack_info,Hosts/cpu_count,Hosts/disk_info,metrics/disk,Hosts/ph_cpu_count"

        def http = new RESTClient(apiUrl)

        http.headers['Authorization'] = 'Basic ' + authString
        http.headers.'X-Requested-By' = 'ambari'
        http.headers.'X-Requested-With' = 'XMLHttpRequest'
        http.headers.'User-Agent' = 'Mozilla/5.0 Ubuntu/8.10 Firefox/3.0.4'

        def slurper = new JsonSlurper();

        def cluster

        http.request(GET) {

            response.success = { resp, json ->
                println resp.statusLine
                cluster = slurper.parse(json)

            }

            response.failure = { resp ->
                println "Unexpected error: ${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}"
            }
        }

        return cluster

    }

    Object setHostRack(host, rack) {
        def apiUrl = url + "/api/v1/clusters/" + clusterName + "/hosts"
        def jsonConfig = new JsonConfig();
        jsonConfig.setCycleDetectionStrategy(CycleDetectionStrategy.NOPROP);
        def putBody = new JsonBuilder(jsonConfig);

        putBody.RequestInfo {
            context "Set Rack"
            query "Hosts/host_name.in(" + host + ")"
            Body {
                Hosts {
                    rack_info rack
                }
            }
        }

        println putBody.toPrettyString()

//        def requestBody = '{"RequestInfo":{"context":"Set Rack","query":"Hosts/host_name.in(' + host + ')"},"Body":{"Hosts":{"rack_info":"' + rack + '"}}}'

        def http = new RESTClient(apiUrl)

        http.headers['Authorization'] = 'Basic ' + authString
        http.headers.'X-Requested-By' = 'Ambari'
        http.headers.'X-Requested-With' = 'XMLHttpRequest'
        http.headers.'User-Agent' = 'Mozilla/5.0 Ubuntu/8.10 Firefox/3.0.4'
        //http.headers.Accept = 'application/json';

//        http.put(JSON) { req ->
        http.request( PUT, JSON ) { req ->
            contentType = JSON
            req.body = [putBody] //.toString()
//            requestContentType = URLENC

            response.success = { resp, json ->
                println resp.statusLine

                json.readLine.each { line ->
                    println line
                }

            }

            response.failure = { resp ->
                println "Unexpected error: ${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}"
            }
        }

    }

}
