package cms.components;

import java.net.URI;
import java.nio.charset.StandardCharsets;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CognitoLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {

  private CognitoProperties cognitoProperties;

  public CognitoLogoutSuccessHandler(CognitoProperties properties) 
  {
	  cognitoProperties = properties;
  }

  @Override
  protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response,
                                      Authentication authentication) {

    UriComponents logoutUri = UriComponentsBuilder
      .fromUriString(UrlUtils.buildFullRequestUrl(request))
      .replacePath(request.getContextPath())  //Override the current path.
      .replaceQuery(null)
      .fragment(null)
      .build();

    return UriComponentsBuilder
      .fromUri(URI.create(cognitoProperties.getEndSessionEndpoint()))
      .queryParam("client_id", cognitoProperties.getClientId())
      .queryParam("logout_uri", logoutUri)
      .encode(StandardCharsets.UTF_8)
      .build()
      .toUriString();
  }
}