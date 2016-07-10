package de.invidit.person;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

/**
 * @author Michael Weber
 * @since 10.07.2016
 */
@RestController
@RequestMapping(value = "/persons")
public class PersonController {
    private PersonRepository personRepository;
    private PersonResourceAssembler resourceAssembler;

    @Autowired
    public PersonController(PersonRepository personRepository, PersonResourceAssembler resourceAssembler) {
        this.personRepository = personRepository;
        this.resourceAssembler = resourceAssembler;
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    public HttpEntity<Resources<PersonResource>> getPersons() {
        List<Person> persons = this.personRepository.findAll();
        List<PersonResource> personResourceList = this.resourceAssembler.toResources(persons);
        Link linkToResources = linkTo(methodOn(PersonController.class).getPersons()).withSelfRel();
        Resources<PersonResource> personResources = new Resources<>(personResourceList);
        personResources.add(linkToResources);

        return ResponseEntity.ok(personResources);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public HttpEntity<PersonResource> getPerson(@PathVariable int id) {
        Person person = returnValidPersonOrThrowException(id);
        return ResponseEntity.ok(this.resourceAssembler.toResource(person));
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public HttpEntity<PersonResource> createPerson(@RequestBody Person person) {
        Person savedPerson = this.personRepository.save(person);
        return new ResponseEntity<>(this.resourceAssembler.toResource(savedPerson), HttpStatus.CREATED);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public HttpEntity<PersonResource> updatePerson(@PathVariable int id, @RequestBody Person person) {
        Person originalPerson = returnValidPersonOrThrowException(id);

        originalPerson.setName(person.getName());
        originalPerson.setFirstname(person.getFirstname());

        Person savedPerson = this.personRepository.save(originalPerson);
        return ResponseEntity.ok(this.resourceAssembler.toResource(savedPerson));
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public HttpEntity<PersonResource> deletePerson(@PathVariable int id) {
        returnValidPersonOrThrowException(id);

        this.personRepository.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }

    private Person returnValidPersonOrThrowException(int id) {
        Person person = this.personRepository.findOne(id);

        if (Objects.isNull(person)) {
            throw new PersonNotFoundException("Could not find person with id '" + id + "'.");
        }
        return person;
    }
}
