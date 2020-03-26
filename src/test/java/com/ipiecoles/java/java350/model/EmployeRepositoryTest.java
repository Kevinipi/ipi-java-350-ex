package com.ipiecoles.java.java350.model;

import com.ipiecoles.java.java350.repository.EmployeRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

@SpringBootTest
public class EmployeRepositoryTest {
    @Autowired
    EmployeRepository employeRepository;

   @BeforeEach //fonctionne même si l'utilisateur à oublier de supprimer les données
    void setUp() {
        employeRepository.deleteAll();
    }

    @Test
    void findLastMatriculeEmploye() {
        //Given
        Employe employe1 = new Employe("Doe", "John", "T12345", LocalDate.now(), 2000d, 1, 1.0);
        Employe employe2 = new Employe("black", "Kevin", "C67890", LocalDate.now(), 5000d, 4, 2.0);
        Employe employe3 = new Employe("Purle", "Steve", "M45678", LocalDate.now(), 4000d, 2, 1.0);

        employeRepository.save(employe1);
        employeRepository.save(employe2);
        employeRepository.save(employe3);

        //When
        String lastMatricule = employeRepository.findLastMatricule();

        //then
        Assertions.assertThat(lastMatricule).isEqualTo("67890");
    }
}