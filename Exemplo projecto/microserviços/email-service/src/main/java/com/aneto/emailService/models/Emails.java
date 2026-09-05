package com.aneto.emailService.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_emails", schema = "auditoria")
@Data
@Builder // <--- Garante que esta anotação está aqui
@NoArgsConstructor
@AllArgsConstructor
public class Emails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email_from", columnDefinition = "TEXT")
    private String from;

    @Column(name = "email_to", columnDefinition = "TEXT")
    private String to;

    @Column(columnDefinition = "TEXT")
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Builder.Default
    private LocalDateTime sentAt = LocalDateTime.now();
}