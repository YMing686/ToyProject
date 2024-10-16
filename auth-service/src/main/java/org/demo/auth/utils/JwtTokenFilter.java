package org.demo.auth.utils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.demo.auth.service.AccountService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

  private final JwtTokenProvider jwtTokenProvider;

  private final AccountService accountService;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    // Extract authorization token
    String authHeader = request.getHeader("Authorization");

    final String jwt;
    final String username;

    // Check Whether jwt token is missing
    if (authHeader == null || !authHeader.startsWith("Bearer")) {
      // No bearer token -> do nothing, call next filter
      filterChain.doFilter(request, response);
      return;
    }

    // Extract user information
    jwt = authHeader.substring(7);

    // if token is invalid or expired, return 401
    try {
      if (!this.jwtTokenProvider.validateToken(jwt)) {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid access token");
        return;
      }
    } catch (Exception e) {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Access token expired");
      return;
    }

    try {
      username = this.jwtTokenProvider.extractUsername(jwt);
    } catch (Exception e) {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid access token");
      return;
    }
    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
      UserDetails userDetails = this.accountService.loadUserByUsername(username);
      UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
          userDetails, null, userDetails.getAuthorities());
      authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }
    filterChain.doFilter(request, response);
  }
}
