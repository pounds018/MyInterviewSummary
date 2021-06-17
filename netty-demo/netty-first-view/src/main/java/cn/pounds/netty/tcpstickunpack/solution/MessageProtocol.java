package cn.pounds.netty.tcpstickunpack.solution;

/**
 * @Date 2021/6/17 22:48
 * @Author by pounds
 * @Description tcp粘包拆包解决方案, 自定义协议加编码解码器
 * 自定义协议示例
 */
public class MessageProtocol {
    private int messageLen;
    private byte[] content;

    public int getMessageLen() {
        return messageLen;
    }

    public void setMessageLen(int messageLen) {
        this.messageLen = messageLen;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
