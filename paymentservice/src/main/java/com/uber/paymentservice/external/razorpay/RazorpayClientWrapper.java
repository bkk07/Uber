package com.uber.paymentservice.external.razorpay;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.uber.paymentservice.config.RazorpayConfig;
import com.uber.paymentservice.exception.PaymentVerificationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Component
@Slf4j
@RequiredArgsConstructor
public class RazorpayClientWrapper {

    private final RazorpayClient razorpayClient;
    private final RazorpayConfig razorpayConfig;

    @Value("${razorpay.api.secret}")
    private String secretKey;

    /**
     * Creates a Razorpay order using real Razorpay API.
     * @param amountInPaise Amount in smallest currency unit (e.g., 10000 = ₹100).
     * @param currency INR, USD, etc.
     * @return Razorpay order_id
     * @throws RazorpayException if order creation fails
     */
    public String createOrder(double amountInPaise, String currency) throws RazorpayException {
        log.info("Creating Razorpay order for amount: {} {}", amountInPaise, currency);

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", (int) amountInPaise); // Razorpay expects int
        orderRequest.put("currency", currency);
        orderRequest.put("payment_capture", 1); // auto capture payments

        Order order = razorpayClient.orders.create(orderRequest);
        String orderId = order.get("id");
        log.info("Razorpay order created successfully: {}", orderId);
        return orderId;
    }

    /**
     * Verifies Razorpay signature for a payment.
     * @param razorpayOrderId Order ID returned by Razorpay
     * @param razorpayPaymentId Payment ID returned by Razorpay
     * @param razorpaySignature Signature returned by Razorpay
     * @return true if verification successful
     * @throws PaymentVerificationException if signature mismatch
     */
    public boolean verifyPayment(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature)
            throws PaymentVerificationException {
        try {
            String payload = razorpayOrderId + "|" + razorpayPaymentId;

            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), "HmacSHA256");
            mac.init(secretKeySpec);

            byte[] actualHash = mac.doFinal(payload.getBytes());
            String generatedSignature = Base64.getEncoder().encodeToString(actualHash);

            if (!generatedSignature.equals(razorpaySignature)) {
                log.error("Payment signature mismatch. Expected: {}, Actual: {}", generatedSignature, razorpaySignature);
                throw new PaymentVerificationException("Invalid Razorpay signature");
            }
            log.info("Payment verified successfully for paymentId: {}", razorpayPaymentId);
            return true;
        } catch (Exception e) {
            log.error("Error verifying Razorpay signature", e);
            return false;
//            throw new PaymentVerificationException("Failed to verify Razorpay payment", e);
        }
    }

    /**
     * Initiates a refund using Razorpay API.
     * @param paymentId Razorpay payment_id for which refund is to be initiated.
     * @param amountInPaise Refund amount in paise
     * @return refundId from Razorpay
     * @throws RazorpayException if refund fails
     */
    public String initiateRefund(String paymentId, double amountInPaise) throws RazorpayException {
        log.info("Initiating Razorpay refund for payment: {} with amount: {}", paymentId, amountInPaise);

        JSONObject refundRequest = new JSONObject();
        refundRequest.put("amount", (int) amountInPaise);
        refundRequest.put("speed", "normal");

        com.razorpay.Refund refund = razorpayClient.payments.refund(paymentId, refundRequest);
        String refundId = refund.get("id");
        log.info("Razorpay refund initiated successfully: {}", refundId);
        return refundId;
    }

    public String getRazorpayKeyId() {
        return razorpayConfig.getKeyId();
    }
}
