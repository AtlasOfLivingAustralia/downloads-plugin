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

// These are sensible defaults that are copied into the application's config object and can be overridden
// by the client application or its external config file

downloads.indexedDownloadUrl = "https://biocache-ws.ala.org.au/ws/occurrences/offline/download"
downloads.checklistDownloadUrl = "https://biocache-ws.ala.org.au/ws/occurrences/facets/download"

// For online fieldguide generation use biocache-hub webservice url e.g. https://biocache.ala.org.au/occurrences/fieldguide/download
// For offline fieldguide generation use fieldguide url e.g. https://fieldguide.ala.org.au
downloads.fieldguideDownloadUrl = "https://fieldguide.ala.org.au"

downloads.indexedFieldsUrl = "https://biocache-ws.ala.org.au/ws/index/fields"

// The parent application should also use biocache.baseUrl, e.g. biocache-hubs
biocache.baseUrl = "https://biocache.ala.org.au/ws"

// Define userdetails.baseUrl to enable saving of fields to userdetails instead of cookies.
// e.g. userdetails.baseUrl = "https://auth.ala.org.au/userdetails"
userdetails.baseUrl = ""

downloads {
    customSections {
        //darwinCore = ["recordLevelTerms","occurrence","organism","materialSampleSpecimen","location","geologicalContext","identification","taxon","measurementOrFact","resourceRelationship"],
        darwinCore = ["recordLevelTerms", "occurrence", "organism", "event", "location", "identification", "taxon", "measurementOrFact", "geologicalContext", "resourceRelationship", "materialSample"]
        speciesTraits = ["conservationStatus", "otherTraits"]
        spatialIntersections = ["environmentalLayers", "contextualLayers"]
        misc = ["qualityAssertions","miscellaneousFields"]
    }
    classMappings {
        Record = "recordLevelTerms"
        Occurrence = "occurrence"
        Organism = "organism"
        Event = "event"
        Location = "location"
        GeologicalContext = "geologicalContext"
        Identification = "identification"
        Taxon = "taxon"
        MeasurementOrFact = "measurementOrFact"
        ResourceRelationship = "resourceRelationship"
        MaterialSample = "materialSample"
    }
    groupingsFilterMap {
        // values used to link through to the index fields web page, via filter param
        recordLevelTerms = "classs:Record"
        occurrence = "classs:Occurrence"
        organism = "classs:Organism"
        event = "classs:Event"
        location = "classs:Location"
        identification = "classs:Identification"
        taxon = "classs:Taxon"
        measurementOrFact = "classs:MeasurementOrFact"
        conservationStatus = ".*Conservation"
        otherTraits = "species_group%7cspecies_subgroup"
        environmentalLayers = "name:el[0-9]*"
        contextualLayers = "name:cl[0-9]*"
        qualityAssertions = "assertion"
        miscellaneousFields = ""
        resourceRelationship = "classs:ResourceRelationship"
        materialSample = "classs:MaterialSample"
        geologicalContext = "classs:GeologicalContext"
    }
}

// other field mappings for classes TODO get from biocache-service
downloads.conservationStatus = ["aust_conservation","state_conservation"]
downloads.otherTraits = ["species_group","species_subgroup"]
downloads.environmentalLayers = ["el_p"]
downloads.contextualLayers = ["cl_p"]
//biocache.downloads.extra = "dataResourceUid,dataResourceName.p,occurrenceStatus.p"

// legacy default fields taken from https://github.com/AtlasOfLivingAustralia/biocache-service/blob/master/src/main/java/au/org/ala/biocache/dto/DownloadRequestParams.java#L35
downloads.legacy.defaultFields = "id,data_resource_uid,data_resource,license,catalogue_number,taxon_concept_lsid,raw_taxon_name,raw_common_name,taxon_name,rank,common_name,kingdom,phylum,class,order,family,genus,species,subspecies,institution_code,collection_code,locality,raw_latitude,raw_longitude,raw_datum,latitude,longitude,coordinate_precision,coordinate_uncertainty,country,state,cl959,min_elevation_d,max_elevation_d,min_depth_d,max_depth_d,individual_count,recorded_by,year,month,day,verbatim_event_date,basis_of_record,raw_basis_of_record,sex,preparations,information_withheld,data_generalizations,outlier_layer,taxonomic_kosher,geospatial_kosher"
downloads.mandatoryFields = ["recordLevelTerms","occurrence"]
downloads.excludeFields = ""
downloads.dwcExtraFields = "data_resource_uid" // It was "uuid" for biocache 1.9.x

downloads.fieldguide.species.max = 1000
downloads.maxRecords = 1000000
downloads.staticDownloadsUrl = "https://downloads.ala.org.au"
downloads.dwcSchemaUrl = "https://raw.githubusercontent.com/tdwg/dwc/master/xsd/tdwg_dwcterms.xsd"
downloads.termsOfUseUrl = "https://www.ala.org.au/about-the-atlas/terms-of-use/#TOUusingcontent"
downloads.defaultDownloadFormat = "dwc"
downloads.includeRawDwcFields = "true" // keep it a string to be consistent with properties file
downloads.gaCustomData = true  // Google Analytics custom dimensions data - set to empty for non-ALA installations
downloads.refresh.confirm.maxTimeout = 20

doiService.baseUrl = "https://doi.ala.org.au"
doiService.doiResolverUrl = "https://doi.org/"

filetype.shapefile.disable = true // set to false, to revert to previous behaviour - will probably/eventually be deprecated in biocache-service
filetype.shapefile.hidden = false
shapefile.kb.url = "https://support.ala.org.au/support/solutions/articles/6000226357"
