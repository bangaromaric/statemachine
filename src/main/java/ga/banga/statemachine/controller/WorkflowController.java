package ga.banga.statemachine.controller;

import ga.banga.statemachine.domain.OrderInvoice;
import ga.banga.statemachine.enumeration.OrderEvents;
import ga.banga.statemachine.enumeration.OrderStates;
import ga.banga.statemachine.repository.OrderInvoiceRepository;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;


/**
 * @author Romaric BANGA
 * @version 1.0
 * @since 3/31/24
 */
@RestController
public class WorkflowController {
    private final OrderInvoiceRepository orderRepository;


    private final StateMachineFactory<OrderStates, OrderEvents> stateMachineFactory;


    public WorkflowController(OrderInvoiceRepository orderRepository, StateMachineFactory<OrderStates, OrderEvents> stateMachineFactory) {
        this.orderRepository = orderRepository;
        this.stateMachineFactory = stateMachineFactory;
    }

    @PostMapping("/createOrder")
    public OrderInvoice createOrder(){
        OrderInvoice order = new OrderInvoice();
        order.setState(OrderStates.SUBMITTED.name());
        order.setLocalDate(LocalDate.now());
        return orderRepository.save(order);
    }

//
    @PutMapping("/change")
    public String changeState(@RequestBody OrderInvoice order){

        //making the machine in current state of the order
        StateMachine<OrderStates, OrderEvents> sm =    build(order);

        sm.getExtendedState().getVariables().put("paymentType",order.getPaymentType());
        sm.sendEvent(
                MessageBuilder.withPayload(OrderEvents.valueOf(order.getEvent()))
                        .setHeader("orderId",order.getId())
                        .setHeader("state",order.getState())
                        .build()
        );
        return "state changed";
    }

//    public StateMachine<OrderStates,OrderEvents> build(final OrderInvoice orderDto){
//        var orderDb =  this.orderRepository.findById(orderDto.getId());
//        var stateMachine =  this.stateMachineFactory.getStateMachine(orderDto.getId().toString());
//        stateMachine.stop();
//        stateMachine.getStateMachineAccessor()
//                .doWithAllRegions(sma -> {
//                    sma.resetStateMachine(new DefaultStateMachineContext<>(OrderStates.valueOf(orderDb.get().getState()), null, null, null));
//                });
//        stateMachine.start();
//        return stateMachine;
//    }

    public StateMachine<OrderStates,OrderEvents> build(final OrderInvoice orderDto){
        var orderDb =  this.orderRepository.findById(orderDto.getId());
        var stateMachine =  this.stateMachineFactory.getStateMachine(orderDto.getId().toString());
        stateMachine.stop();
        stateMachine.getStateMachineAccessor()
                .doWithAllRegions(sma -> {
                    sma.addStateMachineInterceptor(new StateMachineInterceptorAdapter<>() {
                        @Override
                        public void preStateChange(State<OrderStates, OrderEvents> state, Message<OrderEvents> message, Transition<OrderStates, OrderEvents> transition, StateMachine<OrderStates, OrderEvents> stateMachine, StateMachine<OrderStates, OrderEvents> rootStateMachine) {
                            var orderId = Long.class.cast(message.getHeaders().get("orderId"));
                            var order =  orderRepository.findById(orderId);
                            if(order.isPresent()){
                                order.get().setState(state.getId().name());
                                orderRepository.save(order.get());
                            }
                        }
                    });
                    sma.resetStateMachine(new DefaultStateMachineContext<>(OrderStates.valueOf(orderDb.get().getState()), null, null, null));
                });

        stateMachine.start();
        return stateMachine;

    }
}
