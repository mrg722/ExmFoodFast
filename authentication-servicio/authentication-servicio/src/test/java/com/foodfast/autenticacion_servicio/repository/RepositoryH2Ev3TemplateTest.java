package com.foodfast.autenticacion_servicio.repository;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Disabled("Plantilla EV3: activar y adaptar al repository principal del microservicio para probar save, findById, findAll y delete con H2.")
class RepositoryH2Ev3TemplateTest {

    @Test
    void plantillaRepositoryConH2() {
        // Usar @Autowired sobre el Repository real.
        // Guardar entidad, buscar por id, listar y eliminar.
    }
}
