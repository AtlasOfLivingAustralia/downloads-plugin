%{--
  - Copyright (C) 2017 Atlas of Living Australia
  - All Rights Reserved.
  - The contents of this file are subject to the Mozilla Public
  - License Version 1.1 (the "License"); you may not use this file
  - except in compliance with the License. You may obtain a copy of
  - the License at http://www.mozilla.org/MPL/
  - Software distributed under the License is distributed on an "AS
  - IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
  - implied. See the License for the specific language governing
  - rights and limitations under the License.
  --}%

<%--
  Created by IntelliJ IDEA.
  User: dos009
  Date: 10/05/2017
  Time: 4:23 PM
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
    <meta name="fluidLayout" content="false"/>
    <title><g:message code="downloads.fields.heading" default="Occurrence Record Fields"/></title>
    <asset:javascript src="downloads.js" />
    <asset:stylesheet src="downloads.css" />
</head>
<body class="content">
    <div class="">
        <h1><g:message code="downloads.fields.heading" default="Occurrence Record Fields"/></h1>
    </div>
    <div style="padding-bottom:10px;">
        <g:message code="downloads.fields.intro" args="[ grailsApplication.config.downloads.indexedFieldsUrl ]" default="This table provides information on the occurrence record field types and mappings between SOLR (search) fields, Cassandra (download) fields,
        readable names, descriptions, Darwin Core terms and classes, as well as other miscellaneous attributes."/>
    </div>
    <div class="row" id="filters">
        <div class="col-md-12">
            <g:message code="downloads.fields.filters" default="Filters"/>:
            <div class="btn-group">
                <g:link class="btn btn-default btn-sm ${downloads.btnState(fld:'filter', val:'')}" action="fields" params="${params + [filter:'']}"><g:message code="downloads.fields.filter.all" default="all fields"/></g:link>
                <g:link class="btn btn-default btn-sm ${downloads.btnState(fld:'filter', val:'dwcTerm:.*')}" action="fields" params="${params + [filter:"dwcTerm:.*"]}"><g:message code="downloads.fields.filter.dwc" default="only DwC terms"/></g:link>
                <g:link class="btn btn-default btn-sm ${downloads.btnState(fld:'filter', val:'name:el[0-9]*')}" action="fields" params="${params + [filter:'name:el[0-9]*']}"><g:message code="downloads.fields.filter.environmental" default="only environmental layers"/></g:link>
                <g:link class="btn btn-default btn-sm ${downloads.btnState(fld:'filter', val:'name:cl[0-9]*')}" action="fields" params="${params + [filter:'name:cl[0-9]*']}"><g:message code="downloads.fields.filter.contextual" default="only contextual layers"/></g:link>
                <g:link class="btn btn-default btn-sm ${downloads.btnState(fld:'filter', val:'indexed:true')}" action="fields" params="${params + [filter:'indexed:true']}"><g:message code="downloads.fields.filter.indexed" default="only indexed fields"/></g:link>
                <g:link class="btn btn-default btn-sm ${downloads.btnState(fld:'filter', val:'jsonName:.*')}" action="fields" params="${params + [filter:'jsonName:.*']}"><g:message code="downloads.fields.filter.json" default="only JSON output fields"/></g:link>
            </div>
            <form class="form-inline" style="display:inline-block;">
                <div class="input-group input-group-sm">
                    <input class="input-sm form-control" name="filter" id="appendedInputButton" type="text" value="${params.filter}">
                    <span class="input-group-btn">
                        <input class="btn btn-default btn-sm" type="submit" value="Search">
                    </span>
                </div>
            </form>
        </div>
    </div>
    <div class="row" id="table-metadata">
        <g:set var="start" value="${((params.getInt('offset')?:0) + 1)}"/>
        <g:set var="end" value="${(start + ((params.getInt('max')?:10) - 1))}"/>
        <g:set var="end" value="${(end > fieldsMax ? fieldsMax : end)}"/>
        <div class="col-md-5" id="pagination-details">
            <g:message code="downloads.fields.showing.results" args="[start, end, fieldsMax]" />
            <g:if test="${params.filter}">
                (filter: ${params.filter} <g:link class="btn btn-default btn-xs ${downloads.btnState(fld:'filter', val:'')}" action="fields" params="${params + [filter:'']}" title="clear">&times;</g:link>)
            </g:if>
        </div>
        <div class="col-md-7" id="sort-widgets">
            <g:message code="downloads.fields.items.per.page" default="items per page"/>:
            <select id="per-page" name="per-page" class="input-small" onchange="location = this.value;">
                <option value="${g.createLink(action:'fields',params:params + [max:'10'])}" ${params.max == '10' ? 'selected': ''}>10</option>
                <option value="${g.createLink(action:'fields',params:params + [max:'20'])}" ${params.max == '20' ? 'selected': ''}>20</option>
                <option value="${g.createLink(action:'fields',params:params + [max:'50'])}" ${params.max == '50' ? 'selected': ''}>50</option>
                <option value="${g.createLink(action:'fields',params:params + [max:'100'])}" ${params.max == '100' ? 'selected': ''}>100</option>
            </select>&nbsp;

            <g:message code="downloads.fields.sort" default="sort"/>:
            <select id="sort" name="sort" class="input-small" onchange="location = this.value;">
                <option value="${g.createLink(action:'fields',params:params + [sort:'name'])}" ${params.sort == 'name' ? 'selected': ''}><g:message code="downloads.fields.search.name" default="Search term"/></option>
                <option value="${g.createLink(action:'fields',params:params + [sort:'downloadName'])}" ${params.sort == 'downloadName' ? 'selected': ''}><g:message code="downloads.fields.download.name" default="Download term"/></option>
                <option value="${g.createLink(action:'fields',params:params + [sort:'dwcTerm'])}" ${params.sort == 'dwcTerm' ? 'selected': ''}><g:message code="downloads.fields.dwc.term" default="DwC term"/></option>
                <option value="${g.createLink(action:'fields',params:params + [sort:'classs'])}" ${params.sort == 'classs' ? 'selected': ''}><g:message code="downloads.fields.dwc.class" default="DwC class"/></option>
                <option value="${g.createLink(action:'fields',params:params + [sort:'dataType'])}" ${params.sort == 'dataType' ? 'selected': ''}><g:message code="downloads.fields.dwc.dataType" default="Data type"/></option>
            </select>&nbsp;
            <g:message code="downloads.fields.order" default="order"/>:
            <select id="dir" name="dir" class="input-small" onchange="location = this.value;">
                <option value="${g.createLink(action:'fields',params:params + [order:'ASC'])}" ${params.order == 'ASC' ? 'selected': ''}><g:message code="downloads.fields.ascending" default="Ascending"/></option>
                <option value="${g.createLink(action:'fields',params:params + [order:'DESC'])}" ${params.order == 'DESC' ? 'selected': ''}><g:message code="downloads.fields.descending" default="Descending"/></option>
            </select>
        </div>
    </div>
    <div class="fwtable">
        <table id="fieldsTable" class="table table-bordered table-striped table-responsive">
            <thead>
                <tr>
                    <th><g:message code="downloads.fields.search.name" default="Search name"/></th>
                    <th><g:message code="downloads.fields.jsonName.name" default="Search results term"/></th>
                    <th><g:message code="downloads.fields.download.name" default="Download name"/></th>
                    <th><g:message code="downloads.fields.dwc.term" default="DwC term"/></th>
                    <th><g:message code="downloads.fields.dwc.class" default="DwC class"/></th>
                    <th><g:message code="downloads.fields.description" default="Description"/></th>
                    <th><g:message code="downloads.fields.download.description" default="Download description"/></th>
                    <th><g:message code="downloads.fields.furtherinformation" default="Further information"/></th>
                    <th><g:message code="downloads.fields.attributes" default="Attributes"/></th>
                </tr>
            </thead>
            <tbody>
            <g:each in="${fields}" var="fld">
                <tr>
                    <td>${fld.name}</td>
                    <td>${fld.jsonName}</td>
                    <td>${fld.downloadName}</td>
                    <td>${downloads.formatDwcTerm(field:fld)}</td>
                    <td>${fld.classs}</td>
                    <td>${fld.description}</td>
                    <td>${fld.downloadDescription}</td>
                    <td>
                    <g:if test="${fld.infoUrl}">
                        <a href="${fld.infoUrl}" target="_blank">Wiki</a>
                    </g:if>
                    </td>
                    <td>
                        <g:if test="${fld.indexed}"><span class="label label-info tooltips" title="${message(code:"downloads.fields.tooltip.indexed")}">I</span></g:if>
                        <g:if test="${fld.stored}"><span class="label label-success tooltips" title="${message(code:"downloads.fields.tooltip.stored")}">S</span></g:if>
                        <g:if test="${fld.multivalued}"><span class="label label-inverse tooltips" title="${message(code:"downloads.fields.tooltip.multivalued")}">M</span></g:if>
                        <g:if test="${fld.i18nValues}"><span class="label label-important tooltips" title="${message(code:"downloads.fields.tooltip.i18nValues")}">i18n</span></g:if>
                        <span class="label tooltips" title="${message(code:"downloads.fields.tooltip.datatype")}">${fld.dataType}</span>
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </div>
    <div class="pagination">
        <g:paginate total="${fieldsMax}" params="${[filter:params.filter]}"/>
    </div>

<g:javascript>
    $( document ).ready(function() {
        $('.tooltips').tooltip({delay: { show: 100, hide: 2500 }, html:true});

        $(document).on('show.bs.tooltip', function (e) {
            // hide any lingering tooltips
            $(".tooltip.in").removeClass('in');
        });
    });
</g:javascript>
</body>
</html>
