package hu.perlaki.meltwater;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HeldMessage {
    String senderPhoneNumber;
    String message;
}
