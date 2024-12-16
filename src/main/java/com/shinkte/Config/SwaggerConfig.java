package com.shinkte.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;


/**
 * @author shinkte
 * @Description: Knife4j 配置接口文档
 */
@Configuration
@EnableSwagger2WebMvc
@Profile({"dev", "test"})   //版本控制访问


//配置Swagger接口文档
public class SwaggerConfig {

    @Bean(value = "defaultApi2")
    /**
     * @return
     */
    public Docket defaultApi2() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                // 这里一定要标注你控制器的位置
                .apis(RequestHandlerSelectors.basePackage("com.shinkte.controller"))
                .paths(PathSelectors
                        .any() //满足条件的路径
                        // 不满足条件的路径 .none()
                )
                .build();
        }
        /**
         * 该方法定义了API的基本信息，包括标题，描述，服务条款URL，联系人和版本
         * @return
         */
        private ApiInfo apiInfo() {
            return new ApiInfoBuilder()
                    .title("伙伴匹配用户中心")
                    .description("伙伴匹配系统接口文档")
                    .termsOfServiceUrl("https://github.com/liyupi")
                    .contact(new Contact("shinkte","https://shayuyu.cn/","null"))
                    .version("1.0")
                    .build();
        }
    }

