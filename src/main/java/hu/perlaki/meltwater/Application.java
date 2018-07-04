package hu.perlaki.meltwater;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Application {

    public static void main(String args[]) {
        SMSSender smsSender = new SMSSender();
        SMSCenter smsCenter = new SMSCenter(smsSender);
        InputProcessor processor = new InputProcessor(smsCenter);

        processor.process();
    }
}
