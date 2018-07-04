package hu.perlaki.meltwater;

public class SMSSender {

    public void send(String senderPhoneNumber, String receiverPhoneNumber, String message) {
        String output = String.format("%s %s \"%s\"", senderPhoneNumber, receiverPhoneNumber, message);

        System.out.println(output);
    }
}
