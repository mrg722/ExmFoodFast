package com.foodfast.pago_servicio.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.foodfast.pago_servicio.dto.PedidoResponse;
import com.foodfast.pago_servicio.exception.ServicioExternoException;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class PedidoClientTest {

    @Test
    void obtenerPedidoDebeOmitirLlamadaCuandoIntegracionEstaDesactivada() {
        PedidoClient client = new PedidoClient(RestClient.builder(), "http://localhost:8083", false);

        PedidoResponse response = client.obtenerPedido(10L);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getEstado()).isEqualTo("NO_VALIDADO");
    }

    @Test
    void obtenerPedidoDebeLeerApiResponseCuandoIntegracionEstaActiva() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        PedidoClient client = new PedidoClient(builder, "http://pedido-test", true);

        server.expect(requestTo("http://pedido-test/api/pedidos/10"))
                .andRespond(withSuccess("{\"success\":true,\"message\":\"ok\",\"data\":{\"id\":10,\"clienteId\":3,\"estado\":\"CONFIRMADO\",\"total\":15990}}", MediaType.APPLICATION_JSON));

        PedidoResponse response = client.obtenerPedido(10L);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getEstado()).isEqualTo("CONFIRMADO");
        server.verify();
    }

    @Test
    void obtenerPedidoDebeLanzarServicioExternoCuandoPedidoRespondeError() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        PedidoClient client = new PedidoClient(builder, "http://pedido-test", true);

        server.expect(requestTo("http://pedido-test/api/pedidos/99"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client.obtenerPedido(99L))
                .isInstanceOf(ServicioExternoException.class)
                .hasMessageContaining("pedido-servicio respondió");
    }

    @Test
    void obtenerPedidoDebeLanzarCuandoRespuestaNoTraeData() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        PedidoClient client = new PedidoClient(builder, "http://pedido-test", true);

        server.expect(requestTo("http://pedido-test/api/pedidos/11"))
                .andRespond(withSuccess("{\"success\":true,\"message\":\"ok\",\"data\":null}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.obtenerPedido(11L))
                .isInstanceOf(ServicioExternoException.class)
                .hasMessageContaining("no devolvió datos");
    }
}
