package au.org.ala.downloads

import grails.converters.JSON
import grails.plugin.cache.Cacheable
import org.apache.commons.collections.CollectionUtils

import javax.annotation.PostConstruct

/**
 * Service to access data from biocache-service
 */
class BiocacheService {

    def grailsApplication
    def grailsResourceLocator
    def webService
    def utilityService
    BiocacheService thisService // placeholder

    /**
     * Init method - initialise fields after Spring has instantiated this singleton
     *
     * @return
     */
    @PostConstruct
    void init() {
        thisService = grailsApplication.mainContext.biocacheService // to maintain cacheable functionality for internal method calls
    }

    /**
     * Get fields info from http://biocache.ala.org.au/ws/index/fields.
     * Example record:
     *
     * {
     *   dwcTerm: "basisOfRecord",
     *   downloadDescription: "Basis Of Record - processed",
     *   indexed: true,
     *   stored: true,
     *   downloadName: "basisOfRecord.p",
     *   multivalue: false,
     *   classs: "Record",
     *   description: "Basis Of Record - processed",
     *   dataType: "string",
     *   name: "basis_of_record"
     * }
     *
     * @return fields (List)
     */
    @Cacheable('longTermCache')
    List getBiocacheFields()  {
        List fields
        Map resp = webService.get(grailsApplication.config.downloads.indexedFieldsUrl)

        if (resp?.resp) {
            fields = resp.resp
        } else {
            String msg = "Error calling service: ${grailsApplication.config.downloads.indexedFieldsUrl}. Message: ${resp?.error}"
            throw new RuntimeException(msg)
        }

        fields.findAll({ !getExcludedFields().contains(it.name) })
    }

    @Cacheable('longTermCache')
    List getFieldsForDwcClass(String classsName) {
        if (!classsName) {
            throw new IllegalArgumentException("classsName argument not provided")
        }

        List fields = []
        Map fieldsMap = thisService.getFieldsMap(true)

        if (fieldsMap.containsKey(classsName)) {
            fields = fieldsMap.get(classsName)
        }

        log.debug "Getting fields for ${classsName} = ${fields}"

        orderFieldsByDwC(fields)
    }

    @Cacheable('longTermCache')
    Map getFieldsMap(boolean includeRaw = false) {
        List fields = thisService.getBiocacheFields()
        Map fieldsByClassMap = [:]
        Map classLookup = grailsApplication.config.downloads.classMappings

        fields.each { field ->

            if (field && field?.dwcTerm && !(!includeRaw && field.dwcTerm.contains("_raw"))) {
                // only includes "_raw" fields when 'includeRaw' is true
                String classsName = field?.classs ?: "Misc"
                String key = classLookup.get(classsName) ?: "Misc"
                List fieldsList = fieldsByClassMap.get(key) ?: []
                //fieldsList.add(field?.downloadName)
                fieldsList.add([downloadName:field?.downloadName, dwcTerm: field?.dwcTerm])
                fieldsByClassMap.put(key, fieldsList) // overwrites with new list
            }
        }

        log.debug "getFieldsMap(${includeRaw}) -> ${fieldsByClassMap as JSON}"
        fieldsByClassMap
    }

    @Cacheable('longTermCache')
    String getDwCFields() {
        List fields = []
        Boolean includeRaw = utilityService.getBooleanValue(grailsApplication.config.includeRawDwcFields?:true)
        Map fieldsMap = thisService.getFieldsMap(includeRaw) // caching maintained

        fieldsMap.keySet().each {
            log.debug "getDwCFields - ${it} = ${fieldsMap.get(it)}"
            fields.addAll(fieldsMap.get(it))
        }

        String extraFields = grailsApplication.config.getProperty('downloads.dwcExtraFields', String, "")
        ((extraFields != "") ? extraFields + ',' : "") + orderFieldsByDwC(fields).join(",") // comma-separated string
    }

    /**
     * Order the list of fields by DwC order and then by alphabetic order
     *
     * @param fields (List<Map> {downloadName, dwcTerm})
     * @return orderedFields List<String>
     */
    List orderFieldsByDwC(List fields) {
        List orderedFields = []
        List dwcOrdered = thisService.getDwCFieldsOrdered() // cached List contains dwcTerms only - e.g. type, class, kingdom
        List dwcTermsOnly = fields.collect { it.dwcTerm?.replaceAll("dcterms:","") } // flatten List - dwcOrdered has namespace removed

        dwcOrdered.each { field ->
            // fields can contain duplicate entries for dwcTerm (due to raw and processed version
            // which is why #findIndexValues() is used
            List indexValues = dwcTermsOnly.findIndexValues{ it == field} // returns a List<Long> for the matching index positions

            if (indexValues.size() > 0) {
                indexValues.each { Long val ->
                    // add matching terms to orderedFields
                    orderedFields.add(fields.get(val.intValue()))
                }
            }
        }

        List remainingFields = CollectionUtils.subtract(fields, orderedFields) // get the "remaining" list of fields
        orderedFields.addAll(remainingFields.sort()) // sort remainingFields and append to orderedFields
        List outputFields = orderedFields.findAll {it.downloadName != null}.collect { it.downloadName } // flatten to just list of fields

        outputFields
    }

    @Cacheable('longTermCache')
    List getAllFields() {
        getBiocacheFields()
    }

    @Cacheable('longTermCache')
    String getDwCDescriptionForField(String field) {
        List fields = getBiocacheFields()

        Map entry = fields.find { it.dwcTerm == field }
        entry?.containsKey("downloadDescription") ? entry.get("downloadDescription") : ""
    }

    @Cacheable('longTermCache')
    List getDwCFieldsOrdered() {
        List dwcFieldsOrdered

        try {
            // DwC fields canonical version found at https://raw.githubusercontent.com/tdwg/dwc/master/downloads/SimpleDwCCSVheaderUTF8.txt
            dwcFieldsOrdered = grailsResourceLocator.findResourceForURI('classpath:SimpleDwCCSVheaderUTF8.txt').getInputStream().text.replaceAll("\"","").split(",")
            log.debug "dwcFieldsOrdered = ${dwcFieldsOrdered}"
        } catch (Exception ex) {
            log.error "Failed to load SimpleDwCCSVheaderUTF8.txt file - ${ex.message}", ex
        }

        dwcFieldsOrdered
    }

    List getExcludedFields() {
        String excludedFields = grailsApplication.config.downloads.excludeFields?: ""
        excludedFields.split(",")
    }
}
