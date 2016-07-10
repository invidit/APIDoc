package de.invidit.person;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Michael Weber
 * @since 10.07.2016
 */
@Repository
public interface PersonRepository extends JpaRepository<Person, Integer>{}
