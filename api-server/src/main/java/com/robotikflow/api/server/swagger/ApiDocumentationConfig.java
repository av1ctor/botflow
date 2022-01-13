package com.robotikflow.api.server.swagger;

import io.swagger.annotations.Contact;
import io.swagger.annotations.Info;
import io.swagger.annotations.License;
import io.swagger.annotations.SwaggerDefinition;

@SwaggerDefinition(
        info = @Info(
                description = "Robotikflow API",
                version = "V1.0",
                title = "Robotikflow API",
                contact = @Contact(
                   name = "API Help Desk", 
                   email = "api-hdesk@botflow.com", 
                   url = "https://botflow.com"
                ),
                license = @License(
                   name = "Apache 2.0", 
                   url = "http://www.apache.org/licenses/LICENSE-2.0"
                )
        ),
        consumes = {"application/json", "application/xml"},
        produces = {"application/json", "application/xml"},
        schemes = {SwaggerDefinition.Scheme.HTTPS}
)
public class ApiDocumentationConfig 
{

}
