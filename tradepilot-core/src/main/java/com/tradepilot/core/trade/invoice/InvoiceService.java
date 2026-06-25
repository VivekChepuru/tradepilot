package com.tradepilot.core.trade.invoice;

import com.tradepilot.core.config.BusinessProperties;
import com.tradepilot.core.trade.customer.TradeContact;
import com.tradepilot.core.trade.order.Order;
import com.tradepilot.core.webhook.dto.OutboundMessageEvent;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final BusinessProperties businessProperties;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${tradepilot.kafka.topics.messages-outbound}")
    private String outboundTopic;

    @Transactional
    public void generateAndSend(Order order, TradeContact contact) {
        if (invoiceRepository.existsByOrderId(order.getId())) {
            log.warn("Invoice already exists for orderId={}, skipping duplicate generation", order.getId());
            return;
        }

        BusinessProperties.Invoice invoiceConfig = businessProperties.getInvoice();
        BusinessProperties.Bank bank = businessProperties.getBank();

        BigDecimal gstRate = invoiceConfig.getGstRate();
        BigDecimal qty = Objects.requireNonNullElse(order.getQuantity(), BigDecimal.ONE);
        BigDecimal subtotal = order.getQuotedPrice().multiply(qty);
        BigDecimal gstAmount = subtotal.multiply(gstRate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = subtotal.add(gstAmount);

        String invoiceNumber = "TP-INV-" + order.getId() + "-" + System.currentTimeMillis();

        LocalDateTime issuedAt = LocalDateTime.now();
        LocalDateTime dueDate = "ADVANCE".equals(order.getPaymentTerms())
                ? issuedAt.plusDays(invoiceConfig.getDueDaysAdvance())
                : issuedAt.plusDays(invoiceConfig.getDueDaysPostDelivery());

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String invoiceText = String.format(
                "TradePilot — Invoice%n" +
                "━━━━━━━━━━━━━━━━━━━━%n" +
                "Invoice No : %s%n" +
                "Order Ref  : %s%n" +
                "Date       : %s%n" +
                "Due Date   : %s%n" +
                "━━━━━━━━━━━━━━━━━━━━%n" +
                "Item       : %s %s%n" +
                "Quantity   : %s %s%n" +
                "Rate       : ₹%,.2f/MT%n" +
                "━━━━━━━━━━━━━━━━━━━━%n" +
                "Subtotal   : ₹%,.2f%n" +
                "GST (18%%)  : ₹%,.2f%n" +
                "TOTAL      : ₹%,.2f%n" +
                "━━━━━━━━━━━━━━━━━━━━%n" +
                "Payment via RTGS/NEFT:%n" +
                "Bank       : %s%n" +
                "A/C Name   : %s%n" +
                "A/C No     : %s%n" +
                "IFSC       : %s%n" +
                "UPI        : %s%n" +
                "━━━━━━━━━━━━━━━━━━━━%n" +
                "Thank you for your business!",
                invoiceNumber,
                order.getOrderReference(),
                issuedAt.format(fmt),
                dueDate.format(fmt),
                order.getCommodity(), Objects.toString(order.getGrade(), ""),
                qty, Objects.toString(order.getUnit(), ""),
                order.getQuotedPrice(),
                subtotal,
                gstAmount,
                totalAmount,
                bank.getBankName(),
                bank.getAccountName(),
                bank.getAccountNumber(),
                bank.getIfscCode(),
                bank.getUpiId()
        );

        Invoice invoice = Invoice.builder()
                .invoiceNumber(invoiceNumber)
                .order(order)
                .tradeContact(contact)
                .subtotal(subtotal.setScale(2, RoundingMode.HALF_UP))
                .gstRate(gstRate)
                .gstAmount(gstAmount)
                .totalAmount(totalAmount.setScale(2, RoundingMode.HALF_UP))
                .quantity(qty)
                .unit(order.getUnit())
                .commodity(order.getCommodity())
                .grade(order.getGrade())
                .paymentTerms(Objects.toString(order.getPaymentTerms(), "POST_DELIVERY"))
                .status("SENT")
                .invoiceText(invoiceText)
                .issuedAt(issuedAt)
                .dueDate(dueDate)
                .sentAt(LocalDateTime.now())
                .build();

        invoiceRepository.save(invoice);

        OutboundMessageEvent outboundEvent = OutboundMessageEvent.builder()
                .routingDecision("INVOICE_SENT")
                .suggestedReply(invoiceText)
                .fromNumber(contact.getWhatsappNumber())
                .whatsappMessageId("INV-" + invoiceNumber)
                .processedAt(LocalDateTime.now())
                .build();

        kafkaTemplate.send(outboundTopic, contact.getWhatsappNumber(), outboundEvent);

        log.info("Invoice {} generated and sent for orderId={} contact={}",
                invoiceNumber, order.getId(), contact.getWhatsappNumber());
    }
}
