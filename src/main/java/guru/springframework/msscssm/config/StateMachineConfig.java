package guru.springframework.msscssm.config;

import guru.springframework.msscssm.domain.PaymentEvent;
import guru.springframework.msscssm.domain.PaymentState;
import guru.springframework.msscssm.services.PaymentServiceImpl;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import java.util.EnumSet;
import java.util.Random;

@Slf4j
@EnableStateMachineFactory
@Configuration
public class StateMachineConfig extends StateMachineConfigurerAdapter<PaymentState, PaymentEvent> {

    @Override
    public void configure(StateMachineStateConfigurer<PaymentState, PaymentEvent> states) throws Exception {
        states.withStates()
                .initial(PaymentState.NEW)
                .states(EnumSet.allOf(PaymentState.class))
                .end(PaymentState.AUTH)
                .end(PaymentState.AUTH_ERROR)
                .end(PaymentState.PRE_AUTH_ERROR);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<PaymentState, PaymentEvent> transitions) throws Exception {

        // Transition 1 : when on NEW if PRE_AUTHORIZE then go to NEW
        transitions.withExternal()
                .source(PaymentState.NEW).target(PaymentState.NEW).event(PaymentEvent.PRE_AUTHORIZE)
                .action(preAuthAction())
                .and().withExternal()
                .source(PaymentState.NEW).target(PaymentState.PRE_AUTH).event(PaymentEvent.PRE_AUTHORIZE_APPROVED)
                .and().withExternal()
                .source(PaymentState.NEW).target(PaymentState.PRE_AUTH_ERROR).event(PaymentEvent.PRE_AUTHORIZE_DECLINED);

    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<PaymentState, PaymentEvent> config) throws Exception {
        StateMachineListenerAdapter<PaymentState, PaymentEvent> adapter = new StateMachineListenerAdapter<>(){
            @Override
            public void stateChanged(State<PaymentState, PaymentEvent> from, State<PaymentState, PaymentEvent> to) {
                log.info(String.format("stageChange(from: %s, to: %s)", from, to));
            }
        };
        config.withConfiguration().listener(adapter);
    }

    public Action<PaymentState, PaymentEvent> preAuthAction() {
        return context -> {
            log.info("PreAuth was called");
            String headerPaymentId = PaymentServiceImpl.PAYMENT_ID;
            if (new Random().nextInt(10) < 8){
                log.info("Approved");
                context.getStateMachine()
                        .sendEvent(
                                MessageBuilder.withPayload(PaymentEvent.PRE_AUTHORIZE_APPROVED)
                                        .setHeader(headerPaymentId, context.getMessageHeader(headerPaymentId))
                                        .build()
                        );

            } else {
                log.info("Client not credit");
                context.getStateMachine()
                        .sendEvent(
                                MessageBuilder.withPayload(PaymentEvent.PRE_AUTHORIZE_DECLINED)
                                        .setHeader(headerPaymentId, context.getMessageHeader(headerPaymentId))
                                        .build()
                        );
            }
        };
    }
}
