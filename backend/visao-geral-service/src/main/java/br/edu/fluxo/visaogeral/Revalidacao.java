package br.edu.fluxo.visaogeral;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

@Configuration
public class Revalidacao {

    // GET da API leva ETag; sem mudança, o navegador recebe 304 sem o corpo
    @Bean
    public FilterRegistrationBean<ShallowEtagHeaderFilter> etag() {
        var registro = new FilterRegistrationBean<>(new ShallowEtagHeaderFilter());
        registro.addUrlPatterns("/api/*");
        return registro;
    }
}
