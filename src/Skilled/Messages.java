package Skilled;

import aic2019.Location;
import aic2019.UnitType;

public class Messages {
    private final Injection in;

    private final int[][] messages;
    private final boolean[] hasMessage;
    private int messageCounter = 0;

    private int[] emptyMessage() {
        return new int[in.constants.MESSAGE_SIZE];
    }

    Messages(Injection in) {
        this.in = in;
        this.messages = new int[in.constants.MAX_MESSAGES_INBOX][in.constants.MESSAGE_SIZE];
        this.hasMessage = new boolean[in.constants.MAX_MESSAGES_INBOX];
    }

    public void update() {
        int[] message = in.memoryManager.getNewMessage();
        if(message[0] != 0) {

            // find a slot for the message
            for(int i = 0; i < hasMessage.length; i++) {
                int id = (messageCounter + i)%hasMessage.length;
                if(!hasMessage[id]) {
                    hasMessage[id] = true;
                    messages[id] = message;
                    in.memoryManager.clearMessageMine();
                    break;
                }
            }
        }
    }

    public boolean hasMessage() {
        return hasMessage[messageCounter];
    }

    public int[] readMessage() {
        if(this.hasMessage()) {
            int[] message = messages[messageCounter];
            return message;
        }
        return this.emptyMessage();
    }

    public void nextMessage() {
        hasMessage[messageCounter] = false;
        messageCounter = (messageCounter + 1) % hasMessage.length;
    }

    public void sendCreateAndSendToLocation(int unitId, int quantity, UnitType type, int locX, int locY) {

        int[] params = new int [in.constants.MESSAGE_SIZE];

        params[2] = locX;
        params[3] = locY;

        params[0] = in.helper.unitTypeToInt(type);
        params[1] = quantity;

        in.memoryManager.sendMessageTo(unitId, params);
    }

    public void sendToLocation(int unitId, int locX, int locY) {

        int[] params = new int [in.constants.MESSAGE_SIZE];

        params[2] = locX;
        params[3] = locY;

        in.memoryManager.sendMessageTo(unitId, params);
    }

    public void sendToLocation(int unitId, Location loc) {
       this.sendToLocation(unitId, loc.x, loc.y);
    }
}
