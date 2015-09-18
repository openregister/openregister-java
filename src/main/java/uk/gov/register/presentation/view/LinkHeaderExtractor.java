package uk.gov.register.presentation.view;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.util.stream.Stream;

class LinkHeaderExtractor{
    private final HttpServletResponse httpServletResponse;

    public LinkHeaderExtractor(HttpServletResponse httpServletResponse) {
        this.httpServletResponse = httpServletResponse;
    }

    String extractLink(String linkRel) {
        String linkHeader = httpServletResponse.getHeader("Link");

        if (StringUtils.isEmpty(linkHeader)) {
            return null;
        }

        return Stream.of(linkHeader.split(","))
                .filter(h -> h.trim().contains("rel=\"" + linkRel + "\""))
                .map(h -> h.trim().replaceAll("<([^>]+).*", "$1"))
                .findFirst()
                .orElse(null);
    }
}
