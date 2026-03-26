package ru.msu.cmc.webprac.DAOtests;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import ru.msu.cmc.webprac.backend.DAO.BaseDAO;
import ru.msu.cmc.webprac.backend.entity.Company;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(locations="classpath:application.properties")
public class BaseDAOTest {
    @Autowired
    private BaseDAO<Company, Integer> baseDAO;

    @Autowired
    private SessionFactory sessionFactory;

    private Company company0;
    private Company company1;

    @BeforeEach
    void fillDatabase() {
        company0 = new Company("name0");
        company1 = new Company("name1");

        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.persist(company0);
            session.persist(company1);
            session.getTransaction().commit();
        }
    }

    @AfterEach
    @BeforeAll
    void cleanDatabase() {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.createNativeMutationQuery("DELETE FROM company").executeUpdate();
            session.getTransaction().commit();
        }
    }

    @Test
    void getTest() {
        Company getCompany = baseDAO.getById(company0.getId());
        Assertions.assertEquals(company0.getId(), getCompany.getId());
        Assertions.assertEquals(company0.getName(), getCompany.getName());
    }

    @Test
    void getAllTest() {
        Set<Company> getCompanies = new HashSet<>(baseDAO.getAll());
        Set<Company> allCompanies = Set.of(company0, company1);

        Set<Integer> getIds = getCompanies.stream().map(Company::getId).collect(Collectors.toSet());
        Set<Integer> realIds = allCompanies.stream().map(Company::getId).collect(Collectors.toSet());


        Set<String> getNames = getCompanies.stream().map(Company::getName).collect(Collectors.toSet());
        Set<String> realNames = allCompanies.stream().map(Company::getName).collect(Collectors.toSet());

        Assertions.assertEquals(realIds, getIds);
        Assertions.assertEquals(realNames, getNames);
    }

    @Test
    void saveTest() {
        Company newCompany = new Company("name2");
        baseDAO.save(newCompany);
        Company getCompany = baseDAO.getById(newCompany.getId());
        Assertions.assertNotNull(getCompany);
        Assertions.assertEquals(newCompany.getId(), getCompany.getId());
        Assertions.assertEquals(newCompany.getName(), getCompany.getName());
    }

    @Test
    void saveAllTest() {
        Company company2 = new Company("name2");
        Company company3 = new Company("name3");
        List<Company> companies = List.of(company2, company3);
        baseDAO.saveAll(companies);
        Company getCompany = baseDAO.getById(company2.getId());
        Assertions.assertNotNull(getCompany);
        Assertions.assertEquals(company2.getId(), getCompany.getId());
        Assertions.assertEquals(company2.getName(), getCompany.getName());
        getCompany = baseDAO.getById(company3.getId());
        Assertions.assertNotNull(getCompany);
        Assertions.assertEquals(company3.getId(), getCompany.getId());
        Assertions.assertEquals(company3.getName(), getCompany.getName());
    }

    @Test
    void testDelete() {
        baseDAO.delete(company0);
        Company getCompany = baseDAO.getById(company0.getId());
        Assertions.assertNull(getCompany);
    }

    @Test
    void testDeleteById() {
        baseDAO.deleteById(company0.getId());
        Company getCompany = baseDAO.getById(company0.getId());
        Assertions.assertNull(getCompany);
    }

    @Test
    void testUpdate() {
        company0.setName("new name");
        baseDAO.update(company0);
        Company getCompany = baseDAO.getById(company0.getId());
        Assertions.assertNotNull(getCompany);
        Assertions.assertEquals(company0.getId(), getCompany.getId());
        Assertions.assertEquals(company0.getName(), getCompany.getName());
    }
}
