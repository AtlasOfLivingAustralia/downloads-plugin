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
import grails.validation.Validateable
import groovy.util.logging.Log4j
import org.apache.commons.lang.StringUtils
import org.codehaus.groovy.grails.web.util.WebUtils

/**
 * DTO for the params in both the download form and the backend download service
 *
 * @author "Nick dos Remedios <Nick.dosRemedios@csiro.au>"
 */
@Validateable
@Log4j
class DownloadParams {
    String searchParams // q, fq and qc params as query string, URI encoded when sent from browser
    String targetUri // path to page calling the download form (so we can return to that page after download complete)
    String downloadType // records, checklist or field guide TODO put in an Enum?
    String downloadFormat // full-dwc, legacy, custom TODO put in an Enum?
    List customClasses // custom download page - list of field classes
    //
    // Fields for download service
    // see https://github.com/AtlasOfLivingAustralia/biocache-service/blob/master/src/main/java/au/org/ala/biocache/dto/DownloadRequestParams.java
    //
    String reasonTypeId
    String sourceTypeId
    String fields
    String extra // "dataResourceUid,dataResourceName.p,occurrenceStatus.p"
    String file  // file name for download file
    String email
    Boolean dwcHeaders = true
    Boolean includeMisc = false // Miscellaneous fields
    String qa = "none" // can be empty (get default qa fields), "includeall" (get empty fields so same columns every time), "none",  a comma separated fields or "all"

    @Override
    public String toString() {
        Map paramsMap = mapForPropsWithExcludeList()
        //WebUtils.toQueryString(paramsMap)
        paramsMap as JSON
    }

    public String queryString() {
        Map paramsMap = mapForPropsWithExcludeList()
        WebUtils.toQueryString(paramsMap)
    }

    private Map mapForPropsWithExcludeList(List excludes = []) {
        Map paramsMap = [:]
        List excludeParams = ["mapForPropsWithExcludeList","class","constraints","errors","ValidationErrors","action","controller"]

        if (excludes) {
            excludeParams.addAll(excludes)
        }

        this.properties.each { prop, val ->
            if (val && !excludeParams.contains(prop)) {
                paramsMap.put(prop, val)
            }
        }

        paramsMap
    }

    public String biocacheDownloadParamString() {
        Map paramsMap = mapForPropsWithExcludeList(["searchParams","targetUri","downloadType","downloadFormat","customClasses"])
        String queryString = WebUtils.toQueryString(paramsMap) + "&" + StringUtils.removeStart(searchParams,"?")
        queryString
    }
}
