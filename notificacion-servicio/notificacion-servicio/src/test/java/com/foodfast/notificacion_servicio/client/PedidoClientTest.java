package com.foodfast.notificacion_servicio.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.foodfast.notificacion_servicio.dto.PedidoResponse;
import com.foodfast.notificacion_servicio.exception.ServicioExternoException;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class PedidoClientTest {

    @Test
    void debeRetornarNullCuandoIntegracionEstaDesactivada() {
        PedidoClient client = new PedidoClient(RestClient.builder(), "http://localhost:8083", false);

        assertThat(client.isEnabled()).isFalse();
        assertThat(client.buscarPedido(1L)).isNull();
    }

    @Test
    void debeBuscarPedidoCuandoIntegracionEstaActiva() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        PedidoClient client = new PedidoClient(builder, "http://pedido-test", true);

        server.expect(once(), requestTo("http://pedido-test/api/pedidos/1"))
                .andRespond(withSuccess("""
                        {
                          "success": true,
                          "message": "Pedido encontrado",
                          "data": {
                            "id": 1,
                            "clienteId": 10,
                            "estado": "CONFIRMADO",
                            "total": 15990
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        PedidoResponse response = client.buscarPedido(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getClienteId()).isEqualTo(10L);
        server.verify();
    }

    @Test
    void debeLanzarExcepcionCuandoPedidoServicioRetornaError() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        PedidoClient client = new PedidoClient(builder, "http://pedido-test", true);

        server.expect(once(), requestTo("http://pedido-test/api/pedidos/99"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client.buscarPedido(99L))
                .isInstanceOf(ServicioExternoException.class)
                .hasMessageContaining("HTTP");
        server.verify();
    }

    @Test
    void debeLanzarExcepcionCuandoRespuestaNoTraeData() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        PedidoClient client = new PedidoClient(builder, "http://pedido-test", true);

        server.expect(once(), requestTo("http://pedido-test/api/pedidos/2"))
                .andRespond(withSuccess("{\"success\":true,\"message\":\"ok\",\"data\":null}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.buscarPedido(2L))
                .isInstanceOf(ServicioExternoException.class)
                .hasMessageContaining("no devolvió datos");
        server.verify();
    }
}
