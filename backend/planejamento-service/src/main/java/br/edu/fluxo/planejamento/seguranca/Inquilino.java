package br.edu.fluxo.planejamento.seguranca;

import java.util.Map;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;

// Cada consulta do Hibernate fica restrita aos dados do usuário da requisição
@Component
public class Inquilino
    implements CurrentTenantIdentifierResolver<Long>, HibernatePropertiesCustomizer
{

    @Override
    public Long resolveCurrentTenantIdentifier() {
        Long id = UsuarioAtual.id();
        return id == null ? 0L : id;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return false;
    }

    @Override
    public void customize(Map<String, Object> propriedades) {
        propriedades.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, this);
    }
}
