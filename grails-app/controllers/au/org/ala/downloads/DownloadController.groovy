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

import grails.converters.JSON
import grails.core.GrailsApplication
import grails.web.mapping.LinkGenerator
import org.springframework.beans.factory.annotation.Autowired

/**
 * Download controller
 * TODO: use more meaningful method names in this controller!
 */
class DownloadController {
    def customiseService, authService, downloadService, biocacheService, utilityService, doiService

    GrailsApplication grailsApplication

    @Autowired
    LinkGenerator linkGenerator

    static defaultAction = "options1"

    /**
     * Initial download screen with options.
     *
     * @param downloadParams
     * @return
     */
    def options1(DownloadParams downloadParams) {
        log.debug "biocacheDownloadParamString = ${downloadParams.biocacheDownloadParamString()}"
        log.debug "request.getHeader('referer') = ${request.getHeader('referer')}"
        log.debug "downloadParams = ${downloadParams.toString()}"
        downloadParams.file = DownloadType.RECORDS.type + "-" + new Date().format("yyyy-MM-dd")

        boolean askForEmail = (authService?.getEmail() == null);

        if (downloadParams.searchParams) {
            render (view:'options1', model: [
                    searchParams: downloadParams.searchParams,
                    targetUri: downloadParams.targetUri,
                    filename: downloadParams.file,
                    totalRecords: downloadParams.totalRecords,
                    askForEmail: askForEmail,
                    defaults: [ sourceTypeId: downloadParams.sourceTypeId,
                                downloadType: downloadParams.downloadType,
                                downloadFormat: downloadParams.downloadFormat,
                                fileType: downloadParams.fileType,
                                layers: downloadParams.layers,
                                layersServiceUrl: downloadParams.layersServiceUrl,
                                customHeader: downloadParams.customHeader]
            ])
        } else {
            flash.message = "Download error - No search query parameters were provided."
            def redirectUri = request.getHeader('referer') ?: "/"
            redirect(uri: redirectUri)
        }
    }

    /**
     * Action after initial download screen.
     * Either redirects user to customise screen or to confirmation page & triggers download
     *
     * @param downloadParams
     * @return
     */
    def options2(DownloadParams downloadParams) {

        downloadParams.email = authService?.getEmail() ?: downloadParams.email // if AUTH is not present then email should be populated via input on page

        if (!downloadParams.downloadType || !downloadParams.reasonTypeId) {
            flash.message = "No type or reason selected. Please try again."
            redirect(action: "options1", params: params)
        } else if (downloadParams.downloadType == DownloadType.RECORDS.type && downloadParams.downloadFormat == DownloadFormat.CUSTOM.format && !downloadParams.customClasses) {
            // Customise download screen
            Map sectionsMap = biocacheService.getFieldsMap()
            log.debug "sectionsMap = ${sectionsMap as JSON}"
            Map customSections = grailsApplication.config.getRequiredProperty("downloads.customSections", Map).clone()
            // work-around for mixing groovy and property files for config values see issue #53
            List miscItems = grailsApplication.config.getProperty("downloads.customSections.misc", List, [])
            if (miscItems) {
                customSections.misc = miscItems  // replace with sanitised version via getProperty()
            }
            //add preselected layer selection to "SPATIAL INTERSECTIONS"
            def mandatory = grailsApplication.config.downloads.mandatoryFields.clone()
            if (downloadParams.layers) {
                def sections = customSections.get("spatialIntersections")
                if (sections) {
                    sections = sections.clone()
                    sections.add("selectedLayers")
                } else {
                    customSections.put("spatialIntersections", ["selectedLayers"])
                }
                mandatory.add("selectedLayers")
            }

            render (view:'options2', model: [
                    customSections: customSections,
                    mandatoryFields: mandatory,
                    dwcClassesAndTerms: utilityService.getFieldGroupMap(),
                    groupingsFilterMap: grailsApplication.config.downloads.groupingsFilterMap,
                    userSavedFields: customiseService.getUserSavedFields(request?.cookies?.find { it.name == 'download_fields'}, authService?.getUserId()),
                    downloadParams: downloadParams
            ])
        } else if (downloadParams.downloadType == DownloadType.RECORDS.type) {
            // Records download -> confirm
            // targetUri already contains the context path but linkGenerator does not know about it
            // hence we have to manually trim it.
            downloadParams.mintDoi = grailsApplication.config.getProperty('doi.mintDoi', Boolean, false)
            downloadParams.searchUrl = linkGenerator.link(uri: downloadParams.targetUri.replace(linkGenerator.contextPath,""), absolute:true) + downloadParams.searchParams
            downloadParams.doiDisplayUrl = linkGenerator.link(controller: 'download', action: 'doi', params:[doi:''], absolute:true)
            downloadParams.hubName = grailsApplication.config?.info?.app?.description
            // perform the download
            def json = downloadService.triggerDownload(downloadParams)
            chain (action:'confirm', model: [
                    isQueuedDownload: true,
                    downloadParams: downloadParams,
                    json: json // Map
            ], params:[
                    searchParams: downloadParams.searchParams,
                    targetUri: downloadParams.targetUri,
                    downloadType: downloadParams.downloadType])
        } else if (downloadParams.downloadType == DownloadType.CHECKLIST.type) {
            // Checklist download
            def extraParamsString = "&facets=species_guid&lookup=true&counts=true&lists=true"
            chain (action:'confirm', model: [
                    isQueuedDownload: false,
                    isChecklist: true,
                    downloadParams: downloadParams,
                    downloadUrl: grailsApplication.config.downloads.checklistDownloadUrl + downloadParams.biocacheDownloadParamString() + extraParamsString
            ], params: [
                    searchParams: downloadParams.searchParams,
                    targetUri: downloadParams.targetUri,
                    downloadType: downloadParams.downloadType])
        } else if (downloadParams.downloadType == DownloadType.FIELDGUIDE.type) {
            // Field guide download
            def extraParamsString = "&facets=species_guid"
            chain (action:'confirm', model: [
                    isQueuedDownload: false,
                    isFieldGuide: true,
                    downloadParams: downloadParams,
                    json: downloadService.triggerFieldGuideDownload(downloadParams.biocacheDownloadParamString() + extraParamsString),
                    downloadUrl: grailsApplication.config.downloads.fieldguideDownloadUrl + downloadParams.biocacheDownloadParamString() + extraParamsString
            ], params:[searchParams: downloadParams.searchParams, targetUri: downloadParams.targetUri, downloadType: downloadParams.downloadType])
        } else {
            log.warn"Fell through `downloadType` if-else -> downloadParams = ${downloadParams}"
        }
    }

    /**
     * Confirmation page
     *
     * @param downloadParams
     * @return
     */
    def confirm (DownloadParams downloadParams) {

        Map returnModel = [isChained: true]
        if (!chainModel) {
            returnModel = [
                    isChained: false,
                    isQueuedDownload: (downloadParams.downloadType == DownloadType.RECORDS.type) ? true : false,
                    isFieldGuide: (downloadParams.downloadType == DownloadType.FIELDGUIDE.type) ? true : false,
                    isChecklist: (downloadParams.downloadType == DownloadType.CHECKLIST.type) ? true : false,
                    downloadParams: downloadParams
            ]
        }

        returnModel
    }

    /**
     * Paginated list of downloads for logged-in user
     *
     * @return
     */
    def myDownloads() {
        redirect url: "${grailsApplication?.config.doiService.baseUrl}/myDownloads"
    }

    /**
     * View a download via it's DOI identifier (lookup)
     *
     * @return
     */
    def doi() {
        redirect url: "${grailsApplication?.config.doiService.baseUrl}/doi/${params?.doi}"
    }

    /**
     * Remember the user's options for customise download page
     *
     * @return
     */
    def saveUserPrefs() {
        List fields = params.list("fields")

        try {
            def res = customiseService.setUserSavedFields(authService?.getUserId(), fields)
            render res as JSON
        } catch (Exception ex) {
            log.error("Error saving user preferences: ${ex.message}", ex)
            render(status: "400", text: "Error saving user preferences: ${ex.message}")
        }

    }

    /**
     * Display the description attribute for a download field (via it's ID)
     *
     * @param id
     * @return
     */
    def getDescription(String id) {
        if (id) {
            String description = biocacheService.getDwCDescriptionForField(id)
            Map response = [field: id, description: ""]

            if (description) {
                response.description = description
            }

            render (response as JSON)
        } else {
            render (status: 400, text: "no field provided")
        }
    }

    /**
     * Provides a human-readable version of the Biocache /ws/index/fields JSON data.
     * Allows filtering and paginating of fields.
     *
     * @return
     */
    def fields() {
        List fields = biocacheService.getAllFields()
        params.max = params.max ?: 10
        params.order = params.order ?: "ASC"
        params.sort = params.sort ?: "name"
        params.filter = params.filter ?: ""

        if (params.filter) {
            def fld = "name" // default
            def val = params.filter
            def parts = val.split(":") // allow SOLR style: &filter=foo:bar

            if (parts.size() == 2) {
                fld = parts[0]
                val = parts[1]
                fields = fields.findAll() { it[fld] ==~ /${val}/  }
            } else {
                fields = fields.findAll() { Map it ->
                    it.values().join(" ").find(/${val}/) // search in any property
                }
            }
        }

        if (params.dwc) {
            fields = fields.findAll() { it.dwcTerm }
        }

        render (view: "fields", model: [
                fields: utilityService.paginateWrapper(fields, params),
                fieldsMax: fields.size()
        ])
    }
}
