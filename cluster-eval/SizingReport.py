#!/usr/bin/env python

import optparse
from optparse import OptionGroup
import logging
import sys
import os
import json

Mb = 1024*1024
Gb = Mb * 1024

def report(clusterInfo):
    host_count = 0
    with open(clusterInfo) as info_file:
        data = json.load(info_file)
        try:
            print "Cluster: " + data['items'][0]['Hosts']['cluster_name'] + " has " + str(len(data['items'])) + " hosts."
            host_count += len(data['items'])
            # Get each item
            for item in data['items']:
                # isolate the host element
                try:
                    host = item['Hosts']
                    host_name = host["host_name"]
                    host_detail = ""
                    diskCount = 0
                    diskCap = 0
                    # Loop through the disks
                    try:
                        for disk in host["disk_info"]:
                            diskCount += 1
                            diskCap += int(disk["size"])
                        # Summarized each host
                        host_detail = " has " + str(host["cpu_count"]) + "vCores, " + str(host["total_mem"] / Mb) + "Gb RAM, and " \
                              + str(diskCount) + " disks with a total capacity of " + str(diskCap / Mb) + " Gb"
                    except:
                        host_detail = " No host detail information supplied"
                    print "\t[" + host_name + "]" + host_detail
                except:
                    print "No host information supplied"
        except Exception as e:
            e.message
    return host_count


def process_dir(directory, host_count=0):
    for root, subdirs, files in os.walk(directory):
        #print root
        # for subdir in subdirs:
        #     #print('\t- subdirectory ' + subdir)
        #     subdir_path = os.path.join(root, subdir)
        #     process_dir(subdir_path)
            
        for filename in files:
            file_path = os.path.join(root, filename)
            if filename.lower().endswith(".json"):
                print('File %s (full path: %s)' % (filename, file_path))
                host_count += report(file_path)
    return host_count

def main():
    parser = optparse.OptionParser(usage="usage: %prog [options]")

    parser.add_option("-i", "--cluster-info", dest="cluster_info", help="Cluster Information JSON File")
    parser.add_option("-d", "--directory", dest="directory", help="Directory with Cluster Info Files")

    (options, args) = parser.parse_args()
    host_count = 0
    if options.cluster_info:
        host_count = report(str(options.cluster_info), host_count)

    if options.directory:
        host_count = process_dir(options.directory, host_count)

    print "Total Host Count: " + str(host_count)


main()