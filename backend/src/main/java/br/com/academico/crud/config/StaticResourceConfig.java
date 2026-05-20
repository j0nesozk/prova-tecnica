package br.com.academico.crud.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    private final UploadProperties props;

    public StaticResourceConfig(UploadProperties props) {
        this.props = props;
    }

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        Path dir = Paths.get(props.dir()).toAbsolutePath().normalize();
        registry.addResourceHandler(props.publicBase() + "/**")
                .addResourceLocations("file:" + dir + "/")
                .setCachePeriod(3600);
    }
}
