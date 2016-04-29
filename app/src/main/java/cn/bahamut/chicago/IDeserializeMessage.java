package cn.bahamut.chicago;

/**
 * Created by alexchow on 16/4/29.
 */
public interface IDeserializeMessage {
    ClientBaseMessage getMessageFromBuffer(byte[] receiveBuffer, int len);
}
