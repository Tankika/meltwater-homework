package hu.perlaki.meltwater;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Slf4j
@RequiredArgsConstructor
public class InputProcessor {

    private final SMSCenter smsCenter;

    public void process() {
        try(FileInputStream fileInputStream = new FileInputStream("src/main/resources/input.txt");
                Scanner scanner = new Scanner(fileInputStream)) {

            while(scanner.hasNext()) {
                String command = scanner.nextLine();

                processCommand(command);
            }
        } catch (FileNotFoundException e) {
            log.error("Input file could not be found", e);
            return;
        } catch (IOException e) {
            log.error("Error while closing the input file", e);
            return;
        }
    }

    private void processCommand(String command) {
        final String[] commandParts = command.split(" ");
        if(command.startsWith("number")) {
            smsCenter.register(commandParts[0], commandParts[1]);
        } else if(command.startsWith("subscribe")) {
            smsCenter.subscribe(commandParts[1]);
        } else if(command.startsWith("unsubscribe")) {
            smsCenter.unsubscribe(commandParts[1]);
        } else if(command.startsWith("group")) {
            createGroup(commandParts);
        } else if(command.startsWith("message")) {
            sendMessage(command, commandParts);
        }
    }

    private void createGroup(String[] commandParts) {
        List<String> phonePatterns = new ArrayList<>();

        for (int i = 1; i < commandParts.length; i++) {
            phonePatterns.add(commandParts[i]);
        }

        smsCenter.createGroup(commandParts[0], phonePatterns);
    }

    private void sendMessage(String command, String[] commandParts) {
        String senderIdentifier = commandParts[1];
        String[] receiverIdentifiers = commandParts[2].split(",");
        String message = command.split("\"")[1];

        for (String receiverIdentifier : receiverIdentifiers) {
            if(receiverIdentifier.startsWith("number")) {
                smsCenter.sendMessage(senderIdentifier, receiverIdentifier, message);
            } else if(receiverIdentifier.startsWith("group")) {
                smsCenter.sendGroupMessage(senderIdentifier, receiverIdentifier, message);
            } if(receiverIdentifier.equals("broadcast")) {
                smsCenter.sendBroadcast(senderIdentifier, message);
            }
        }
    }
}
