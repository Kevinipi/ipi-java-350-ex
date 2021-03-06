package com.ipiecoles.java.java350.service;
import com.ipiecoles.java.java350.model.Employe;
import com.ipiecoles.java.java350.model.NiveauEtude;
import com.ipiecoles.java.java350.model.Poste;
import com.ipiecoles.java.java350.exception.EmployeException;
import com.ipiecoles.java.java350.repository.EmployeRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityExistsException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
 class EmployeServiceTest {

    @InjectMocks
    EmployeService employeService;

    @Mock
    EmployeRepository employeRepository;

    @BeforeEach
     void setup(){
        MockitoAnnotations.initMocks(this.getClass());
    }

    //Test sur la méthode d'embaucheEmploye si technicien avec un niveau BTS
    @Test
     void testEmbaucheEmployeTechnicienPleinTempsBts() throws EmployeException {
        //Given
        String nom = "Doe";
        String prenom = "John";
        Poste poste = Poste.TECHNICIEN;
        NiveauEtude niveauEtude = NiveauEtude.BTS_IUT;
        Double tempsPartiel = 1.0;
        when(employeRepository.findLastMatricule()).thenReturn("00345");
        when(employeRepository.findByMatricule("T00346")).thenReturn(null);

        //When
        employeService.embaucheEmploye(nom, prenom, poste, niveauEtude, tempsPartiel);

        //Then
        ArgumentCaptor<Employe> employeArgumentCaptor = ArgumentCaptor.forClass(Employe.class);
        verify(employeRepository, times(1)).save(employeArgumentCaptor.capture());
        Assertions.assertEquals(nom, employeArgumentCaptor.getValue().getNom());
        Assertions.assertEquals(prenom, employeArgumentCaptor.getValue().getPrenom());
        Assertions.assertEquals(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")), employeArgumentCaptor.getValue().getDateEmbauche().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        Assertions.assertEquals("T00346", employeArgumentCaptor.getValue().getMatricule());
        Assertions.assertEquals(tempsPartiel, employeArgumentCaptor.getValue().getTempsPartiel());

        //1521.22 * 1.2 * 1.0
        Assertions.assertEquals(1825.46, employeArgumentCaptor.getValue().getSalaire().doubleValue());
    }

    //Test sur la méthode d'embaucheEmploye si Manager avec un niveau Master
    @Test
     void testEmbaucheEmployeManagerMiTempsMaster() throws EmployeException {
        //Given
        String nom = "Doe";
        String prenom = "John";
        Poste poste = Poste.MANAGER;
        NiveauEtude niveauEtude = NiveauEtude.MASTER;
        Double tempsPartiel = 0.5;
        when(employeRepository.findLastMatricule()).thenReturn("00345");
        when(employeRepository.findByMatricule("M00346")).thenReturn(null);

        //When
        employeService.embaucheEmploye(nom, prenom, poste, niveauEtude, tempsPartiel);

        //Then
        ArgumentCaptor<Employe> employeArgumentCaptor = ArgumentCaptor.forClass(Employe.class);
        verify(employeRepository, times(1)).save(employeArgumentCaptor.capture());
        Assertions.assertEquals(nom, employeArgumentCaptor.getValue().getNom());
        Assertions.assertEquals(prenom, employeArgumentCaptor.getValue().getPrenom());
        Assertions.assertEquals(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")), employeArgumentCaptor.getValue().getDateEmbauche().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        Assertions.assertEquals("M00346", employeArgumentCaptor.getValue().getMatricule());
        Assertions.assertEquals(tempsPartiel, employeArgumentCaptor.getValue().getTempsPartiel());

        //1521.22 * 1.4 * 0.5
        Assertions.assertEquals(1064.85, employeArgumentCaptor.getValue().getSalaire().doubleValue());
    }

    //Test sur la méthode d'embaucheEmploye si manager à mi-temps mais n'a pas le dernier matricule
    @Test
     void testEmbaucheEmployeManagerMiTempsMasterNoLastMatricule() throws EmployeException {
        //Given
        String nom = "Doe";
        String prenom = "John";
        Poste poste = Poste.MANAGER;
        NiveauEtude niveauEtude = NiveauEtude.MASTER;
        Double tempsPartiel = 0.5;
        when(employeRepository.findLastMatricule()).thenReturn(null);
        when(employeRepository.findByMatricule("M00001")).thenReturn(null);

        //When
        employeService.embaucheEmploye(nom, prenom, poste, niveauEtude, tempsPartiel);

        //Then
        ArgumentCaptor<Employe> employeArgumentCaptor = ArgumentCaptor.forClass(Employe.class);
        verify(employeRepository, times(1)).save(employeArgumentCaptor.capture());
        Assertions.assertEquals("M00001", employeArgumentCaptor.getValue().getMatricule());
    }

    //Test sur la méthode d'embaucheEmploye si un manager à mi-temps avec un niveau Master existe
    @Test
     void testEmbaucheEmployeManagerMiTempsMasterExistingEmploye(){
        //Given
        String nom = "Doe";
        String prenom = "John";
        Poste poste = Poste.MANAGER;
        NiveauEtude niveauEtude = NiveauEtude.MASTER;
        Double tempsPartiel = 0.5;
        when(employeRepository.findLastMatricule()).thenReturn(null);
        when(employeRepository.findByMatricule("M00001")).thenReturn(new Employe());

        //When/Then
        EntityExistsException e = Assertions.assertThrows(EntityExistsException.class, () -> employeService.embaucheEmploye(nom, prenom, poste, niveauEtude, tempsPartiel));
        Assertions.assertEquals("L'employé de matricule M00001 existe déjà en BDD", e.getMessage());
    }

    //Test sur la méthode d'embaucheEmploye si un manager à mi-temps à pour matricule 99999 ou plus
    @Test
     void testEmbaucheEmployeManagerMiTempsMaster99999(){
        //Given
        String nom = "Doe";
        String prenom = "John";
        Poste poste = Poste.MANAGER;
        NiveauEtude niveauEtude = NiveauEtude.MASTER;
        Double tempsPartiel = 0.5;
        when(employeRepository.findLastMatricule()).thenReturn("99999");

        //When/Then
        EmployeException e = Assertions.assertThrows(EmployeException.class, () -> employeService.embaucheEmploye(nom, prenom, poste, niveauEtude, tempsPartiel));
        Assertions.assertEquals("Limite des 100000 matricules atteinte !", e.getMessage());
    }

    //Test de la méthode calculPerformanceCommercial si un matricule donné existe
    @Test
     void testCalculPerformanceCommercialMatriculeIsExist() {
        //Given
        String matricule = "C00001";
        Long caTraite = 40L;
        Long objectifCa = 13454L;
        try{
            employeService.calculPerformanceCommercial(matricule,caTraite,objectifCa);
            Assertions.fail("Aurait du lancer une exception");
        } catch(EmployeException e){
            //Then
            org.assertj.core.api.Assertions.assertThat(e).isInstanceOf(EmployeException.class);
            org.assertj.core.api.Assertions.assertThat(e.getMessage()).isEqualTo("Le matricule C00001 n'existe pas !");
        }
    }

    //Test de la méthode calculPerformanceCommercial si un matricule est null
    @Test
     void testCalculPerformanceCommercialMatriculeNull1() {
        //Given
        String matricule = null;
        Long caTraite = 30L;
        Long objectifCa = 15000L;

        employeRepository.findByMatricule(matricule);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> {
            //When
            employeService.calculPerformanceCommercial(matricule,caTraite,objectifCa);
        })//Then
                .isInstanceOf(EmployeException.class).hasMessage("Le matricule ne peut être null et doit commencer par un C !");
    }

    //Test Mocker de la méthode calculPerformanceCommercial si un matricule donné existe et est null
     @ParameterizedTest
    @CsvSource({"C00011",
            "C00012", })
     void testCalculPerformanceCommercialMatriculeIsNull2(String matricule) {
        //Given
        Mockito.when(employeRepository.findByMatricule(matricule)).thenReturn(null);
        //When
        EmployeException e = Assertions.assertThrows(EmployeException.class, () ->  employeService.calculPerformanceCommercial(matricule,2000L , 2500L));
        //Then
        Assertions.assertEquals(e.getMessage(), "Le matricule "+matricule+" n'existe pas !");
    }

    //Test paramétré de la méthode calculPerformanceCommercial si un matricule donné existe et est null
    @ParameterizedTest
    @CsvSource({"C00011",
            "C00012", })
     void testCalculPerformanceCommercialEmployeIsNull(String matricule) {
        //Given
        Employe emp = employeRepository.findByMatricule(matricule);
        try {
            Mockito.when(emp).thenReturn(null);
            employeService.calculPerformanceCommercial(matricule, 2000L, 2500L);
            //When
        }catch (EmployeException e){
            //Then
            Assertions.assertEquals(e.getMessage(), "Le matricule "+matricule+" n'existe pas !");
        }
    }

    //Test de la méthode calculPerformanceCommercial si un matricule commence par C
    @Test
     void testCalculPerformanceCommercialMatriculeStartC(){
        //Given
        String matricule = "M00001";
        Long caTraite = 50L;
        Long objectifCa = 20000L;
        //Given
        try{
            employeService.calculPerformanceCommercial(matricule,caTraite,objectifCa);
            Assertions.fail("Aurait du lancer une exception");
        } catch(EmployeException e){
            //Then
            org.assertj.core.api.Assertions.assertThat(e).isInstanceOf(EmployeException.class);
            org.assertj.core.api.Assertions.assertThat(e.getMessage()).isEqualTo("Le matricule ne peut être null et doit commencer par un C !");
        }
    }

    //Test de la méthode calculPerformanceCommercial si le CA traité (caTraite) est négatif
    @Test
     void testCalculPerformanceCommercialCaTraiteNegatif(){
        //Given
        String matricule = "C00001";
        Long caTraite = -40L;
        Long objectifCa = 40000L;
        try{
            employeService.calculPerformanceCommercial(matricule,caTraite,objectifCa);
            Assertions.fail("Aurait du lancer une exception");
        } catch(EmployeException e){
            //Then
            org.assertj.core.api.Assertions.assertThat(e).isInstanceOf(EmployeException.class);
            org.assertj.core.api.Assertions.assertThat(e.getMessage()).isEqualTo("Le chiffre d'affaire ou l'objectif de chiffre d'affaire traités ne peuvent être négatifs ou null !");
        }
    }

    //Test de la méthode calculPerformanceCommercial si le CA traité (caTraite) est null
    @Test
     void testCalculPerformanceCommercialCaTraiteNull(){
        //Given
        String matricule = "C00001";
        Long caTraite = null;
        Long objectifCa = 40000L;
        try{
            employeService.calculPerformanceCommercial(matricule,caTraite,objectifCa);
            Assertions.fail("Aurait du lancer une exception");
        } catch(EmployeException e){
            //Then
            org.assertj.core.api.Assertions.assertThat(e).isInstanceOf(EmployeException.class);
            org.assertj.core.api.Assertions.assertThat(e.getMessage()).isEqualTo("Le chiffre d'affaire ou l'objectif de chiffre d'affaire traités ne peuvent être négatifs ou null !");
        }
    }

    //Test de la méthode calculPerformanceCommercial si l'objectif CA (ObjectifCa) est négatif
    @Test
     void testCalculPerformanceCommercialObjectifCaNegatif(){
        //Given
        String matricule = "C00001";
        Long caTraite = 50L;
        Long objectifCa = -40000L;
        try{
            employeService.calculPerformanceCommercial(matricule,caTraite,objectifCa);
            Assertions.fail("Aurait du lancer une exception");
        } catch(EmployeException e){
            //Then
            org.assertj.core.api.Assertions.assertThat(e).isInstanceOf(EmployeException.class);
            org.assertj.core.api.Assertions.assertThat(e.getMessage()).isEqualTo("Le chiffre d'affaire ou l'objectif de chiffre d'affaire traités ne peuvent être négatifs ou null !");
        }
    }

    //Test de la méthode calculPerformanceCommercial si l'objectif CA (ObjectifCa) est null
    @Test
     void testCalculPerformanceCommercialObjectifCaNull(){
        //Given
        String matricule = "C00001";
        Long caTraite = 50L;
        Long objectifCa = null;
        try{
            employeService.calculPerformanceCommercial(matricule,caTraite,objectifCa);
            Assertions.fail("Aurait du lancer une exception");
        } catch(EmployeException e){
            //Then
            org.assertj.core.api.Assertions.assertThat(e).isInstanceOf(EmployeException.class);
            org.assertj.core.api.Assertions.assertThat(e.getMessage()).isEqualTo("Le chiffre d'affaire ou l'objectif de chiffre d'affaire traités ne peuvent être négatifs ou null !");
        }
    }

    //Test paramétré de la méthode calculPerformanceCommercial afin de couvrir plusieur condition
    @ParameterizedTest
    @CsvSource({"'C00011', 2000, 2500, Le matricule C00011 n'existe pas !",
            "'C00011',, 2500, Le chiffre d'affaire ou l'objectif de chiffre d'affaire traités ne peuvent être négatifs ou null !",
            "'C00011',2000,, Le chiffre d'affaire ou l'objectif de chiffre d'affaire traités ne peuvent être négatifs ou null !",
            "'M00011',2000, 2500, Le matricule ne peut être null et doit commencer par un C !",
            "'C00012', 2000, 2500, Le matricule C00012 n'existe pas !",
            "'C00012', -2000, 2500, Le chiffre d'affaire ou l'objectif de chiffre d'affaire traités ne peuvent être négatifs ou null !",
            "'C00012', 2000, -2500, Le chiffre d'affaire ou l'objectif de chiffre d'affaire traités ne peuvent être négatifs ou null !",
            "'C00012', -2000, -2500, Le chiffre d'affaire ou l'objectif de chiffre d'affaire traités ne peuvent être négatifs ou null !",
            ", 2000, 2500, Le matricule ne peut être null et doit commencer par un C !",
    })
    void calculPerformanceCommercialNotFoundTest(String matricule, Long caTraite, Long objectifCa, String result) {
        //Given
        if((caTraite != null && caTraite != -2000) && (objectifCa != null && objectifCa != -2500) && (matricule != null && !matricule.equals("M00011"))) {
            Mockito.when(employeRepository.findByMatricule(matricule)).thenReturn(null);
        }
        //When
        EmployeException e = Assertions.assertThrows(EmployeException.class, () ->  employeService.calculPerformanceCommercial(matricule,caTraite , objectifCa));
        //Then
        org.junit.jupiter.api.Assertions.assertEquals(e.getMessage(), result);
    }

    //Test paramétré de la méthode calculPerformanceCommercial afin de couvrir plusieur condition
    @ParameterizedTest
    @CsvSource({
            "'C00011', 800, 2500, 1",
            "'C00011', 2000, 2500, 1",
            "'C00011', 2200, 2500, 1",
            "'C00011', 2499, 2500, 1",
            "'C00011', 2500, 2500, 1",
            "'C00011', 2502, 2500, 1",
            "'C00011', 2550, 2500, 1",
            "'C00011', 2600, 2500, 1",
            "'C00012', 3000, 2500, 1",
            "'C00013', 10000, 2500,1",
    })
    void calculPerformanceCommercialNotFoundTest2(String matricule, Long caTraite, Long objectifCa, Integer result) throws EmployeException {
        //Given
        Employe employe = new Employe("Delacour", "Michel", "T00001", LocalDate.now(), 1825.46, 1, null);
        Mockito.when(employeRepository.findByMatricule(matricule)).thenReturn(employe);
        Integer perf = employe.getPerformance();
        if (matricule.equals("C00012")) {
            Mockito.when( employeRepository.avgPerformanceWhereMatriculeStartsWith("C")).thenReturn(40D);
        } else if (matricule.equals("C00013")) {
            Mockito.when( employeRepository.avgPerformanceWhereMatriculeStartsWith("C")).thenReturn(null);
        } else {
            Mockito.when( employeRepository.avgPerformanceWhereMatriculeStartsWith("C")).thenReturn(1D);
        }
        //When
        employeService.calculPerformanceCommercial(matricule, caTraite, objectifCa);
        Employe newEmploye = new Employe("Delacour", "Michel", "T00001", LocalDate.now(), 1825.46, 1, null);
        Mockito.when(employeRepository.findByMatricule(matricule)).thenReturn(newEmploye);
        //Then
        Assertions.assertEquals(employeRepository.findByMatricule(matricule).getPerformance(), result);
    }
}