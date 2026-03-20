package dev.lydtech.dispatch.handler;

import dev.lydtech.dispatch.service.DispatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderCreateHandlerTest {
    private OrderCreateHandler handler;
    private DispatchService dispatchServiceMock;

    @BeforeEach
    void setUp(){
        dispatchServiceMock=mock(DispatchService.class);
        handler=new OrderCreateHandler(dispatchServiceMock);
    }

    @Test
    void listen(){
        handler.listen("payload");
        verify(dispatchServiceMock,times(1)).process("payload");
    }


}