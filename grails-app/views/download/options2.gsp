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
<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
    <meta name="fluidLayout" content="false"/>
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

</style>
</head>
<body>
<div class="row" id="customiseDownload">
    <div class="col-md-10 col-md-offset-1">
        <h2 class="heading-medium"><g:message code="download.customize.title"/></h2>
        <g:set var="toolbar">
            <div class="button-toolbar row-fluid">
                <div class="btn-group">
                    <a class="btn btn-default select-all-btn" href="#"><i class="fa fa-check"></i> <span class="hidden-xs"><g:message code="download.customize.select.all"/></span></a>
                    <a class="btn btn-default select-none-btn" href="#"><i class="fa fa-times"></i> <span class="hidden-xs"><g:message code="download.customize.unselect.all"/></span></a>
                </div>
                <div class="btn-group pull-right">
                    <a class="btn btn-default save-btn"><i class="fa fa-cog"></i> <span class="hidden-xs"><g:message code="download.customize.save.preferences"/></span></a>
                    <a class="btn btn-primary next-btn" href="#"><span class="hidden-xs"><g:message code="download.customize.next"/></span> <i class="fa fa-chevron-right color--white"></i></a>
                </div>
            </div>
        </g:set>
        <g:set var="selectedItem" value="fa-toggle-on"/>
        <g:set var="unselectedItem" value="fa-toggle-off"/>

        ${raw(toolbar)}

        <div class="well">
            <g:each in="${customSections}" var="section" status="s">
                <div class="row ${(s > 0) ? "margin-top-1" : ""}">
                    <div class="col-md-12">
                            <div class="comment-wrapper push">
                                <div class="row">
                                    <div class="col-md-2 hidden-xs">
                                        <g:if test="${s == 0}">
                                            <div class="contrib-stats">
                                                <div class="no-of-questions">
                                                    <div class="survey-details">
                                                        <div class="survey-counter"><strong><i class="fa fa-cog color--yellow"></i></strong></div>
                                                    </div>
                                                </div>
                                            </div>
                                        </g:if>
                                    </div>
                                    <div class="col-md-8">
                                        <h4 class="text-uppercase"><g:message code="section.${section.key}" default="${section.key}"/></h4>
                                        <p class="margin-bottom-2 hide">
                                            <g:message code="section.description.${section.key}" default="A description of the ${section.key}"/>.
                                        </p>

                                        <div class="list-group">
                                            <g:each in="${section.value}" var="group" status="i">
                                                <g:set var="disabled" value="${(mandatoryFields.contains(group) || (section.key == 'SPATIAL INTERSECTION' && downloadParams?.layers)) ? true : false }"/>
                                                <g:set var="active" value="${(mandatoryFields.contains(group) || userSavedFields.contains(group)) ? true : false }"/>
                                                <a href="#" class="list-group-item ${(disabled)?"disabled":""} ${(active)?"list-group-item-success":""}" title="${(disabled)?message(code: "download.customize.required.item.title", default: ""):""}">
                                                    <div class="checkbox pull-left">
                                                        <label><input type="checkbox" class="fieldClass" value="${group}" ${(disabled || active)?"checked='checked'":""}></label>
                                                    </div>
                                                    <div class="pull-left form-control-inline">
                                                        <h4 class="padding-left-1">
                                                            <i class="fa  ${(disabled || active)? selectedItem : unselectedItem}"></i>
                                                            <g:message code="customGroup.${group}" default="${group}"/>
                                                            <i class="fa fa-question-circle tooltips" data-group="${group}"
                                                                   data-content="<downloads:groupHelpHtml fields="${dwcClassesAndTerms.get(group)?.join(', ')}" filter="${groupingsFilterMap.get(group)}" group="${group}"/>"></i>
                                                        </h4>
                                                    </div>
                                                    <div class="clearfix"></div>
                                                </a>
                                            </g:each>
                                        </div><!-- /.list-group -->
                                    </div><!-- /.col-md-8 -->
                                </div>
                            </div><!-- End comment-wrapper push -->
                        %{--</div>--}%
                    </div>
                </div>
            </g:each>
        </div>

        ${raw(toolbar)}

    </div>
</div>
<g:javascript>
    $( document ).ready(function() {
        $('.list-group-item.disabled').click(function(e) {
            e.preventDefault(); // prevent page jump
        }).tooltip({placement: 'bottom'});

        // catch clicks on list group items
        $('a.list-group-item').not('.disabled').click(function(e) {
            e.preventDefault(); // its link so stop any regular link stuff hapenning
            var link = this;
            if ($(link).hasClass('list-group-item-success')) {
                // already selected
                deselectItem(link);
            } else {
                // not selected
                selectItem(link);
            }
        });

        $('.select-all-btn').click(function(e) {
            e.preventDefault();
            $('a.list-group-item').not('.disabled').each(function(i) {
                selectItem(this);
            });
        });

        $('.select-none-btn').click(function(e) {
            e.preventDefault();
            $('a.list-group-item').not('.disabled').each(function(i) {
                deselectItem(this);
            });
        });

        $('.next-btn').click(function(e) {
            e.preventDefault();
            var queryString = "${downloadParams?.queryString()}";
            var fields = [];
            $('.fieldClass:checked').each(function(i) {
                fields.push($(this).val());
            });
            queryString += '&customClasses=' +fields.join('&customClasses=');
            window.location = "${g.createLink(action:'options2')}" + queryString;
        });

        $('.save-btn').click(function(e) {
            e.preventDefault();
            var fields = [];
            $('.fieldClass:checked').each(function(i) {
                fields.push($(this).val());
            });

            $.cookie("download_fields", fields.join(","));
            <g:if test="${grailsApplication.config.userdetails.baseUrl}">
                $.post("${g.createLink(action: 'saveUserPrefs')}?fields=" + fields.join("&fields="),
                                { },
                                function(data) {
                                    bootbox.alert("<g:message code="download.customize.preferences.saved"/>");
                                }
                        ).error(function (request, status, error) {
                                    bootbox.alert("<g:message code="download.customize.preferences.failed"/>");
                                });
            </g:if>
            <g:else>
                bootbox.alert("Preferences saved.");
            </g:else>
        });

        // add BS tooltips trigger
        $(".tooltips").popover({
            trigger: 'hover',
            placement: 'top',
            delay: { show: 100, hide: 2500 },
            html: true,
            container: 'body'
        });
        //hide previous tooltip if new tooltip is triggered
        $(document).on('show.bs.popover', function (e) {
            // hide any lingering tooltips
            $(".popover.in").removeClass('in');
        });
    });

    function selectItem(item) {
        $(item).find('i.fa').not('.tooltips').removeClass('${unselectedItem}');
        $(item).find('i.fa').not('.tooltips').addClass('${selectedItem}');
        $(item).addClass('list-group-item-success');
        $(item).find('input').attr('checked','checked');
        $(item).blur(); // prevent BS focus
    }

    function deselectItem(item) {
        $(item).removeClass('disabled').removeClass('list-group-item-success');
        $(item).find('.fa').not('.tooltips').addClass('${unselectedItem}');
        $(item).find('.fa').not('.tooltips').removeClass('${selectedItem}');
        $(item).find('input').removeAttr('checked');
        $(item).blur(); // prevent BS focus
    }

</g:javascript>
</body>
</html>