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

import groovy.util.logging.Log4j

/**
 * Enum for file type
 *
 */
@Log4j
enum FileType {
    CSV("csv"),
    TSV("tsv"),
    SHAPE("shapefile")

    String type

    FileType(String name) {
        this.type = name
    }

    @Override
    public String toString() {
        return this.type;
    }

    /**
     * Do a lookup on the format field
     *
     * @param type
     * @return
     */
    public static FileType valueOfType(String type) {
        for (FileType ff : values()) {
            if (ff.format.equals(type)) {
                log.info "matched value = ${ff}"
                return ff
            }
        }
        throw new IllegalArgumentException("No enum const " + FileType.class + "@format." + type);
    }
}
