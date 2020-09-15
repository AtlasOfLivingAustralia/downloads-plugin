package downloads.plugin

class BootStrap {
    def messageSource
    def grailsApplication

    def init = { servletContext ->
        messageSource.setBasenames(
                "file:///var/opt/atlas/i18n/downloads-plugin/messages",
                "file:///opt/atlas/i18n/downloads-plugin/messages",
                "WEB-INF/grails-app/i18n/messages",
                "classpath:messages",
                "${grailsApplication.config.getRequiredProperty("biocache.baseUrl")}/facets/i18n"
        )
    }
    def destroy = {
    }
}
