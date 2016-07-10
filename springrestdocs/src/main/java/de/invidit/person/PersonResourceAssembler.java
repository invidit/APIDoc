package de.invidit.person;

import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

/**
 * @author Michael Weber
 * @since 10.07.2016
 */
@Component
public class PersonResourceAssembler extends ResourceAssemblerSupport<Person, PersonResource> {
    public PersonResourceAssembler() {
        super(PersonController.class, PersonResource.class);
    }

    @Override
    public PersonResource toResource(Person person) {
        PersonResource resource = instantiateResource(person);
        resource.setPerson(person);

        resource.add(linkTo(methodOn(PersonController.class).getPerson(person.getId())).withSelfRel());
        return resource;
    }
}
