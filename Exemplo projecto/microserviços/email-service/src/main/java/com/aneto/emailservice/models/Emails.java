package com.aneto.emailservice.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;

@Builder
@Entity
@Table(name = "TB_EMAILS", schema = "AUDITORIA")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
public class Emails {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
       private Long id;
        String name;
        String emailOrigin;
        String resetLink;
        @CreatedDate
        LocalDate createdDate;

    public Emails(String name, String emailOrigin, String resetLink) {
        // Chama o construtor canônico (o construtor de 5 argumentos)
        // Passando 'null' para o 'id', indicando que será gerado pelo BD
        this(null, name, emailOrigin, resetLink, null);
    }
}
