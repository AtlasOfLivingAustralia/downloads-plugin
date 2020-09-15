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

package au.org.ala.downloads

import grails.plugin.cache.Cacheable
import org.grails.web.util.WebUtils

import java.text.SimpleDateFormat

/**
 * Service to perform the records downloads (marshalls to appropriate web service)
 */
class DownloadService {
    def grailsApplication
    def webService   // via ala-ws-plugin
    def biocacheService

    /**
     * Records download service
     *
     * @param downloadParams
     * @return
     * @throws Exception
     */
    Map triggerDownload(DownloadParams downloadParams) throws Exception {

        if (downloadParams.downloadType == DownloadType.RECORDS.type) {
            // set some defaults
            downloadParams.file = downloadParams.file?:downloadParams.downloadType + "-" + new Date().format("yyyy-MM-dd")
            // catch different formats
            if (downloadParams.downloadFormat == DownloadFormat.DWC.format) {
                // DwC download
                downloadParams.fields = biocacheService.getDwCFields()
                triggerOfflineDownload(downloadParams)
            } else if (downloadParams.downloadFormat == DownloadFormat.LEGACY.format) {
                // Legacy download
                downloadParams.extra = grailsApplication.config.biocache.downloads.extra?: ""
                downloadParams.fields = grailsApplication.config.biocache.downloads.legacy.defaultFields?: ""
                downloadParams.dwcHeaders = false
                log.debug "downloadParams = ${downloadParams} | ${grailsApplication.config.biocache.downloads.extra}"
                triggerOfflineDownload(downloadParams)
            } else if (downloadParams.downloadFormat == DownloadFormat.CUSTOM.format) {
                // Custom download
                List<String> customFields = [grailsApplication.config.downloads.dwcExtraFields]
                downloadParams.customClasses.each {
                    log.debug "classs = ${it}"

                    List dwcClasses = grailsApplication.config.downloads.customSections.darwinCore ?: []
                    log.debug "dwcClasses = ${dwcClasses}"
                    if (dwcClasses.contains(it)) {
                        customFields.addAll(biocacheService.getFieldsForDwcClass(it))
                    } else if (grailsApplication.config.downloads.containsKey(it)) {
                        def fields = grailsApplication.config.downloads.get(it)
                        customFields.addAll(fields)
                    } else if (it == "qualityAssertions") {
                        downloadParams.qa = "includeall"
                    } else if (it == "miscellaneousFields") {
                        downloadParams.includeMisc = true
                    } else if (it == "selectedLayers") {
                        //already added to extra
                    } else if (it =~ /^dr\d+/) {
                        // species list DR
                        downloadParams.extra = (downloadParams.extra) ? "${downloadParams.extra},${it}" : it
                    } else {
                        throw new Exception("Custom field class not recognised: ${it}")
                    }
                }
                downloadParams.fields = customFields.findAll({it != null && it != ""}).join(",")
                triggerOfflineDownload(downloadParams)
            } else {
                def msg = "Download records format not recognised: ${downloadParams.downloadFormat}"
                log.warn msg
                throw new IllegalArgumentException(msg)
            }

        } else if (downloadParams.downloadType == DownloadType.CHECKLIST.type) {
            log.info "${DownloadType.CHECKLIST.type} download triggered"
        } else if (downloadParams.downloadType == DownloadType.FIELDGUIDE.type) {
            log.info "${DownloadType.FIELDGUIDE.type} download triggered"
        } else {
            def msg = "Download type not recognised: ${downloadParams.downloadType}"
            log.warn msg
            throw new IllegalArgumentException(msg)
        }
    }

    /**
     * Send the occurrences download request to biocache-service
     *
     * @param downloadParams
     * @return
     * @throws Exception
     */
    Map triggerOfflineDownload(DownloadParams downloadParams) throws Exception {
        String url = grailsApplication.config.downloads.indexedDownloadUrl + downloadParams.biocacheDownloadParamString()
        Map resp

        if (url.length() < 8000) {
            log.debug "Doing GET on ${url}"
            resp = webService.get(url)
        } else {
            // TODO send via POST
            def msg = "Download GET requests too long (> 8000 char) -> ${url}"
            log.error msg
            throw new Exception(msg)
        }


        if (resp?.resp) {
            resp.resp.put("requestUrl", url)
            resp.resp
        } else {
            throw new Exception("${resp?.error}")
        }
    }

    /**
     * Get the list of reason codes from logger system
     * TODO: provide option to read this from a config file
     *
     * @return
     */
    @Cacheable('longTermCache')
    List getLoggerReasons() {
        def url = "${grailsApplication.config.logger.baseUrl}/logger/reasons"
        try {
            webService.get(url).resp.findAll { !it.deprecated } // skip deprecated reason codes
        } catch (Exception ex) {
            log.error "Error calling logger service: ${ex.message}", ex
        }
    }

    /**
     * Get the list of reason sources from logger system
     * TODO: provide option to read this from a config file
     *
     * @return
     */
    @Cacheable('longTermCache')
    List getLoggerSources() {
        def url = "${grailsApplication.config.logger.baseUrl}/logger/sources"
        try {
            webService.get(url).resp
        } catch (Exception ex) {
            log.error "Error calling logger service: ${ex.message}", ex
        }
    }

    /**
     * Prepare the field guide download request to be sent to the field-guide server
     *
     * @param params
     * @return
     */
    def triggerFieldGuideDownload(String params) {
        String url = grailsApplication.config.downloads.fieldguideDownloadUrl + '/generate/offline'

        //detect fieldguide vs biocache-hub url. fieldguide url returns 400 when missing email parameter
        if (webService.post(url, null)?.statusCode != 400) {
            null
        } else {
            def resp = fieldGuideRequest(params)

            if (resp?.statusUrl) {
                resp.put("requestUrl", url)
                resp
            } else {
                throw new Exception((String) resp?.error?.toString())
            }
        }
    }

    /**
     * Run the field guide download via POST
     * 
     * @param params
     * @return
     */
    private fieldGuideRequest(String params) {
        String flimit = params.replaceAll("^.*maxSpecies=|[^0-9]+.*","")
        String requestParams = params.replaceAll("pageSize=[0-9]+|flimit=[0-9]+|facets=[a-zA-Z_]+", "") +
                "&pageSize=0&flimit=" + (flimit?:grailsApplication.config.downloads.fieldguide.species.max) + "&facet=true&facets=species_guid"

        def result = webService.get(grailsApplication.config.biocache.baseUrl + "/occurrences/search" + requestParams)

        def fg = [guids: [], link: "", title: ""]

        result?.resp?.facetResults?.each { fr ->
            if (fr.fieldName == 'species_guid') {
                fg.guids = fr.fieldResult?.label // groovy does an implicit collect
            }
        }

        if (fg.guids.isEmpty()) {
            [status: "error", message: "Error: No species were found for the requested search (${params})."]
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMMM yyyy")

            //set the properties of the query
            fg.title = "This document was generated on " + sdf.format(new Date())
            String serverName = grailsApplication.config.serverName ?: grailsApplication.config.security.cas.appServerName

            def request = WebUtils.retrieveGrailsWebRequest().getCurrentRequest()
            String contextPath = request.contextPath
            fg.link = serverName + contextPath + "/occurrences/search" + requestParams.replaceAll("flimit=[0-9]+|facets=[a-zA-Z_]+","")

            try {
                def response = webService.post(grailsApplication.config.downloads.fieldguideDownloadUrl + "/generate/offline" + params, fg)

                if (response?.resp) {
                    //response data
                    // {
                    //  "status": "inQueue",
                    //  "statusUrl": "http://fieldguide.ala.org.au/generate/status?id=30032017-fieldguide1490878211762.pdf"
                    // }
                    response.resp
                } else {
                    [status: "error", message: "failed to get fieldguide for this taxa"]
                }
            } catch (Exception ex) {
                log.error ex.getMessage(), ex
                [status: "error", message: "error generating fieldguide for this taxa"]
            }
        }
    }
}
