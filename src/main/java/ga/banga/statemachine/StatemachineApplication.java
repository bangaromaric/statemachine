package ga.banga.statemachine;

import ga.banga.statemachine.enumeration.Events;
import ga.banga.statemachine.enumeration.States;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.statemachine.StateMachine;

@SpringBootApplication
public class StatemachineApplication implements CommandLineRunner {
    @Autowired
    private StateMachine<States, Events> stateMachine;

    public static void main(String[] args) {
        SpringApplication.run(StatemachineApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        stateMachine.sendEvent(Events.E1);
        stateMachine.sendEvent(Events.E2);

        System.out.println("*************** "+ stateMachine.getState().getId());
    }
}
