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

import org.apache.http.client.utils.URLEncodedUtils
import org.apache.http.NameValuePair

import java.nio.charset.Charset

class DownloadsTagLib {

    def downloadService
    //static defaultEncodeAs = [taglib:'html']
    //static encodeAsForTags = [tagName: [taglib:'html'], otherTagName: [taglib:'none']]
    static returnObjectForTags = ['getAllLoggerReasons','testListOutput','getLoggerReasons']
    static namespace = 'downloads'
    static defaultEncodeAs = "raw"

    static final UTF8_CHARSET =  Charset.lookup("UTF-8")
    static final SEPARATORS = "?;&".toCharArray()


    /**
     * Determine the URL prefix for biocache-service AJAX calls. Looks at the
     * biocache.ajax.useProxy config var to see whether or not to use the proxy
     */
    def getBiocacheAjaxUrl = { attrs ->
        String url = grailsApplication.config.biocache.baseUrl
        Boolean useProxy = grailsApplication.config.biocache.ajax.useProxy?.toString()?.toBoolean() // will convert String 'true' to boolean true
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
        List allreasons = downloadService.getLoggerReasons()
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
        def skin = grailsApplication.config.skin.layout?.toString()?.toUpperCase()
        log.debug "skin = ${skin}"
        def sources = downloadService.getLoggerSources()
        sources.each {
            log.debug "it = ${it}"
            if (it.name == skin) {
                out << it.id
            }
        }
    }

    def isDefaultDownloadFormat = { attrs ->

        if(grailsApplication.config.downloads.defaultDownloadFormat){
            if(grailsApplication.config.downloads.defaultDownloadFormat?.toString()?.equalsIgnoreCase(attrs.df.name())) {
                out << 'checked'
            }
        } else {
            if(attrs.df.ordinal() == 0) {
                out << 'checked'
            }
        }
    }

    /**
     * Get the list of available reason codes and labels from the Logger app
     *
     * Note: outputs an Object and thus needs:
     *
     *   static returnObjectForTags = ['getLoggerReasons']
     *
     * at top of taglib
     */
    def getLoggerReasons = { attrs ->
        downloadService.getLoggerReasons()
    }

    /**
     * Get the download reason "rkey" (default) or "name" for a given download reasonTypeId
     *
     * @attr id REQUIRED the reasonTypeId (id)
     * @attr field the reason field to return (rkey | name)
     */
    def getLoggerReasonString = { attrs ->
        def reasonsList = downloadService.getLoggerReasons()
        def returnField = attrs.field ?: "rkey"
        def reasonText = ""
        def reasonIndex

        try {
            reasonIndex = attrs.id as Integer
        } catch (NumberFormatException nfe) {
            // just log it
            log.warn "Couldn't convert id (${attrs.id}) to Integer in getLoggerReasonString(). ${nfe.localizedMessage}"
        }

        if (reasonIndex) {
            Map obj = reasonsList.find { it.id == reasonIndex}
            log.debug "getLoggerReasonString - ID: ${reasonIndex} || obj = ${obj} "

            if (obj && obj.containsKey(returnField)) {
                reasonText = obj.get(returnField)
            }
        }

        out << reasonText
    }

    /**
     * Add extra class name if current param name & value match the provided
     * version
     *
     * @attr fld REQUIRED the field name
     * @attr val REQUIRED the value name
     */
    def btnState = { attrs ->
        def outputClass = ""
        def field = attrs.fld
        def value = attrs.val
        def activeClass = attrs.msg ?: "btn-inverse"

        log.debug "field = ${field} || value = ${value}"
        log.debug "params = ${params}"

        if (params.containsKey(field) && (params.get(field) as String) == value) {
            outputClass = activeClass
        } else if (field == "*" && value == "*") {
            outputClass = activeClass
        }

        out << outputClass
    }

    /**
     * If field.info exists, then format field.dwcTerm as a link
     * with field.info as the href attr.
     *
     * @attr field REQUIRED the field Map
     */
    def formatDwcTerm = { attrs ->
        Map fieldMap = attrs.field
        def text = fieldMap?.dwcTerm

        if (fieldMap.containsKey("info")) {
            String link = fieldMap.info
            if (link.startsWith("http")) {
                text = "<a href='${link}' target='ext'>${text}</a>"
            }
        }

        out << text
    }

    /**
     * Format HTML to be inserted into popup
     *
     * @attr group REQUIRED
     * @attr filter REQUIRED
     * @attr fields REQUIRED
     * @attr action
     */
    def groupHelpHtml = { attrs ->
        def group = attrs.group
        def filter = attrs.filter
        def fields = attrs.fields
        def action = attrs.action ?: "fields"
        def html
        log.debug "attrs = ${attrs}"

        if (filter && fields) {
            html =  "<a href='${g.createLink(action: action)}?filter=${filter}' target='_fields'>" +
                    g.message(code:"downloads.fields.group.${group}", default: group) +
                    g.message(code:"downloads.taglib.list.fields", default: " (click for full list of fields)</a> which include: ") +
                    "[${fields}]"
        } else {
            html = g.message(code:"downloads.fields.group.${group}", default: group)
        }

        out << html
    }

    def sanitiseRawContent = { attrs ->
        String content = attrs.content
        try {
            String sanitised = org.jsoup.Jsoup.clean(content, org.jsoup.safety.Whitelist.basic())
            out << sanitised
        } catch (Exception e) {
            log.error "sanitiseRawContent failed for '${content}'. " +
                    "This will be ignored in the output to allow the calling page to display.", e
        }
    }
    /**
     * Format search query
     *
     * @attr searchUrl REQUIRED
     * @attr queryTitle
     */
    def formatSearchQuery = { attrs, body ->
        def searchUrl = attrs.searchUrl
        def queryTitle = attrs.queryTitle
        def content = ""

        try {
            if(queryTitle) {
                queryTitle = org.jsoup.Jsoup.clean(queryTitle, org.jsoup.safety.Whitelist.basic())
            }

            log.debug "searchUrl = ${searchUrl} || queryTitle = ${queryTitle}"

            if (searchUrl) {
                List<NameValuePair> params = URLEncodedUtils.parse(searchUrl, UTF8_CHARSET, SEPARATORS)
                content += "<ul class='searchQueryParams'>"

                for (NameValuePair param : params) {
                    if (param.name && param.value) {
                        String paramValue = ((param.name == "q" && queryTitle) ? queryTitle : param.value)
                        paramValue = paramValue.replaceAll(/ (AND|OR) /, " <span class=\"boolean-op\">\$1</span> ")
                        content += "<li><strong>${g.message code: "doi.param.name.${param.name}", default: "${param.name}"}:</strong>&nbsp;"
                        List fieldItems = paramValue.tokenize(':')
                        log.debug "fieldItems = ${fieldItems.size()}"
                        if (fieldItems.size() == 2 && paramValue != "*:*") {
                            // Attempt to substitute i18n labels where possible
                            content += "${g.message code: "facet.${fieldItems[0]}", default: "${fieldItems[0]}"}:"
                            log.debug "if: i18n: \"facet.${fieldItems[0]}\" || ${g.message(code: "facet.${fieldItems[0]}")}"
                            content += "${g.message code: "${fieldItems[0]}.${fieldItems[1]}", default: "${fieldItems[1]}"}</li>"
                        } else {
                            content += "${g.message code: "doi.param.value.${paramValue}", default: "${paramValue}"}</li>"
                            log.debug "else: i18n: \"doi.param.value.${paramValue}\" || ${g.message(code: "doi.param.value.${paramValue}")}"
                        }
                    }

                }

                content += "</ul>"
            }

            out << content
        } catch (Exception e) {
            log.error "formatSearchQuery failed for searchUrl = '${searchUrl}' and queryTitle = '${queryTitle}'. " +
                    "This will be ignored in the output to allow the calling page to display.", e
        }
    }
}
