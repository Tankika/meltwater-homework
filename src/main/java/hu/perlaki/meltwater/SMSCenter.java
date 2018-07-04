package hu.perlaki.meltwater;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

import static java.util.stream.Collectors.toList;

@Slf4j
@RequiredArgsConstructor
public class SMSCenter {

    protected Set<String> subscribedNumbers = new HashSet<>();
    protected Map<String, String> registeredNumbers = new HashMap<>();
    protected Map<String, List<HeldMessage>> heldMessages = new HashMap<>();
    protected Map<String, List<String>> groups = new HashMap<>();

    private final SMSSender smsSender;

    public void register(String identifier, String phoneNumber) {
        log.info("Registering identifier: {} with phoneNumber: {}", identifier, phoneNumber);

        if (registeredNumbers.containsKey(identifier)) {
            throw new RuntimeException("An identifier cannot be registered in the system more than one time");
        }

        registeredNumbers.put(identifier, phoneNumber);
    }

    public void subscribe(String identifier) {
        log.info("Subscribing identifier: {}", identifier);

        String phoneNumber = registeredNumbers.get(identifier);

        if(phoneNumber != null) {
            subscribedNumbers.add(phoneNumber);
            sendHeldMessage(phoneNumber);
        }
    }

    public void unsubscribe(String identifier) {
        log.info("Unsubscribing identifier: {}", identifier);

        String phoneNumber = registeredNumbers.get(identifier);

        subscribedNumbers.remove(phoneNumber);
    }

    public void createGroup(String groupIdentifier, List<String> phonePatterns) {
        log.info("Creating group with identifier: {}, patterns: {}",
                groupIdentifier, String.join(",", phonePatterns));

        final List<String> trimmedPhonePatterns = phonePatterns
                .stream()
                .map(pp -> pp.replace("*", ""))
                .collect(toList());

        groups.put(groupIdentifier, trimmedPhonePatterns);
    }

    public void sendMessage(String senderIdentifier, String receiverIdentifier, String message) {
        String senderPhoneNumber = registeredNumbers.get(senderIdentifier);
        String receiverPhoneNumber = registeredNumbers.get(receiverIdentifier);

        if(senderPhoneNumber == null) {
            throw new RuntimeException("Sender is not registered!");
        } else if (receiverPhoneNumber == null) {
            throw new RuntimeException("Receiver is not registered!");
        } else if (!subscribedNumbers.contains(senderPhoneNumber)) {
            throw new RuntimeException("Sender is not subscribed!");
        }

        if(!subscribedNumbers.contains(receiverPhoneNumber)) {
            holdMessage(message, senderPhoneNumber, receiverPhoneNumber);
        } else {
            smsSender.send(senderPhoneNumber, receiverPhoneNumber, message);
        }
    }

    public void sendBroadcast(String senderIdentifier, String message) {
        String senderPhoneNumber = registeredNumbers.get(senderIdentifier);

        for (String receiverPhoneNumber : subscribedNumbers) {
            smsSender.send(senderPhoneNumber, receiverPhoneNumber, message);
        }
    }

    public void sendGroupMessage(String senderIdentifier, String groupIdentifier, String message) {
        String senderPhoneNumber = registeredNumbers.get(senderIdentifier);
        List<String> phonePatterns = groups.get(groupIdentifier);

        if(phonePatterns == null) {
            return;
        }

        for (String subscribedNumber : subscribedNumbers) {
            for (String phonePattern : phonePatterns) {
                if(subscribedNumber.startsWith(phonePattern)) {
                    smsSender.send(senderPhoneNumber, subscribedNumber, message);
                    break;
                }
            }
        }
    }

    private void holdMessage(String message, String senderPhoneNumber, String receiverPhoneNumber) {
        HeldMessage heldMessage = new HeldMessage(senderPhoneNumber, message);

        List<HeldMessage> receiverHeldMessages = heldMessages.get(receiverPhoneNumber);
        if(receiverHeldMessages != null) {
            receiverHeldMessages.add(heldMessage);
        } else {
            receiverHeldMessages = new ArrayList<>();
            receiverHeldMessages.add(heldMessage);
            heldMessages.put(receiverPhoneNumber, receiverHeldMessages);
        }
    }

    private void sendHeldMessage(String receiverPhoneNumber) {
        List<HeldMessage> receiverHeldMessages = heldMessages.get(receiverPhoneNumber);

        if(receiverHeldMessages != null) {
            for (HeldMessage heldMessage : receiverHeldMessages) {
                smsSender.send(heldMessage.getSenderPhoneNumber(), receiverPhoneNumber, heldMessage.getMessage());
            }
        }
    }
}
