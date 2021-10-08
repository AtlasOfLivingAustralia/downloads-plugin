%{--
  - Copyright (C) 2016 Atlas of Living Australia
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
  User: dos009@csiro.au
  Date: 22/02/2016
  Time: 1:53 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page import="grails.converters.JSON" contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
    <meta name="fluidLayout" content="false"/>
    <meta name="breadcrumbParent" content="${request.contextPath ?: '/'},${message(code: "download.occurrence.records")}"/>
    <meta name="breadcrumb" content="${message(code: "download.breadcumb.title")}"/>
    <title><g:message code="download.page.title"/></title>

    <asset:javascript src="downloads.js" />
    <asset:stylesheet src="downloads.css" />

    <style type="text/css">
        a h4 > .fa {
            width: 18px;
            margin-right: 5px;
            color:#9A9A9A;
        }
        a h4 {
            font-size: 16px;
        }
        .list-group-item {
            padding:5px 0px;
        }
        .margin-top-1 {
            margin-top: 2em;
        }
        textarea {
            width: 100%;
        }
        .progress {
            height: 12px;
            margin-bottom: 10px;
        }
        #mydownloads {
            text-align: center;
        }
    </style>
</head>
<body>
<div class="row">
    <div class="col-md-4 col-md-offset-4">
        <div class="well">
            <div class="row">
                <div class="col-md-12">
                    <div class="logo-brand">
                        <div class="font-awesome-icon-large">
                            <i class="fa fa-check-circle color--apple"></i>
                        </div>
                        <h2 class="heading-medium-large"> <g:message code="download.confirm.thanks" default="Thank you for your download"/></h2>
                        <p class="lead">
                            <g:if test="${isQueuedDownload && json}">
                                <g:message code="download.confirm.queued" default="Your download is now being queued"/>
                            </g:if>
                            <g:elseif test="${isFieldGuide && (downloadUrl || json)}">
                                <g:if test="${json}">
                                    <g:message code="download.confirm.queue" default="Your download is now being queued"/>
                                </g:if>
                                <g:else>
                                    <g:message code="download.confirm.ready" default="Your field guide is ready ${json}"/>
                                </g:else>
                            </g:elseif>
                            <g:else>
                                <g:message code="download.confirm.started" default="Your download has completed"/>
                            </g:else>
                        </p>
                        <code class="collapse">
                            isQueuedDownload = ${isQueuedDownload}<br>
                            isFieldGuide = ${isFieldGuide}<br>
                            downloadUrl = ${downloadUrl}<br>
                            json = ${json}<br>
                        </code>
                        <p>
                            <g:if test="${(isQueuedDownload || isFieldGuide) && json}">
                                <div class="progress active">
                                    <div class="progress-bar progress-bar-striped progress-bar-animated" style="width: 100%;"></div>
                                </div>
                                <div id="queueStatus"></div>
                                <p>&nbsp;</p>
                                <g:message code="download.confirm.emailed" default="An email containing a link to the download file will be sent to your email address (linked to your ALA account) when it is completed."/>
                            </g:if>
                            <g:elseif test="${isFieldGuide && downloadUrl}">
                                <button id="fieldguideBtn" class="btn btn-lg btn-success btn-block"><g:message code="download.confirm.newWindow" default="View the field guide (new window)"/></button>
                            </g:elseif>
                            <g:elseif test="${isChecklist && downloadUrl}">
                                <g:message code="download.confirm.browser" default="Check your downloads folder or your browser's downloads window."/>
                            </g:elseif>
                            <g:else>
                                <g:message code="download.confirm.completed" default="The download has already been run. Click the button below to start over."/>
                            </g:else>
                        </p>
                        <p>&nbsp;</p>
                    </div>
                    <a href="${downloadParams.targetUri}${downloadParams.searchParams}" class="btn btn-primary btn-block margin-bottom-1 font-xxsmall"
                           type="button"><g:message code="download.confirm.returnToSearch" default="Return to search results"/></a>
                    <g:if test="${isQueuedDownload && json}">
                        <button class="btn btn-link btn-block margin-bottom-1" data-toggle="modal" data-target="#downloadUrlModal"><g:message code="download.confirm.rawUrlBtn" default="View the raw download URL"/></button>
                    </g:if>
                </div>
            </div>
        </div>
        <div id="mydownloads">
            <a href="${grailsApplication?.config?.doiService?.baseUrl}/myDownloads" target="_blank"><g:message
                    code="download.confirm.myDownloadsLink"
                    default="My Downloads - View a list of all your previous downloads"/></a>
        </div>
    </div>
</div>
<!-- Copy download URL modal -->
<div class="modal fade" id="downloadUrlModal" tabindex="-1" role="dialog">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title"><g:message code="download.downloadUrl.title" default="Download URL"/> </h4>
            </div>
            <div class="modal-body">
                <textarea id='requestUrl'>${json?.requestUrl}</textarea>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal"><g:message code="modal.close" default="Close"/></button>
                <button id="copyBtn" class="btn btn-primary" data-clipboard-action="copy" data-clipboard-target="#requestUrl">Copy to clipboard</button>
            </div>
        </div><!-- /.modal-content -->
    </div><!-- /.modal-dialog -->
</div><!-- /.modal -->
<g:javascript>
    $( document ).ready(function() {
        // raw download URL popup
        new Clipboard('#copyBtn');

        $('#fieldguideBtn').click(function(e) {
            e.preventDefault();
            var url = "${downloadUrl}";
            window.open(url);
        });

        var isChecklist  = "${isChecklist}";
        var downloadUrl  = "${downloadUrl}";
        if (isChecklist == "true" && downloadUrl) {
            window.location.href = downloadUrl;
        }

    <g:if test="${json}">
        <g:applyCodec encodeAs="none">
            // Update status of offline download
            var jsonResponse = ${json as JSON};

            if (jsonResponse) {
                updateStatus(jsonResponse);
            }
        </g:applyCodec>
    </g:if>
    <g:if test="${json && downloadParams?.reasonTypeId && grailsApplication.config.getProperty('downloads.gaCustomData')}">
        <g:set var="downloadReasonCode" value="${downloads.getLoggerReasonString(id: downloadParams.reasonTypeId)}"/>
        ga('set', 'dimension2', '${downloadParams.downloadType}');
        ga('set', 'dimension3', '${downloadReasonCode}');
    </g:if>
    });

    /**
     * Check offline download statusUrl and update UI, recursively
     *
     * @param json
     */

    // Update status periodically, starting at 1 sec,
    // doubling at each call with a limit to maxTimeout
    var maxTimeout = ${grailsApplication.config.downloads.refresh.confirm.maxTimeout} * 1000;
    var timeout = 1 * 1000;

    function updateStatus(json) {
        if (json.status == "inQueue" && json.statusUrl) {
            $('.lead').html("<g:message code="download.confirm.queue"/>");
            $('.progress').show();
            reloadStatus(json.statusUrl);
        }
        else if (json.status == "running" && json.statusUrl) {
            $('.lead').html("<g:message code="download.confirm.running"/>");
            $('.progress').show();
            reloadStatus(json.statusUrl);
        }
        else if (json.status == "skipped") {
            $('.lead').html("<g:message code="download.confirm.skipped"/>");
            $('.progress').hide();
        }
        else if (json.status == "finished") {
            $('.lead').html("<g:message code="download.confirm.finished"/>");
            $('#queueStatus').html("<a class='btn btn-primary' href='" + json.downloadUrl + "'><i class='fa fa-download'></i> <g:message code="download.confirm.download.now"/></a>");
            $('.progress').hide();
        }
        else {
            // Error cases
            $('.lead').html("<g:message code="download.confirm.failed"/>");
            $('.progress').hide();
            var errorMessage = "";
            if (json.status) {
                errorMessage = "status: <code>" + json.status + "</code><br/>";
            }
            if (json.message) {
                errorMessage = errorMessage + "message: <code>" + json.message + "</code>";
            }
            $('#queueStatus').html(errorMessage);
        }
    }

    function reloadStatus(url) {
        setTimeout(function(){
            $.getJSON(url, function(data) {
                timeout = Math.min(timeout * 2, maxTimeout);
                updateStatus(data);
            }).fail(function( jqxhr, textStatus, error ) {
                $('.lead').html("<g:message code="download.confirm.failed"/>");
                $('.progress').hide();
                $('#queueStatus').html("status: <code>"+textStatus+"</code><br/>message: <code>"+error+"</code>");
            });
        }, timeout);
    }

</g:javascript>
</body>
</html>