package de.invidit.person;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.invidit.SpringRestDocApplication;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.halLinks;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Michael Weber
 * @since 10.07.2016
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SpringRestDocApplication.class)
@WebIntegrationTest(randomPort = true)
public class PersonControllerTest {

    @Rule
    public final RestDocumentation restDocumentation = new RestDocumentation("target/generated-snippets");

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    private RestDocumentationResultHandler document;

    private Person personOne;
    private Person personTwo;

    @Before
    public void setUp() {
        this.document = document("{methodName}", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()));
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
                .apply(documentationConfiguration(this.restDocumentation))
                .alwaysDo(this.document)
                .build();

        this.personRepository.deleteAll();
        this.personOne = this.personRepository.save(Person.builder().firstname("Mario").name("Muller").build());
        this.personTwo = this.personRepository.save(Person.builder().firstname("Heinz").name("Hegen").build());
    }

    @Test
    public void getPersons() throws Exception {
        this.mockMvc.perform(get("/persons"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$._embedded.persons", hasSize(2)))
                .andExpect(jsonPath("$._embedded.persons[*].person.name",
                        containsInAnyOrder(this.personOne.getName(), this.personTwo.getName())))
                .andExpect(jsonPath("$._embedded.persons[*].person.firstname",
                        containsInAnyOrder(this.personOne.getFirstname(), this.personTwo.getFirstname())))
                .andExpect(jsonPath("$._embedded.persons[*]._links.self.ref", is(notNullValue())))
                .andDo(this.document.snippets(
                        responseFields(
                                fieldWithPath("_embedded.persons").description("An array of persons"),
                                fieldWithPath("_links").description("Links to other resources")
                        ),
                        links(halLinks(),
                                linkWithRel("self").description("Link to this resource.")
                        )
                        ));
    }

    @Test
    public void getPerson() throws Exception {
        this.mockMvc.perform(get("/persons/{id}", this.personOne.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$.person.name", is(this.personOne.getName())))
                .andExpect(jsonPath("$.person.firstname", is(this.personOne.getFirstname())))
                .andDo(this.document.snippets(
                        pathParameters(
                                parameterWithName("id").description("The id to find the person with.")
                        ),
                        responseFields(
                                fieldWithPath("person.id").description("The person's id"),
                                fieldWithPath("person.name").description("The person's name"),
                                fieldWithPath("person.firstname").description("The person's first name"),
                                fieldWithPath("_links").description("Links to this resource")
                        ),
                        links(halLinks(),
                                linkWithRel("self").description("Link to this resource.")
                        )
                ));
    }

    @Test
    public void createPerson() throws Exception {
        this.mockMvc.perform(post("/persons")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(this.objectMapper.writeValueAsString(this.personOne))
                )
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$.person.name", is(this.personOne.getName())))
                .andExpect(jsonPath("$.person.firstname", is(this.personOne.getFirstname())))
                .andDo(this.document.snippets(
                        responseFields(
                                fieldWithPath("person.id").description("The person's id"),
                                fieldWithPath("person.name").description("The person's name"),
                                fieldWithPath("person.firstname").description("The person's first name"),
                                fieldWithPath("_links").description("Links to this resource")
                        ),
                        links(halLinks(),
                                linkWithRel("self").description("Link to this resource.")
                        )
                ));
    }

    @Test
    public void updatePerson() throws Exception {
        Person updatePerson = Person.builder().firstname("Mario").name("Meier").build();

        this.mockMvc.perform(put("/persons/{id}", this.personOne.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(updatePerson))
        )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$.person.id", is(this.personOne.getId())))
                .andExpect(jsonPath("$.person.name", is(updatePerson.getName())))
                .andExpect(jsonPath("$.person.firstname", is(updatePerson.getFirstname())))
                .andDo(this.document.snippets(
                        responseFields(
                                fieldWithPath("person.id").description("The person's id"),
                                fieldWithPath("person.name").description("The person's name"),
                                fieldWithPath("person.firstname").description("The person's first name"),
                                fieldWithPath("_links").description("Links to this resource")
                        ),
                        links(halLinks(),
                                linkWithRel("self").description("Link to this resource.")
                        )
                ));
    }

    @Test
    public void deletePerson() throws Exception {
        this.mockMvc.perform(delete("/persons/{id}", this.personOne.getId()))
                .andExpect(status().isNoContent());
    }
}