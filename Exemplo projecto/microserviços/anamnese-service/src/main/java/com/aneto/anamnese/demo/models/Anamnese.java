package com.aneto.anamnese.demo.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class Anamnese {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String pacienteNome;

    @Column(columnDefinition = "TEXT")
    private String conteudoExtraido;

    private String nomeArquivoOriginal;

    private LocalDateTime dataCriacao = LocalDateTime.now();
}