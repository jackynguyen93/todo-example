package com.example.todo;


import com.example.todo.controllers.WorkController;
import com.example.todo.models.Work;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;


@DatabaseSetup("/seeder.xml")
@RunWith(SpringRunner.class)
@SpringBootTest
@TestExecutionListeners({
        TransactionalTestExecutionListener.class,
        DependencyInjectionTestExecutionListener.class,
        DbUnitTestExecutionListener.class
})
@Transactional
@TestPropertySource(locations="/application-test.properties")
public class WorkControllerIntegrationTest {

    private final String API_URL = "/api/v1/works";
    private MockMvc mockMvc;

    @Autowired
    WorkController workController;

    @Autowired
    private ObjectMapper objectMapper;

    @Before
    public void setup() {
        this.mockMvc = standaloneSetup(this.workController).build();// Standalone context
    }

    @Test
    public void testGetAllWorksWithNoParamSuccess() throws Exception{
        MvcResult result = mockMvc.perform(get(API_URL).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        String responseContent = result.getResponse().getContentAsString();
        List<Work> works = objectMapper.readValue(responseContent, new TypeReference<List<Work>>(){});
        Assert.assertEquals(works.size(), 12);
    }

    @Test
    public void testGetAllWorksWithParamFail() throws Exception{
        String invalidPagingUrl = API_URL + "?pageNo=no&pageSize=unlimit";
        mockMvc.perform(get(invalidPagingUrl).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        String invalidSortUrl = API_URL + "?sortBy=no&sortDirection=media";
        mockMvc.perform(get(invalidSortUrl).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetAllWorksWithPagingParamNoResult() throws Exception{
        String pagingUrl = API_URL + "?pageNo=6&pageSize=5";
        MvcResult result = mockMvc.perform(get(pagingUrl).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        String responseContent = result.getResponse().getContentAsString();
        List<Work> works = objectMapper.readValue(responseContent, new TypeReference<List<Work>>(){});
        Assert.assertEquals(0, works.size());
    }

    @Test
    public void testGetAllWorksWithPagingParamSuccess() throws Exception{
        String pagingUrl = API_URL + "?pageNo=0&pageSize=5";
        MvcResult result = mockMvc.perform(get(pagingUrl).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        String responseContent = result.getResponse().getContentAsString();
        List<Work> works = objectMapper.readValue(responseContent, new TypeReference<List<Work>>(){});
        Assert.assertEquals(5, works.size());
        Assert.assertEquals(12, works.get(0).getId().longValue()); //default sort desc so last record id = 12 is first

        pagingUrl = API_URL + "?pageNo=1&pageSize=6";
        result = mockMvc.perform(get(pagingUrl).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        responseContent = result.getResponse().getContentAsString();
        works = objectMapper.readValue(responseContent, new TypeReference<List<Work>>(){});
        Assert.assertEquals(6, works.size());
        Assert.assertEquals(6, works.get(0).getId().longValue());
        Assert.assertEquals(1, works.get(works.size() - 1).getId().longValue());
    }

    @Test
    public void testGetAllWorksWithSortParamSuccess() throws Exception{
        String sortUrl = API_URL + "?sortBy=status";
        MvcResult result = mockMvc.perform(get(sortUrl).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        String responseContent = result.getResponse().getContentAsString();
        List<Work> works = objectMapper.readValue(responseContent, new TypeReference<List<Work>>(){});
        Assert.assertEquals(Work.Status.PLANNING, works.get(0).getStatus());

        sortUrl = API_URL + "?sortBy=status&sortDirection=ASC";
        result = mockMvc.perform(get(sortUrl).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        responseContent = result.getResponse().getContentAsString();
        works = objectMapper.readValue(responseContent, new TypeReference<List<Work>>(){});
        Assert.assertEquals(Work.Status.COMPLETE, works.get(0).getStatus());
    }

    @Test
    public void testGetAllWorksWithSortAndPagingParamSuccess() throws Exception{
        String requestUrl = API_URL + "?pageNo=0&pageSize=6&sortBy=startDate&sortDirection=ASC";
        MvcResult result = mockMvc.perform(get(requestUrl).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        String responseContent = result.getResponse().getContentAsString();
        List<Work> works = objectMapper.readValue(responseContent, new TypeReference<List<Work>>(){});
        Assert.assertEquals(6, works.size());
        Assert.assertEquals(2, works.get(0).getId().longValue());
    }

    @Test
    public void testGetWorkNotFound() throws Exception{
        mockMvc.perform(get(API_URL + "/100").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetWorkSuccess() throws Exception{
        MvcResult result = mockMvc.perform(get(API_URL + "/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        String responseContent = result.getResponse().getContentAsString();
        Work work = objectMapper.readValue(responseContent, Work.class);
        Assert.assertEquals("Task1", work.getWorkName());
    }

    @Test
    public void testCreateWorkFail() throws Exception{
        String sample = "{\"workName\": \"Home work fail\", \"startDate\": \"2019-12-03\", " +
                "\"endDate\": \"2019-12-04\", \"status\": \"INVALID\" }";
        mockMvc.perform(post(API_URL)
                .content(sample)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateWorkSuccess() throws Exception{
        String sample = "{\"workName\": \"Home work\", \"startDate\": \"2019-12-03\", " +
                "\"endDate\": \"2019-12-04\", \"status\": \"PLANNING\" }";
        MvcResult result = mockMvc.perform(post(API_URL)
                .content(sample)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        String responseContent = result.getResponse().getContentAsString();
        Work work = objectMapper.readValue(responseContent, Work.class);
        Assert.assertEquals("Home work", work.getWorkName());

        //verified by get this new
        result = mockMvc.perform(get(API_URL + "/" + work.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        responseContent = result.getResponse().getContentAsString();
        work = objectMapper.readValue(responseContent, Work.class);
        Assert.assertEquals("Home work", work.getWorkName());
    }

    @Test
    public void testUpdateWorkFail() throws Exception{
        String sample = "{\"workName\": \"Home work fail\", \"startDate\": \"2019-12-03\", " +
                "\"endDate\": \"2019-12-04\", \"status\": \"INVALID\" }";
        mockMvc.perform(put(API_URL + "/1")
                .content(sample)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateWorkNotFound() throws Exception{
        String sample = "{\"workName\": \"Home work\", \"startDate\": \"2019-12-03\", " +
                "\"endDate\": \"2019-12-04\", \"status\": \"PLANNING\" }";
        mockMvc.perform(put(API_URL + "/100")
                .content(sample)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUpdateWorkSuccess() throws Exception{
        String sample = "{\"workName\": \"Updated Home work\", \"startDate\": \"2019-12-03\", " +
                "\"endDate\": \"2019-12-04\", \"status\": \"PLANNING\" }";
        MvcResult result = mockMvc.perform(put(API_URL + "/1")
                .content(sample)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        String responseContent = result.getResponse().getContentAsString();
        Work work = objectMapper.readValue(responseContent, Work.class);
        Assert.assertEquals("Updated Home work", work.getWorkName());

        //verified by get this new
        result = mockMvc.perform(get(API_URL + "/" + work.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        responseContent = result.getResponse().getContentAsString();
        work = objectMapper.readValue(responseContent, Work.class);
        Assert.assertEquals("Updated Home work", work.getWorkName());
    }

    @Test
    public void testDeleteWorkNotFound() throws Exception{
        mockMvc.perform(delete(API_URL + "/100")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteWorkSuccess() throws Exception{
        mockMvc.perform(delete(API_URL + "/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        //verify this one is deleted
        mockMvc.perform(get(API_URL + "/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}