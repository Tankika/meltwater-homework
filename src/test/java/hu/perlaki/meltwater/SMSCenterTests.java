package hu.perlaki.meltwater;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(MockitoJUnitRunner.class)
public class SMSCenterTests {

    private static final String MESSAGE = "MESSAGE";
    private static final String MESSAGE_2 = "MESSAGE 2";
    private static final String MESSAGE_3 = "MESSAGE 3";
    private static final String IDENTIFIER_1 = "number1";
    private static final String IDENTIFIER_2 = "number2";
    private static final String IDENTIFIER_3 = "number3";
    private static final String PHONE_NUMBER_1 = "+36991212321";
    private static final String PHONE_NUMBER_2 = "+36123456789";
    private static final String PHONE_NUMBER_3 = "+36987654321";
    public static final String GROUP_IDENTIFIER = "group1";
    public static final String TRIMMED_PHONE_PATTERN_1 = "+369";
    private static final String TRIMMED_PHONE_PATTERN_2 = "+36123";
    public static final String PHONE_PATTERN_1 = TRIMMED_PHONE_PATTERN_1 + "*";
    private static final String PHONE_PATTERN_2 = TRIMMED_PHONE_PATTERN_2 + "*";

    @Mock
    private SMSSender smsSender;

    private SMSCenter smsCenter;

    @Before
    public void setup() {
         smsCenter = new SMSCenter(smsSender);
    }

    @Test
    public void registerShouldStoreNumberAsAvailable() {
        // GIVEN

        // WHEN
        smsCenter.register(IDENTIFIER_1, PHONE_NUMBER_1);

        // THEN
        assertThat(smsCenter.registeredNumbers).containsEntry(IDENTIFIER_1, PHONE_NUMBER_1);
    }

    @Test
    public void registerShouldAllowSameNumberWithDifferentIdentifiers() {
        // GIVEN

        // WHEN
        smsCenter.register(IDENTIFIER_1, PHONE_NUMBER_1);
        smsCenter.register(IDENTIFIER_2, PHONE_NUMBER_1);

        // THEN
        assertThat(smsCenter.registeredNumbers).containsEntry(IDENTIFIER_1, PHONE_NUMBER_1);
        assertThat(smsCenter.registeredNumbers).containsEntry(IDENTIFIER_2, PHONE_NUMBER_1);
    }

    @Test(expected = RuntimeException.class)
    public void registerShouldThrowErrorOnDuplicateIdentifier() {
        // GIVEN

        // WHEN
        smsCenter.register(IDENTIFIER_1, PHONE_NUMBER_1);
        smsCenter.register(IDENTIFIER_1, PHONE_NUMBER_1);

        // THEN
    }

    @Test
    public void subscribeShouldSetRegisteredNumberAsSubscribed() {
        // GIVEN
        smsCenter.registeredNumbers.put(IDENTIFIER_1, PHONE_NUMBER_1);

        // WHEN
        smsCenter.subscribe(IDENTIFIER_1);

        // THEN
        assertThat(smsCenter.subscribedNumbers).contains(PHONE_NUMBER_1);
    }

    @Test
    public void subscribeShouldIgnoreUnregisteredNumbers() {
        // GIVEN

        // WHEN
        smsCenter.subscribe(IDENTIFIER_1);

        // THEN
        assertThat(smsCenter.subscribedNumbers).isEmpty();
    }

    @Test
    public void unsubscribeShouldRemoveNumberFromSubscribeds() {
        // GIVEN
        smsCenter.registeredNumbers.put(IDENTIFIER_1, PHONE_NUMBER_1);
        smsCenter.subscribedNumbers.add(PHONE_NUMBER_1);

        // WHEN
        smsCenter.unsubscribe(IDENTIFIER_1);

        // THEN
        assertThat(smsCenter.subscribedNumbers).isEmpty();
    }

    @Test
    public void unsubscribeShouldIgnoreUnregisteredIdentifier() {
        // GIVEN
        smsCenter.subscribedNumbers.add(PHONE_NUMBER_1);

        // WHEN
        smsCenter.unsubscribe(IDENTIFIER_1);

        // THEN
        assertThat(smsCenter.subscribedNumbers).contains(PHONE_NUMBER_1);
    }

    @Test
    public void unsubscribeShouldIgnoreNumbersAlreadyUnsubscribed() {
        // GIVEN
        smsCenter.registeredNumbers.put(IDENTIFIER_1, PHONE_NUMBER_1);

        // WHEN
        smsCenter.unsubscribe(IDENTIFIER_1);

        // THEN
        assertThat(smsCenter.subscribedNumbers).isEmpty();
    }

    @Test
    public void unsubscribeShouldKeepNumberRegistered() {
        // GIVEN
        smsCenter.registeredNumbers.put(IDENTIFIER_1, PHONE_NUMBER_1);
        smsCenter.subscribedNumbers.add(PHONE_NUMBER_1);

        // WHEN
        smsCenter.unsubscribe(IDENTIFIER_1);

        // THEN
        assertThat(smsCenter.registeredNumbers).containsEntry(IDENTIFIER_1, PHONE_NUMBER_1);
    }

    @Test
    public void sendMessageShouldSendMessageOnSenderAndReceiverSubscribed() {
        // GIVEN
        String senderIdentifier = IDENTIFIER_1;
        String senderPhoneNumber = PHONE_NUMBER_1;
        smsCenter.registeredNumbers.put(senderIdentifier, senderPhoneNumber);
        smsCenter.subscribedNumbers.add(senderPhoneNumber);

        String receiverIdentifier = IDENTIFIER_2;
        String receiverPhoneNumber = PHONE_NUMBER_2;
        smsCenter.registeredNumbers.put(receiverIdentifier, receiverPhoneNumber);
        smsCenter.subscribedNumbers.add(receiverPhoneNumber);

        // WHEN
        smsCenter.sendMessage(senderIdentifier, receiverIdentifier, MESSAGE);

        // THEN
        then(smsSender).should().send(senderPhoneNumber, receiverPhoneNumber, MESSAGE);
    }

    @Test(expected = RuntimeException.class)
    public void sendMessageShouldErrorOnUnregisteredSender() {
        // GIVEN
        String senderIdentifier = IDENTIFIER_1;
        String senderPhoneNumber = PHONE_NUMBER_1;
        smsCenter.subscribedNumbers.add(senderPhoneNumber);

        String receiverIdentifier = IDENTIFIER_2;
        String receiverPhoneNumber = PHONE_NUMBER_2;
        smsCenter.registeredNumbers.put(receiverIdentifier, receiverPhoneNumber);
        smsCenter.subscribedNumbers.add(receiverPhoneNumber);

        // WHEN
        smsCenter.sendMessage(senderIdentifier, receiverIdentifier, MESSAGE);

        // THEN
        verifyZeroInteractions(smsSender);
    }

    @Test(expected = RuntimeException.class)
    public void sendMessageShouldErrorOnUnregisteredReceiver() {
        // GIVEN
        String senderIdentifier = IDENTIFIER_1;
        String senderPhoneNumber = PHONE_NUMBER_1;
        smsCenter.registeredNumbers.put(senderIdentifier, senderPhoneNumber);
        smsCenter.subscribedNumbers.add(senderPhoneNumber);

        String receiverIdentifier = IDENTIFIER_2;
        String receiverPhoneNumber = PHONE_NUMBER_2;
        smsCenter.subscribedNumbers.add(receiverPhoneNumber);

        // WHEN
        smsCenter.sendMessage(senderIdentifier, receiverIdentifier, MESSAGE);

        // THEN
        verifyZeroInteractions(smsSender);
    }

    @Test(expected = RuntimeException.class)
    public void sendMessageShouldErrorOnUnSubscribedSender() {
        // GIVEN
        String senderIdentifier = IDENTIFIER_1;
        String senderPhoneNumber = PHONE_NUMBER_1;
        smsCenter.registeredNumbers.put(senderIdentifier, senderPhoneNumber);

        String receiverIdentifier = IDENTIFIER_2;
        String receiverPhoneNumber = PHONE_NUMBER_2;
        smsCenter.registeredNumbers.put(receiverIdentifier, receiverPhoneNumber);
        smsCenter.subscribedNumbers.add(receiverPhoneNumber);

        // WHEN
        smsCenter.sendMessage(senderIdentifier, receiverIdentifier, MESSAGE);

        // THEN
        verifyZeroInteractions(smsSender);
    }

    @Test
    public void sendMessageShouldHoldMessagesOnUnsubscribedReceiver() {
        // GIVEN
        String senderIdentifier = IDENTIFIER_1;
        String senderPhoneNumber = PHONE_NUMBER_1;
        smsCenter.registeredNumbers.put(senderIdentifier, senderPhoneNumber);
        smsCenter.subscribedNumbers.add(senderPhoneNumber);

        String receiverIdentifier = IDENTIFIER_2;
        String receiverPhoneNumber = PHONE_NUMBER_2;
        smsCenter.registeredNumbers.put(receiverIdentifier, receiverPhoneNumber);

        String receiverIdentifier2 = IDENTIFIER_3;
        String receiverPhoneNumber2 = PHONE_NUMBER_3;
        smsCenter.registeredNumbers.put(receiverIdentifier2, receiverPhoneNumber2);

        // WHEN
        smsCenter.sendMessage(senderIdentifier, receiverIdentifier, MESSAGE);
        smsCenter.sendMessage(senderIdentifier, receiverIdentifier, MESSAGE_2);
        smsCenter.sendMessage(senderIdentifier, receiverIdentifier2, MESSAGE_3);

        // THEN
        assertThat(smsCenter.heldMessages).containsKey(receiverPhoneNumber);
        assertThat(smsCenter.heldMessages).containsKey(receiverPhoneNumber2);
        assertThat(smsCenter.heldMessages.get(receiverPhoneNumber))
                .contains(new HeldMessage(senderPhoneNumber, MESSAGE), new HeldMessage(senderPhoneNumber, MESSAGE_2));
        assertThat(smsCenter.heldMessages.get(receiverPhoneNumber2))
                .contains(new HeldMessage(senderPhoneNumber, MESSAGE_3));
        verifyZeroInteractions(smsSender);
    }

    @Test
    public void sendMessageShouldSendHeldMessagesOnSubscribe() {
        // GIVEN
        String senderPhoneNumber = PHONE_NUMBER_1;
        String receiverIdentifier = IDENTIFIER_1;
        String receiverPhoneNumber = PHONE_NUMBER_2;
        smsCenter.registeredNumbers.put(receiverIdentifier, receiverPhoneNumber);
        smsCenter.heldMessages.put(receiverPhoneNumber,
                asList(new HeldMessage(senderPhoneNumber, MESSAGE), new HeldMessage(senderPhoneNumber, MESSAGE_2)));

        // WHEN
        smsCenter.subscribe(receiverIdentifier);

        // THEN
        then(smsSender).should().send(senderPhoneNumber, receiverPhoneNumber, MESSAGE);
        then(smsSender).should().send(senderPhoneNumber, receiverPhoneNumber, MESSAGE_2);
    }

    @Test
    public void sendBroadcastShouldSendMessageToAllSubscribedNumbers() {
        // GIVEN
        String senderIdentifier = IDENTIFIER_1;
        String senderPhoneNumber = PHONE_NUMBER_1;
        smsCenter.registeredNumbers.put(senderIdentifier, senderPhoneNumber);
        smsCenter.subscribedNumbers.add(senderPhoneNumber);

        String receiverIdentifier = IDENTIFIER_2;
        String receiverPhoneNumber = PHONE_NUMBER_2;
        smsCenter.registeredNumbers.put(receiverIdentifier, receiverPhoneNumber);
        smsCenter.subscribedNumbers.add(receiverPhoneNumber);

        String receiverIdentifier2 = IDENTIFIER_3;
        String receiverPhoneNumber2 = PHONE_NUMBER_3;
        smsCenter.registeredNumbers.put(receiverIdentifier2, receiverPhoneNumber2);
        smsCenter.subscribedNumbers.add(receiverPhoneNumber2);

        // WHEN
        smsCenter.sendBroadcast(senderIdentifier, MESSAGE);

        // THEN
        then(smsSender).should().send(senderPhoneNumber, receiverPhoneNumber, MESSAGE);
        then(smsSender).should().send(senderPhoneNumber, receiverPhoneNumber2, MESSAGE);
    }

    @Test
    public void createGroupShouldCreateGroup() {
        // GIVEN
        List<String> phonePatterns = asList(PHONE_PATTERN_1, PHONE_PATTERN_2);

        // WHEN
        smsCenter.createGroup(GROUP_IDENTIFIER, phonePatterns);

        // THEN
        assertThat(smsCenter.groups).containsKey(GROUP_IDENTIFIER);
        assertThat(smsCenter.groups.get(GROUP_IDENTIFIER)).contains(TRIMMED_PHONE_PATTERN_1, TRIMMED_PHONE_PATTERN_2);
    }

    @Test
    public void sendGroupMessageShouldSendToAllMatchedNumbers() {
        // GIVEN
        String senderIdentifier = IDENTIFIER_1;
        String senderPhoneNumber = PHONE_NUMBER_1;
        smsCenter.registeredNumbers.put(senderIdentifier, senderPhoneNumber);
        smsCenter.subscribedNumbers.add(senderPhoneNumber);

        String receiverIdentifier = IDENTIFIER_2;
        String receiverPhoneNumber = PHONE_NUMBER_2;
        smsCenter.registeredNumbers.put(receiverIdentifier, receiverPhoneNumber);
        smsCenter.subscribedNumbers.add(receiverPhoneNumber);

        String receiverIdentifier2 = IDENTIFIER_3;
        String receiverPhoneNumber2 = PHONE_NUMBER_3;
        smsCenter.registeredNumbers.put(receiverIdentifier2, receiverPhoneNumber2);
        smsCenter.subscribedNumbers.add(receiverPhoneNumber2);

        smsCenter.groups.put(GROUP_IDENTIFIER, asList(TRIMMED_PHONE_PATTERN_1));

        // WHEN
        smsCenter.sendGroupMessage(senderIdentifier, GROUP_IDENTIFIER, MESSAGE);

        // THEN
        then(smsSender).should().send(senderPhoneNumber, senderPhoneNumber, MESSAGE);
        then(smsSender).should().send(senderPhoneNumber, receiverPhoneNumber2, MESSAGE);
    }
}
