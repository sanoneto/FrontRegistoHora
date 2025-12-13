package com.aneto.emailservice.repository;

import com.aneto.emailservice.models.Emails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailRepository extends JpaRepository<Emails, Long> {
}
