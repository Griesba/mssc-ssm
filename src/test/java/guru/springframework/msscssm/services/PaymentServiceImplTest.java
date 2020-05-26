package guru.springframework.msscssm.services;

import guru.springframework.msscssm.domain.Payment;
import guru.springframework.msscssm.domain.PaymentState;
import guru.springframework.msscssm.repositories.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;

import javax.transaction.Transactional;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PaymentServiceImplTest {

    @Autowired PaymentService paymentService;

    @Autowired
    PaymentRepository paymentRepository;

    Payment payment;

    @BeforeEach
    public void setUp() {
        payment = Payment.builder().amount(new BigDecimal("12.99")).build();
    }

    @Transactional
    @Test
    public void testPreAuth() {
        Payment savedPayment = paymentService.newPayment(payment);

        assertEquals("NEW", savedPayment.getState().toString());

        StateMachine sm = paymentService.preAuth(savedPayment.getId());

        assertEquals("PRE_AUTH", savedPayment.getState().toString());

        Payment preAuthPayment = paymentRepository.getOne(savedPayment.getId());

        assertEquals("PRE_AUTH", preAuthPayment.getState().toString());

        System.out.println(preAuthPayment);

    }

    @Test
    public void testAuthEvent() {
        Payment savedPayment = paymentService.newPayment(payment);
        assertEquals("NEW", savedPayment.getState().toString());

        StateMachine sm = paymentService.preAuth(savedPayment.getId());

        //assertEquals("PRE_AUTH", sm.getState().getId().toString());
        System.out.println(sm.getState().getId().toString());


        if (sm.getState().getId() == PaymentState.PRE_AUTH) {
            sm = paymentService.authorizePayment(savedPayment.getId());

            //assertEquals("AUTH", sm.getState().getId().toString());
            System.out.println(sm.getState().getId().toString());
        } else {
            System.out.println();
        }

    }
}