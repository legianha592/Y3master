package com.y3technologies.masters;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import com.y3technologies.masters.constants.AppConstants;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.data.jpa.datatables.repository.DataTablesRepositoryFactoryBean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import com.y3technologies.masters.util.EncryptUtils;
import com.y3technologies.masters.util.FeignConfig;
import com.y3technologies.masters.util.MessagesUtilities;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@EnableJpaRepositories(repositoryFactoryBeanClass = DataTablesRepositoryFactoryBean.class)
@EnableFeignClients(defaultConfiguration = { FeignConfig.class })
@EnableScheduling
public class MastersApplication {

	public static void main(String[] args) {
		SpringApplication.run(MastersApplication.class, args);
	}

	@Bean
	public LocaleResolver localeResolver() {
		AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver();
		localeResolver.setDefaultLocale(Locale.ENGLISH);
		return localeResolver;
	}

	@Bean
	public MessagesUtilities messagesUtilities() {
		return new MessagesUtilities();
	}
	
	@Bean
	public MessageSource messageSource() {
		ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
		messageSource.setBasenames("classpath:ValidationMessages", "classpath:messages");
		messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());
//		messageSource.setUseCodeAsDefaultMessage(true);
		messageSource.setCacheSeconds(5);
		return messageSource;
	}

	@Bean
	public ModelMapper modelMapper() {

		ModelMapper modelMapper = new ModelMapper();
		modelMapper.getConfiguration().setFieldMatchingEnabled(true);
		modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

		return modelMapper;
	}

	@Bean
	public ThreadPoolTaskExecutor taskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(AppConstants.MastersConstants.CORE_POOL_SIZE);
		executor.setMaxPoolSize(AppConstants.MastersConstants.MAX_POOL_SIZE);
		executor.setQueueCapacity(AppConstants.MastersConstants.QUEUE_CAPACITY);
		executor.setThreadNamePrefix(AppConstants.MastersConstants.THREAD_NAME_PREFIX);
		executor.initialize();
		return executor;
	}

  @Bean
  public EncryptUtils encryptUtils() {
    return new EncryptUtils();
  }
}
