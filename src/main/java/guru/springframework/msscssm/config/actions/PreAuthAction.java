package guru.springframework.msscssm.config.actions;


import guru.springframework.msscssm.domain.PaymentEvent;
import guru.springframework.msscssm.domain.PaymentState;
import guru.springframework.msscssm.services.PaymentServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.Random;

@Slf4j
@Component
public class PreAuthAction implements Action<PaymentState, PaymentEvent> {
    public void execute(StateContext<PaymentState, PaymentEvent> context) {
        log.info("PreAuth was called");
        if (new Random().nextInt(10) < 8){
            log.info("Approved");
            context.getStateMachine()
                    .sendEvent(
                            MessageBuilder.withPayload(PaymentEvent.PRE_AUTHORIZE_APPROVED)
                                    .setHeader(PaymentServiceImpl.PAYMENT_ID, context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID))
                                    .build()
                    );

        } else {
            log.info("Client not credit");
            context.getStateMachine()
                    .sendEvent(
                            MessageBuilder.withPayload(PaymentEvent.PRE_AUTHORIZE_DECLINED)
                                    .setHeader(PaymentServiceImpl.PAYMENT_ID, context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID))
                                    .build()
                    );
        }
    }
}
