package com.y3technologies.masters.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;



@Configuration
//@EnableSwagger2
public class Swagger {
	/*@Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.y3technologies.masters.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("TRX-MASTERS")
                .description("")
                .termsOfServiceUrl("")
                .version("1.0")
                .build();
    }*/

    @Bean
    public OpenAPI customOpenAPI() {

        Info info = new Info().title("Masters API").version("1.0").description("Masters API DOCUMENTATION")
                .license(new License().name("Apache 2.0").url("http://springdoc.org"));

        return new OpenAPI()
                .components(new Components())
                .info(info);
    }
}
