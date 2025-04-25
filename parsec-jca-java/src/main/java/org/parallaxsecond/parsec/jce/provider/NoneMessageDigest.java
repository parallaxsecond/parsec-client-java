package org.parallaxsecond.parsec.jce.provider;

import java.security.MessageDigest;

public class NoneMessageDigest extends MessageDigest {
    public NoneMessageDigest(ParsecClientAccessor unused) {
        this();
    }

    public NoneMessageDigest() {
        super("None");
        init();
    }

    private void init() {
        this.buf = new DynamicByteBuffer(1024, 2);
    }

    private DynamicByteBuffer buf;

    @Override
    protected void engineUpdate(byte input) {
        buf.put(input);
    }

    @Override
    protected void engineUpdate(byte[] input, int offset, int len) {
        buf.put(input, offset, len);
    }

    @Override
    protected byte[] engineDigest() {
        byte[] copy = new byte[buf.position()];
        buf.rewind();
        buf.get(copy);
        buf.clear();
        return copy;
    }

    @Override
    protected void engineReset() {
        if (buf != null) {
            buf.clear();
        }
    }
}
