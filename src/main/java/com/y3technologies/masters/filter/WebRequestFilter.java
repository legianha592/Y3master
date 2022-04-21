package com.y3technologies.masters.filter;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.y3technologies.masters.client.AasClient;
import com.y3technologies.masters.dto.aas.SessionUserInfoDTO;
import com.y3technologies.masters.exception.FeignException;

@Component
@Order(1)
public class WebRequestFilter extends OncePerRequestFilter {

    @Autowired
    AasClient aasClient;

    @Autowired
    private MessageSource messageSource;

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authToken = request.getHeader(SecurityConstant.TOKEN_HEADER);
        if (authToken == null) {
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
            return;
        }
        authToken = authToken.replace(SecurityConstant.TOKEN_PREFIX, "");
        try {
            SecurityContextHolder.clearContext();
            SessionUserInfoDTO sessionUserInfoDTO = aasClient.getSessionInfo(authToken);
            RequestContextHolder.currentRequestAttributes().setAttribute("SESSION_INFO", sessionUserInfoDTO, RequestAttributes.SCOPE_REQUEST);
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(new UsernamePasswordAuthenticationToken(sessionUserInfoDTO.getAasUserId(),
                    authToken, new ArrayList<>()));
            SecurityContextHolder.setContext(context);
            filterChain.doFilter(request,response);
        } catch(FeignException e){
            String message = e.getMessages().get(0);
            String errorMessage = messageSource.getMessage("exception.session.activeSession.invalid", null, Locale.getDefault());
            if(errorMessage.equalsIgnoreCase(message)){
                response.sendError(HttpServletResponse.SC_FORBIDDEN, errorMessage);
            }
        }
    }
}
