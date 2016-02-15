/*
 * Copyright (C) 2016 Atlas of Living Australia
 * All Rights Reserved.
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 */

package au.org.ala.downloads.plugin

class DownloadsTagLib {

    def webServicesService
    //static defaultEncodeAs = [taglib:'html']
    //static encodeAsForTags = [tagName: [taglib:'html'], otherTagName: [taglib:'none']]
    static returnObjectForTags = ['getAllLoggerReasons','testListOutput']
    static namespace = 'downloads'

    /**
     * Determine the URL prefix for biocache-service AJAX calls. Looks at the
     * biocache.ajax.useProxy config var to see whether or not to use the proxy
     */
    def getBiocacheAjaxUrl = { attrs ->
        String url = grailsApplication.config.biocache.baseUrl
        Boolean useProxy = grailsApplication.config.biocache.ajax.useProxy.toBoolean() // will convert String 'true' to boolean true
        log.debug "useProxy = ${useProxy}"

        if (useProxy) {
            url = g.createLink(uri: '/proxy')
        }

        out << url
    }

    /**
     * Get the list of available reason codes and labels from the Logger app
     *
     * Note: outputs an Object and thus needs:
     *
     *   static returnObjectForTags = ['getAllLoggerReasons']
     *
     * at top of taglib
     */
    def getAllLoggerReasons = { attrs ->
        List allreasons = webServicesService.getLoggerReasons()
        log.debug "allreasons => ${allreasons.getClass().name}"
        allreasons
    }

    def testListOutput = { attrs ->
        def outputList = [[name: "foo",id:0],[name: "bar",id:1]]
        outputList
    }

    /**
     * Get the appropriate sourceId for the current hub
     */
    def getSourceId = { attrs ->
        def skin = grailsApplication.config.skin.layout?.toUpperCase()
        log.debug "skin = ${skin}"
        def sources = webServicesService.getLoggerSources()
        sources.each {
            log.debug "it = ${it}"
            if (it.name == skin) {
                out << it.id
            }
        }
    }

}