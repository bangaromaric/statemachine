package ga.banga.statemachine.config;

import ga.banga.statemachine.enumeration.Events;
import ga.banga.statemachine.enumeration.OrderEvents;
import ga.banga.statemachine.enumeration.OrderStates;
import ga.banga.statemachine.enumeration.States;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.monitor.StateMachineMonitor;
import org.springframework.statemachine.state.State;
import org.springframework.util.StringUtils;

import java.util.EnumSet;

/**
 * @author Romaric BANGA
 * @version 1.0
 * @since 3/30/24
 */
@Configuration
//@EnableStateMachine
@EnableStateMachineFactory
public class StateMachineConfig extends EnumStateMachineConfigurerAdapter<OrderStates, OrderEvents> {

    private static final Logger log = LoggerFactory.getLogger(StateMachineConfig.class);


    @Override
    public void configure(StateMachineConfigurationConfigurer<OrderStates, OrderEvents> config)
            throws Exception {

        config

                .withConfiguration()
                .autoStartup(true)
                .listener(listener());
    }

    @Override
    public void configure(StateMachineStateConfigurer<OrderStates, OrderEvents> states)
            throws Exception {
        states
                .withStates()
                .initial(OrderStates.SUBMITTED)
                .state(OrderStates.PAID)
                .end(OrderStates.FULFILLED)
                .end(OrderStates.CANCELED);
//                .states(EnumSet.allOf(OrderStates.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<OrderStates, OrderEvents> transitions)
            throws Exception {
        transitions
                .withExternal()
                .source(OrderStates.SUBMITTED)
                .target(OrderStates.PAID)
                .event(OrderEvents.PAY)
                .guard(ctx -> {
                    log.info(" true->statechanged. false->do not change " );
                    var paymentType = String.class.cast(ctx.getExtendedState()
                            .getVariables().get("paymentType"));
                    log.info("paymentType is {} ",paymentType );

                    if (!StringUtils.isEmpty(paymentType) && paymentType.equals("cod"))
                        return false;
                    else return true;
                })

                .and()
                .withExternal()
                .source(OrderStates.PAID)
                .target(OrderStates.FULFILLED)
                .event(OrderEvents.FULFILL)
                .action(ctx -> {
                    log.info("This PAID handler where we can perform some logging");
                })

                .and()
                .withExternal()
                .source(OrderStates.SUBMITTED)
                .target(OrderStates.CANCELED)
                .event(OrderEvents.CANCEL)
                .action(ctx -> {
                    log.info("This SUBMITTED handler where we can perform some logging");
                })


                .and()
                .withExternal()
                .source(OrderStates.PAID)
                .target(OrderStates.CANCELED)
                .event(OrderEvents.CANCEL)
                .action(ctx -> {
                    log.info("This PAID handler where we can perform some logging");
                });
//        transitions
//                .withExternal()
//                .source(States.SI).target(States.S1).event(Events.E1)
//                .and()
//                .withExternal()
//                .source(States.S1).target(States.S2).event(Events.E2);
    }

    @Bean
    public StateMachineListener<OrderStates, OrderEvents> listener() {
        return new StateMachineListenerAdapter<>() {
            @Override
            public void stateChanged(State<OrderStates, OrderEvents> from, State<OrderStates, OrderEvents> to) {
                System.out.println("State change to " + to.getId());
            }
        };
    }

}