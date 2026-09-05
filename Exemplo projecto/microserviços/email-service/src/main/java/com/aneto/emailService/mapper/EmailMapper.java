package com.aneto.emailService.mapper;

import com.aneto.emailService.dto.request.EmailRequest;
import com.aneto.emailService.models.Emails;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

// unmappedTargetPolicy = ReportingPolicy.IGNORE resolve os warnings de campos extras
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EmailMapper {

    /**
     * Converte o Request para a Entidade.
     * O ID é gerado pelo banco, por isso ignoramos.
     */
    @Mapping(target = "id", ignore = true)
    Emails toEntity(EmailRequest request);

    /**
     * Converte a Entidade para o Request (Response).
     */
    EmailRequest toResponse(Emails emails);
}