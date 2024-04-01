package ga.banga.statemachine.repository;

import ga.banga.statemachine.domain.OrderInvoice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderInvoiceRepository extends JpaRepository<OrderInvoice, Long> {
}