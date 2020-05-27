package guru.springframework.msscssm.config;

import guru.springframework.msscssm.domain.PaymentEvent;
import guru.springframework.msscssm.domain.PaymentState;
import guru.springframework.msscssm.services.PaymentServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import java.util.EnumSet;
import java.util.Random;

@Slf4j
@RequiredArgsConstructor
@EnableStateMachineFactory
@Configuration
public class StateMachineConfig extends StateMachineConfigurerAdapter<PaymentState, PaymentEvent> {

    //Pay attention that here names of attributes are matching with beans name.
    // If name of an attribute we should used @Qualifier to tell Spring witch bean to load.
    //Because we are not injecting by type but we are using common interface.
    private final Action<PaymentState, PaymentEvent> preAuthAction;
    private final Action<PaymentState, PaymentEvent> authAction;
    private final Guard<PaymentState, PaymentEvent> paymentIdGuard;
    private final Action<PaymentState, PaymentEvent> preAuthApprovedAction;
    private final Action<PaymentState, PaymentEvent> preAuthDeclinedAction;
    private final Action<PaymentState, PaymentEvent> authApprovedAction;
    private final Action<PaymentState, PaymentEvent> authDeclinedAction;

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
        transitions
                .withExternal()
                    .source(PaymentState.NEW)
                    .target(PaymentState.NEW)
                    .event(PaymentEvent.PRE_AUTHORIZE)
                    .action(preAuthAction)
                    .guard(paymentIdGuard)
                .and().withExternal()
                    .source(PaymentState.NEW)
                    .target(PaymentState.PRE_AUTH)
                    .event(PaymentEvent.PRE_AUTHORIZE_APPROVED)
                    .action(preAuthApprovedAction)
                .and().withExternal()
                    .source(PaymentState.NEW)
                    .target(PaymentState.PRE_AUTH_ERROR)
                    .event(PaymentEvent.PRE_AUTHORIZE_DECLINED)
                    .action(preAuthDeclinedAction)
                .and().withExternal()
                    .source(PaymentState.PRE_AUTH)
                    .target(PaymentState.PRE_AUTH)
                    .event(PaymentEvent.AUTHORIZE)
                    .action(authAction)
                .and().withExternal()
                    .source(PaymentState.PRE_AUTH)
                    .target(PaymentState.AUTH)
                    .event(PaymentEvent.AUTHORIZE_APPROVED)
                    .action(authApprovedAction)
                .and().withExternal()
                    .source(PaymentState.PRE_AUTH)
                    .target(PaymentState.AUTH_ERROR)
                    .event(PaymentEvent.AUTHORIZE_DECLINED)
                    .action(authDeclinedAction);

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
/*
    public Action<PaymentState, PaymentEvent> preAuthAction() {
        return context -> {
            log.info("PreAuth was called");
            if (new Random().nextInt(10) < 8){
                log.info("Approved");
                sendActionEvent(context, PaymentEvent.PRE_AUTHORIZE_APPROVED, PaymentServiceImpl.PAYMENT_ID);

            } else {
                log.info("Client not credit");
                sendActionEvent(context, PaymentEvent.PRE_AUTHORIZE_DECLINED, PaymentServiceImpl.PAYMENT_ID);
            }
        };
    }

    private Action<PaymentState, PaymentEvent> authAction() {
        return context -> {
            log.info("Authorized was called");
            if (new Random().nextInt(10) < 8) {
                log.info("Authorized");
                sendActionEvent(context, PaymentEvent.AUTHORIZE_APPROVED, PaymentServiceImpl.PAYMENT_ID);
            } else {
                log.info("Authorization declined");
                sendActionEvent(context, PaymentEvent.AUTHORIZE_DECLINED, PaymentServiceImpl.PAYMENT_ID);
            }
        };
    }

    private void sendActionEvent(StateContext<PaymentState, PaymentEvent> context, PaymentEvent authorizeApproved, String paymentId) {
        context.getStateMachine()
                .sendEvent(
                        MessageBuilder.withPayload(authorizeApproved)
                                .setHeader(paymentId, context.getMessageHeader(paymentId))
                                .build()
                );
    }

    public Guard<PaymentState, PaymentEvent> paymentIdGuard() {
        return context -> context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID) != null;
    }

    private Action<PaymentState, PaymentEvent> preAuthDeclineAction() {
        return context -> {
            log.info("Pre auth declined");
        };
    }

    private Action<PaymentState, PaymentEvent> authDeclineAction() {
        return context -> {
            log.info("Auth declined");
        };
    }*/
}
