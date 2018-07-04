## Application
The application has two main modules, InputProcessor and SMSCenter.

InputProcessor is a rather simple class, that reads the input file line by line and decides what to do based on the commands. This class's responsibility is to determine what to do for the different commands and it also transforms additional parameters into a more structured format. The class does not have logic on handling messages, it delegates all the work to the SMSCenter class.

The SMSCenter class holds the main logic of the application. The logic itself is described using the SMSCenterTests class (written with Test-Driven Development). This class holds the state of the application, i.e. the currently registered and subscribed phones and the messages waiting to be delivered for unsubscribed phones. SMSCenter does all the logic on what should be send from which sender to which receiver, but the actual sending is not implemented here. The sending of the messages, which is currently a simple write to the console, is implemented in a separate class, SMSSender. This separation of concerns help us test the classes separately and enables us to mock the actual sending and focus only on the business logic.
## Assumptions and decisions
### Error on example input
When the program is run with the example input, it will throw an exception at certain point. This is as intended, and the cause is, that the program does not allow unsubscribed phones to send messages.
### Phone number patterns
The program assumes, that all phone number patterns has an asterisk at the end of them, thus phone number pattern matching will look at the start of the matched numbers.
### Input
For faster development, the input file's location was hard-coded into the program. In a real, production environment this path would be configurable some way, for example as a command line argument.
### Duplicate numbers
The homework does not state anything about duplicate numbers. Assumption is that the same number can exist in the system multiple times with different identifiers, but an identifier cannot exist in the system more than once.
### Exceptions
The applications throws plain RuntimeExceptions on unexpected states. This decision was made to speed up development, on a real project, instead of general exceptions, dedicated ones would be created.
### Tests and corner cases
Even though a number of unit tests has been created for the program, there are a great number of tests that could have been written. These tests have not been written because of time shortage. Also there are a number of corner cases that has not been handled in the program, also because of time shortage. The program currently expects, that everything is used as intended.
