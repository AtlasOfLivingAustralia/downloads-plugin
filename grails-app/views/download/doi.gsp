<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
    <meta name="fluidLayout" content="false"/>
    <meta name="breadcrumbParent" content="${g.createLink(action:'myDownloads')},${message(code: "download.mydownloads.pageTitle")}"/>
    <meta name="breadcrumb" content="Download ${doi.doi}"/>
    <title><g:message code="download.doilanding.pageTitle"  args="[doi.doi]"/></title>
    <asset:javascript src="downloads.js" />
    <asset:stylesheet src="downloads.css" />
</head>
<body>
<div class="container">
<div class="row">
    <div class="col-md-12" align="center">
        <h3>
            <a href="${grailsApplication.config.doiService.doiResolverUrl}${doi.doi}" type="button" class="doi"><span>DOI</span><span>${doi.doi}</span></a>
        </h3>
    </div>
    <div class="col-md-12 text-right">
        <a href="${grailsApplication?.config.doiService.baseUrl}/doi/${URLEncoder.encode(doi.doi, 'UTF-8')}/download" class="btn btn-primary"><i class="glyphicon glyphicon-download-alt"></i>&nbsp; Download file</a>
    </div>
    <div class="col-md-12"><b>Record count:</b> ${doi?.applicationMetadata?.recordCount}</div>
    %{--<div class="col-md-12"><b>Title:</b> ${doi.title}</div>--}%
    %{--<div class="col-md-12"><b>Description:</b> ${doi.description}</div>--}%
    <div class="col-md-12"><b>Search URL:</b><a href="${doi.applicationMetadata?.searchUrl}"> ${URLDecoder.decode(doi.applicationMetadata?.searchUrl, 'UTF-8')}</a></div>
    <div class="col-md-12"><b>Search query:</b> <downloads:formatSearchQuery searchUrl="${doi.applicationMetadata?.searchUrl}" /> </div>
    <div class="col-md-12"><b>File:</b> <a href="${grailsApplication?.config.doiService.baseUrl}/doi/${URLEncoder.encode(doi.doi, 'UTF-8')}/download"> ${doi.filename}</a></div><br>
    <div class="col-md-12"><b>Licence:</b> ${doi.licence}</div>
    <div class="col-md-12"><b>Authors:</b> ${doi.authors}</div>
    <div class="col-md-12"><b>Date Created:</b> ${doi.dateCreated}</div>
    %{--<div class="col-md-12"><b>Date Minted:</b> ${doi.dateCreated}</div>--}%

</div>
    <div class="row">
        <div class="fwtable col-md-12">
            <h3>Datasets</h3>
            <table class="table table-bordered table-striped table-responsive">
                <thead>
                <tr>
                    <th>Name</th>
                    <th>Licence</th>
                    <th style="text-align: center">Count</th>
                </tr>
                </thead>
                <tbody>
                <g:each in="${doi.applicationMetadata?.datasets}" var="dataset">
                    <tr>
                        <td class="col-xs-4"><a href="${grailsApplication?.config.collections.baseUrl}/public/show/${dataset.uid}">${dataset.name}</a></td>
                        <td class="col-xs-3">${dataset.licence}</td>
                        <td class="col-xs-1" align="center">${dataset.count}</td>
                    </tr>
                </g:each>
                </tbody>
            </table>
        </div>

    </div>
</div>
</body>
</html>