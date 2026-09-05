package com.aneto.emailService.repository;

import com.aneto.emailService.models.Emails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailRepository extends JpaRepository<Emails, Long> {
}
