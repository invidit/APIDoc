package de.invidit.person;

import lombok.Getter;
import lombok.Setter;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.core.Relation;

/**
 * @author Michael Weber
 * @since 10.07.2016
 */
@Relation(value = "person", collectionRelation = "persons")
public class PersonResource extends ResourceSupport {
    @Getter @Setter
    private Person person;
}
