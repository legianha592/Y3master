package com.y3technologies.masters.util;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;
import java.util.ResourceBundle;

public class MessagesUtilities implements MessageSourceAware {

    private static Locale defaultLocale = Locale.ENGLISH;
    private final static Logger log = LoggerFactory.getLogger(ExcelUtils.class);

    private MessageSource messageSource;

    private static ResourceBundle getResourceBundle(Locale locale) {
        return ResourceBundle.getBundle("messages", locale);
    }

    public MessageSource getMessageSource() {
        return messageSource;
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String getMessageWithParam(String messageKey, Object[] params) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (!ObjectUtils.isEmpty(requestAttributes)){
            HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
            String acceptlanguage = request.getHeader("Accept-Language");

            if (!StringUtils.isEmpty(acceptlanguage)) {
                defaultLocale = new Locale(acceptlanguage);
            }
        }

        String value = null;
        if (messageKey != null) {
            try {
                value = messageSource.getMessage(messageKey, params, defaultLocale);
            }catch (Exception e){
                log.error(String.valueOf(e));
            }

            if(value == null) {
                value = getResourceBundle(defaultLocale).getString(messageKey);
            }
        }
        if (value == null) {
            return messageKey;
        }

        return value;
    }

    public String getResourceMessage(String messageKey, Locale locale) {
        String value = messageKey;
        if (messageKey != null) {
            try {
                value = messageSource.getMessage(messageKey, null, locale);
            } catch (Exception e){
                log.error(String.valueOf(e));
            }
            if (value == null) {
                value = getResourceBundle(locale).getString(messageKey);
            }
        }

        if (value == null) {
            return messageKey;
        }

        return value;
    }

}
